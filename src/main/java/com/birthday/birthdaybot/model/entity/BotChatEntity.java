package com.birthday.birthdaybot.model.entity;

import com.birthday.birthdaybot.constants.LangugeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BOT_CHAT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotChatEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,
            generator="bot_chat_id_seq")
    @SequenceGenerator(name="bot_chat_id_seq",
            sequenceName="bot_chat_id_seq", allocationSize=1)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "chatId", unique = true, nullable = false)
    private String chatId;

    @Column(name = "need_notify", nullable = false)
    private Boolean needNotify;

    @Enumerated(EnumType.STRING)
    @Column(name="language", nullable = false)
    private LangugeEnum language;

    @Column(name = "admin_notify", nullable = false)
    private Boolean adminNotify;

    public BotChatEntity(String chatId) {
        this.chatId = chatId;
        this.needNotify = true;
        this.language = LangugeEnum.RU;
        this.adminNotify = false;
    }
}
