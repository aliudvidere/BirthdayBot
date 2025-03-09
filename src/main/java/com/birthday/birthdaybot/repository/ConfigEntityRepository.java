package com.birthday.birthdaybot.repository;

import com.birthday.birthdaybot.model.entity.BirthdayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BirthdayEntityRepository extends JpaRepository<BirthdayEntity, Integer> {

        @Query(value = """

                SELECT *\s
                     FROM birthday b\s
                     WHERE\s
                         MAKE_DATE(
                             CASE\s
                                 WHEN MAKE_DATE(
                                     EXTRACT(YEAR FROM CURRENT_DATE)::INT,\s
                                     CASE\s
                                         WHEN DATE_PART('month', b.birthday) = 2\s
                                          AND DATE_PART('day', b.birthday) = 29\s
                                          AND NOT (
                                             EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0\s
                                             AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)
                                          )\s
                                         THEN 3 \s
                                         ELSE DATE_PART('month', b.birthday)::INT\s
                                     END,
                                     CASE\s
                                         WHEN DATE_PART('month', b.birthday) = 2\s
                                          AND DATE_PART('day', b.birthday) = 29\s
                                          AND NOT (
                                             EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0\s
                                             AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)
                                          )\s
                                         THEN 1 \s
                                         ELSE DATE_PART('day', b.birthday)::INT\s
                                     END
                                 ) < CURRENT_DATE \s
                                 THEN EXTRACT(YEAR FROM CURRENT_DATE)::INT + 1 \s
                                 ELSE EXTRACT(YEAR FROM CURRENT_DATE)::INT \s
                             END,
                             CASE\s
                                 WHEN DATE_PART('month', b.birthday) = 2\s
                                  AND DATE_PART('day', b.birthday) = 29\s
                                  AND NOT (
                                     EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0\s
                                     AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)
                                  )\s
                                 THEN 3 \s
                                 ELSE DATE_PART('month', b.birthday)::INT\s
                             END,
                             CASE\s
                                 WHEN DATE_PART('month', b.birthday) = 2\s
                                  AND DATE_PART('day', b.birthday) = 29\s
                                  AND NOT (
                                     EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0\s
                                     AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)
                                  )\s
                                 THEN 1 \s
                                 ELSE DATE_PART('day', b.birthday)::INT\s
                             END
                         )\s
                         BETWEEN NOW() AND :date
                     
                     
        """, nativeQuery = true)
    List<BirthdayEntity> findUpcomingBirthdays(@Param("date") LocalDate date);


}
