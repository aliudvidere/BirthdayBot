package com.birthday.birthdaybot.model.entity;

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

    public BotChatEntity(String chatId) {
        this.chatId = chatId;
        this.needNotify = true;
    }
}
