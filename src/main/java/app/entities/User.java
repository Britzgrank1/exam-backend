package app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Table(name = "users")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(unique = true, nullable = false)
    String username;

    @Setter
    String email;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Column(name = "password_hash",nullable = false,length = 60)
    String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "users")
    Set<UserMovieRating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "users")
    Set<Watchlist> watchlists = new HashSet<>();


    public User(String username, String password) {
        this.username = Objects.requireNonNull(username);
        if (password == null || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain 1 to 72 UTF-8 bytes and not be blank");
        }
        this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean verifyPassword(String password) {
        return password != null
                && password.getBytes(StandardCharsets.UTF_8).length <= 72
                && BCrypt.checkpw(password, this.password);
    }

    public void addRole(Role role) {
        roles.add(Objects.requireNonNull(role));
    }

    public Set<String> getRolesAsStrings() {
        return roles.stream().map(Role::getRoleName).collect(Collectors.toSet());
    }
}
