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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    private User teacher;
}
