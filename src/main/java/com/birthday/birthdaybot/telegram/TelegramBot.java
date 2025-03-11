package com.birthday.birthdaybot.telegram;


import com.birthday.birthdaybot.config.BotProperties;
import com.birthday.birthdaybot.constants.CallbackTypeEnum;
import com.birthday.birthdaybot.service.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static com.birthday.birthdaybot.constants.CommandConstants.*;
import static com.birthday.birthdaybot.constants.MessageConstants.*;



@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;

    private final CommandService commandService;

    List<BotCommand> commands = Arrays.asList(
            new BotCommand(START_COMMAND, START_COMMAND_DESCRIPTION),
            new BotCommand(NEAREST_BIRTHDAYS_COMMAND, NEAREST_BIRTHDAYS_COMMAND_DESCRIPTION),
            new BotCommand(THIS_WEEK_BIRTHDAYS_COMMAND, THIS_WEEK_BIRTHDAYS_COMMAND_DESCRIPTION),
            new BotCommand(TODAY_BIRTHDAYS_COMMAND, TODAY_BIRTHDAYS_COMMAND_DESCRIPTION)
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
                    case START_COMMAND -> {
                        commandService.register(update.getMessage());
                        sendMessage(new SendMessage(update.getMessage().getChatId().toString(), HELP));
                    }
                    case HELP_COMMAND -> {
                        sendMessage(new SendMessage(update.getMessage().getChatId().toString(), HELP));
                    }
                    case NEAREST_BIRTHDAYS_COMMAND -> {
                        sendMessage(commandService.getNearestBirthdays(update.getMessage().getChatId()));
                    }
                    case THIS_WEEK_BIRTHDAYS_COMMAND -> {
                        sendMessage(commandService.getThisWeekBirthdays(update.getMessage().getChatId()));
                    }
                    case TODAY_BIRTHDAYS_COMMAND -> {
                        sendMessage(commandService.getTodayBirthdays(update.getMessage().getChatId()));
                    }
                    case ADMIN_HELP_COMMAND -> {
                        SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), ADMIN_HELP);
                        sendMessage.setParseMode(HTML);
                        sendAdminMessage(update.getMessage().getFrom().getUserName(), sendMessage);
                    }
                    case PERIOD_COMMAND -> {
                        if (data.isEmpty()) {
                            sendAdminMessage(update.getMessage().getFrom().getUserName(), commandService.getPeriod(update.getMessage().getChatId()));
                        }
                        else {
                            sendAdminMessage(update.getMessage().getFrom().getUserName(), commandService.setPeriod(update.getMessage().getChatId(), data));
                        }
                    }
                    case PEOPLE_LIST_COMMAND -> {
                        sendAdminMessages(update.getMessage(), commandService.getPeopleList(update.getMessage().getChatId()));
                    }
                    case FIND_COMMAND -> {
                        sendAdminMessages(update.getMessage(), commandService.getPeopleList(update.getMessage().getChatId(), data));
                    }
                    case STOP_NOTIFY_COMMAND -> {
                        sendAdminMessage(update.getMessage().getFrom().getUserName(), commandService.stopNotify(update.getMessage().getChatId()));
                    }
                    case START_NOTIFY_COMMAND -> {
                        sendAdminMessage(update.getMessage().getFrom().getUserName(), commandService.startNotify(update.getMessage().getChatId()));
                    }
                    case DELETE_COMMAND -> {
                        sendAdminMessage(update.getMessage().getFrom().getUserName(), commandService.deletePerson(update.getMessage().getChatId(), data));
                    }
                }
            }
            else {
                sendMessage(new SendMessage(update.getMessage().getChatId().toString(), COMMAND_FORMAT));
            }
        } else if (update.hasCallbackQuery()) {
            DeleteMessage deleteMessage = new DeleteMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId());
            deleteMessage(deleteMessage);
            switch (CallbackTypeEnum.values()[Integer.parseInt(update.getCallbackQuery().getData().split(SEMICOLON)[0])]) {
                case CallbackTypeEnum.DELETE -> {
                    sendAdminMessage(update.getCallbackQuery().getFrom().getUserName(), commandService.deletePersonCallback(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getData()));
                }
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

    public void sendMessages(List <SendMessage> sendMessageList) {
        sendMessageList.forEach(this::sendMessage);
    }

    private void sendAdminMessage(String username, SendMessage sendMessage) {
        if (commandService.checkAdmin(username)) {
            sendMessage(sendMessage);
        }
    }

    private void sendAdminMessages(Message message, List <SendMessage> sendMessageList) {
        sendMessageList.forEach(t -> sendAdminMessage(message.getFrom().getUserName(), t));
    }

    private void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
