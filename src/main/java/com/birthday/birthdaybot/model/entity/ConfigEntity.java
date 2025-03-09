package com.birthday.birthdaybot.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CONFIG")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {
    @Id
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;
}
