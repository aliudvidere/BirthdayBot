package com.birthday.birthdaybot.model.entity;

import com.birthday.birthdaybot.constants.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "tg_code", unique = true, nullable = false)
    private String tgCode;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private RoleEnum role;

    public BotUserEntity(String tgCode) {
        this.tgCode = tgCode;
        this.role = RoleEnum.USER;
    }
}
