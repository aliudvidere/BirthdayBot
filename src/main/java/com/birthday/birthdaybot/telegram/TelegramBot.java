package com.schedule.scheduledtaskbot.telegram;

import com.schedule.scheduledtaskbot.config.BotProperties;
import com.schedule.scheduledtaskbot.service.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static com.schedule.scheduledtaskbot.constants.CommandConstants.*;
import static com.schedule.scheduledtaskbot.constants.MessageConstants.*;


@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;

    private final CommandService commandService;

    List<BotCommand> commands = Arrays.asList(
            new BotCommand(START_COMMAND, START_COMMAND_DESCRIPTION),
            new BotCommand(REGISTER_COMMAND, REGISTER_COMMAND_DESCRIPTION),
            new BotCommand(ACTIVITY_DATES_COMMAND, ACTIVITY_DATES_COMMAND_DESCRIPTION),
            new BotCommand(TASKS_COMMAND, TASKS_COMMAND_DESCRIPTION)
    );

    public TelegramBot(BotProperties botProperties, CommandService commandService) throws TelegramApiException {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.commandService = commandService;
        execute(new SetMyCommands(commands, null, null));
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String[] parts = update.getMessage().getText().split(WHITE_SPACE, 2);
            String command = parts[0];
            String data = parts.length > 1 ? parts[1] : EMPTY_STRING;
            if (command.startsWith(SLASH)) {
                command = command.substring(1);
                switch (command) {
                    case START_COMMAND, HELP_COMMAND ->
                            sendMessage(new SendMessage(update.getMessage().getChatId().toString(), HELP));
                    case REGISTER_COMMAND ->
                        sendMessage(commandService.register(update.getMessage()));
                    case ACTIVITY_DATES_COMMAND ->
                        sendMessage(commandService.sendLunaMessage(update.getMessage().getChatId().toString()));
                    case TASKS_COMMAND ->
                        sendMessage(commandService.getPeriodicTasks(update.getMessage().getChatId().toString()));
                }
            }
            else {
                sendMessage(new SendMessage(update.getMessage().getChatId().toString(), COMMAND_FORMAT));
            }
        }
    }


    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
