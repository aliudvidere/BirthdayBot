package com.birthday.birthdaybot.model.entity;

import com.birthday.birthdaybot.constants.RoleEnum;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "BOT_USER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotUserEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,
            generator="bot_user_id_seq")
    @SequenceGenerator(name="bot_user_id_seq",
            sequenceName="bot_user_id_seq", allocationSize=1)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private RoleEnum role;

    public BotUserEntity(String username) {
        this.username = username;
        this.role = RoleEnum.USER;
    }
}
