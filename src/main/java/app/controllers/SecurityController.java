package app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import app.DAO.SecurityDAO;
import app.DTO.UserDTO;
import app.DTO.LoginDTO;
import app.exceptions.ApiException;
import app.security.TokenSecurity;
import app.utils.Utils;

import io.javalin.http.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityDAO securityDAO;
    private final TokenSecurity tokenSecurity = new TokenSecurity();

    public SecurityController(SecurityDAO securityDAO) {
        this.securityDAO = securityDAO;
    }

    //#region login
    public void login(Context ctx) {
        ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client

        LoginDTO input = ctx.bodyAsClass(LoginDTO.class);
        UserDTO verifiedUser = securityDAO.getVerifiedUser(input.getUsername(), input.getPassword());
        String token = createToken(verifiedUser);

        ctx.status(200).json(returnObject
                .put("token",token)
                .put("username",verifiedUser.getUsername()));
    }

    private String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            logger.error("Could not create token", e);
            throw new ApiException(500, "Could not create token");
        }
    }

    //#endregion

    //#region register
    public void register(Context ctx) // to get a user
    {
        LoginDTO input = ctx.bodyAsClass(LoginDTO.class);

        if (input.getUsername() == null || input.getUsername().isBlank()
            || input.getPassword() == null || input.getPassword().isBlank()){
            throw new ApiException(400,"Username and password is required");
        }
        UserDTO created = securityDAO.createUser(input.getUsername(), input.getPassword());
        ctx.status(201).json(created);
    }

    //#region authentication
    public void authenticate(Context ctx) {
        // This is a preflight request => no need for authentication
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }
        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
        Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
        if (isOpenEndpoint(allowedRoles))
            return;

        // If there is no token we do not allow entry
        UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
        ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
    }

    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }

    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new ApiException(401,"Invalid user or token");
        }
        return verifiedTokenUser;
    }

    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new ApiException(401,"Authorization header is missing");
        }

        String[] parts = header.split(" ");
        if (parts.length != 2 || !parts[0].equals("Bearer")){
            throw new ApiException(401,"Invalid format used");
        }
        return parts[1];
    }

    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") :
                Utils.getPropertyValue("SECRET_KEY", "config.properties");

        if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
            return tokenSecurity.getUserWithRolesFromToken(token);
        }
        throw new ApiException(401,"Token is not valid or expired");
    }


    //#region authorization
    public void authorize(Context ctx) {
        Set<String> allowedRoles = ctx.routeRoles()
                .stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
        if (isOpenEndpoint(allowedRoles))
            return;
        // 2. Get user and ensure it is not null
        UserDTO user = ctx.attribute("user");
        if (user == null) {
            throw new ApiException(401,"No user was added from the token");
        }
        // 3. See if any role matches
        if (!userHasAllowedRole(user, allowedRoles))
            throw new ApiException(403,"User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }


}
