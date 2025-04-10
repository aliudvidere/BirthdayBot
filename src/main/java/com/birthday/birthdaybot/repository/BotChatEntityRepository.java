package com.birthday.birthdaybot.repository;

import com.birthday.birthdaybot.model.entity.BotChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BotChatEntityRepository extends JpaRepository<BotChatEntity, Integer> {

    Optional<BotChatEntity> findByChatId(String chatId);

    List<BotChatEntity> findByNeedNotifyTrue();

    List<BotChatEntity> findByAdminNotifyTrue();

    Boolean existsByChatId(String chatId);

}
