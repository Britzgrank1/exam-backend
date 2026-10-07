package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "persons")

public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String name;

    @Column(name = "tmdb_person_id")
    Integer tmbdPersonId;

    @OneToMany(mappedBy = "person")
    Set<Credit> credits = new HashSet<>();



}
