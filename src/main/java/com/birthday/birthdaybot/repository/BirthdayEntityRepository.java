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
        WITH adjusted_birthdays AS (
            SELECT 
                b.*, 
                MAKE_DATE(
                    CASE 
                        WHEN MAKE_DATE(EXTRACT(YEAR FROM CURRENT_DATE)::INT, 
                                       DATE_PART('month', b.birthday)::INT, 
                                       DATE_PART('day', b.birthday)::INT) 
                             < CURRENT_DATE 
                        THEN EXTRACT(YEAR FROM CURRENT_DATE)::INT + 1 
                        ELSE EXTRACT(YEAR FROM CURRENT_DATE)::INT 
                    END,
                    CASE 
                        WHEN DATE_PART('month', b.birthday) = 2 
                         AND DATE_PART('day', b.birthday) = 29 
                         AND NOT (EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0 
                                  AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 
                                       OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)) 
                        THEN 3  -- Adjust leap year birthdays to March 1st
                        ELSE DATE_PART('month', b.birthday)::INT 
                    END,
                    CASE 
                        WHEN DATE_PART('month', b.birthday) = 2 
                         AND DATE_PART('day', b.birthday) = 29 
                         AND NOT (EXTRACT(YEAR FROM CURRENT_DATE) % 4 = 0 
                                  AND (EXTRACT(YEAR FROM CURRENT_DATE) % 100 <> 0 
                                       OR EXTRACT(YEAR FROM CURRENT_DATE) % 400 = 0)) 
                        THEN 1  -- Adjust leap year birthdays to March 1st
                        ELSE DATE_PART('day', b.birthday)::INT 
                    END
                ) AS next_birthday
            FROM birthday b
        )
        SELECT * FROM adjusted_birthdays 
        WHERE next_birthday >= :dateFrom 
          AND next_birthday < :dateTo
        ORDER BY next_birthday
    """, nativeQuery = true)
    List<BirthdayEntity> findUpcomingBirthdays(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);


    List<BirthdayEntity> findAllByFullNameIgnoreCaseLikeOrTeamIgnoreCaseLike(String fullName, String team);
}
