package me.raducapatina.server.data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// to prevent chain reference
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")

@Getter
@Setter
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findByUsername", query = "select u from User u where u.username = :username"),
})
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType type;

    @OneToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private Set<Subject> subjects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Grade> grades = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
