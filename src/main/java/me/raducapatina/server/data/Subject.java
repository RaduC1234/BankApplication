package me.raducapatina.server.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "subjects")
@NoArgsConstructor
public class Subject {

    public Subject(String name, User teacher) {
        this.name = name;
        users.add(teacher);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "subjects")
    private Set<User> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "subject")
    private Set<Grade> grades = new LinkedHashSet<>();

    public void addUser(User user) {

        if (user.getType().equals(UserType.TEACHER))
            throw new IllegalStateException();

        users.add(user);
    }

    public boolean removeUser(User user) {
        return users.remove(user);
    }

    public User getTeacher() {
        for(User u : users) {
            if(u.getType().equals(UserType.TEACHER))
                return u;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Subject subject = (Subject) o;
        return id != null && Objects.equals(id, subject.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}