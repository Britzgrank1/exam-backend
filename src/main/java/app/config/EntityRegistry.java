package app.config;


import app.entities.*;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
       configuration.addAnnotatedClass(Movie.class);
       configuration.addAnnotatedClass(Genre.class);
       configuration.addAnnotatedClass(User.class);
       configuration.addAnnotatedClass(Person.class);
       configuration.addAnnotatedClass(Credit.class);
       configuration.addAnnotatedClass(UserMovieRating.class);
       configuration.addAnnotatedClass(Watchlist.class);
       configuration.addAnnotatedClass(Role.class);
    }
}