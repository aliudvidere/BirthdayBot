package com.birthday.birthdaybot.scheduler;

import com.birthday.birthdaybot.service.CommandService;
import com.birthday.birthdaybot.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifyScheduler {

    private final TelegramBot telegramBot;

    private final CommandService commandService;

    @Scheduled(cron = "0 0 9 * * *")
    public void todayBirthdays() {
        telegramBot.sendMessages(commandService.getTodayBirthdays());
        telegramBot.sendMessages(commandService.getTodayBirthdaysAdmin());
    }
}
