package me.raducapatina.server.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

   /* @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "Subjects",
            joinColumns = {@JoinColumn("subject_id")},
            inverseJoinColumns = {@JoinColumn("users")}
    )
    private Set<User> userList;*/
}