package app.routes;

import app.Main;
import app.config.HibernateTestConfig;
import app.entities.Movie;
import app.testutils.MovieTestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class RestApiTest {

    private static final String BASE_URL = "http://localhost:7777/api";
    private static Javalin app;
    private static EntityManagerFactory emf;

    private Map<String, Movie> seeded;

    @BeforeAll
    static void setUpAll() {
        RestAssured.baseURI = BASE_URL;
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = Main.startServer(7777, emf);   // samme app som i produktion, bare med test-database og port 7777
    }

    @AfterAll
    static void tearDownAll() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        clearUsers();
        seeded = MovieTestPopulator.populate(emf);
    }

    // Mine endpoints(Uden at logge ind)

    @Test
    void getAllMovies() {
        given()
                .when()
                .get("/movies")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("title", hasItems("Test Movie 1", "Test Movie 2", "Test Movie 3"));
    }

    //  Security

    @Test
    void register_withoutPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "username": "anna", "password": "test1234" }
                        """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("username", is("anna"))
                .body("roles", hasItem("user"))
                .body("$", not(hasKey("password")));
    }

    @Test
    void login_withWrongPassword() {
        registerAndLogin("anna", "test1234");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "username": "anna", "password": "forkert" }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void rateMovie_withoutToken() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "rating": 8 }
                        """)
                .when()
                .post("/movies/" + seeded.get("movie1").getId() + "/rating")
                .then()
                .statusCode(401);
    }

    @Test
    void rateMovie_withInvalidRating() {
        String token = registerAndLogin("anna", "test1234");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        { "rating": 11 }
                        """)
                .when()
                .post("/movies/" + seeded.get("movie1").getId() + "/rating")
                .then()
                .statusCode(400);
    }

    // case: Testen nedenfor er muligvis den case jeg fremviser

    @Test
    void examStory_register_login_rate_recommend() {
        // 1+2. Registrer og log ind
        String token = registerAndLogin("anna", "test1234");

        // 3. Vurder Test Movie 1 (genre: Action) og giver den en rating og kommentar
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        { "rating": 9, "comment": "Super god" }
                        """)
                .when()
                .post("/movies/" + seeded.get("movie1").getId() + "/rating")
                .then()
                .statusCode(200)
                .body("movieTitle", is("Test Movie 1"))
                .body("username", is("anna"))
                .body("rating", is(9));

        // 4. Få anbefalinger: her er Test Movie 2 den eneste der deler genren Action. Movie 3 er kun Drama
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/recommendations")
                .then()
                .statusCode(200)
                .body("becauseYouLiked", contains("Test Movie 1"))
                .body("recommendations.title", contains("Test Movie 2"));
    }

    // Mine hjælpemetoder

    private String registerAndLogin(String username, String password) {
        String body = """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/auth/register")
                .then().statusCode(201);

        return given().contentType(ContentType.JSON).body(body)
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    private void clearUsers() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE users, app_role RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }
}