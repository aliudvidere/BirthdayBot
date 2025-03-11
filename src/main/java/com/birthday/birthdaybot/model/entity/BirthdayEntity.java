package com.birthday.birthdaybot.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "BIRTHDAY")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,
            generator="birthday_id_seq")
    @SequenceGenerator(name="birthday_id_seq",
            sequenceName="birthday_id_seq", allocationSize=1)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "login")
    private String login;

    @Column(name = "team")
    private String team;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Override
    public String toString() {
        return fullName + " - " + team + " - " + birthday;
    }

    public String toStringForDelete() {
        return "<b>%d</b>".formatted(id) + ". " + fullName + " - " + team + " - " + birthday;
    }
}
