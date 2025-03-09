package com.birthday.birthdaybot.repository;

import com.birthday.birthdaybot.model.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigEntityRepository extends JpaRepository<ConfigEntity, String> {



}
