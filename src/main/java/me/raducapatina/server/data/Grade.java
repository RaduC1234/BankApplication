package me.raducapatina.server.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "mark", nullable = false)
    private String mark;

    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY) // do not remove this to prevent loading loops
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)  // do not remove this to prevent loading loops
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
