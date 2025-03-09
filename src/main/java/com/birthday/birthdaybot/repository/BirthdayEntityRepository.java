package com.birthday.birthdaybot.repository;

import com.birthday.birthdaybot.model.entity.BotUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserEntityRepository extends JpaRepository<BotUserEntity, Integer> {
    Optional<BotUserEntity> findByTgCode(String tgCode);

}
