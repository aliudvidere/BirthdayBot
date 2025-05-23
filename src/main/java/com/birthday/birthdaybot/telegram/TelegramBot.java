package com.birthday.birthdaybot.telegram;


import com.birthday.birthdaybot.config.BotProperties;
import com.birthday.birthdaybot.constants.CallbackTypeEnum;
import com.birthday.birthdaybot.service.CommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
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
            new BotCommand(LANGUAGE_COMMAND, LANGUAGE_COMMAND_DESCRIPTION),
            new BotCommand(STOP_COMMAND, STOP_COMMAND_DESCRIPTION),
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
                        sendMessage(commandService.register(update.getMessage()));
                    }
                    case LANGUAGE_COMMAND -> {
                        sendMessage(commandService.chooseLanguage(update.getMessage().getChatId()));
                    }
                    case STOP_COMMAND -> {
                        sendMessage(commandService.stopNotify(update.getMessage().getChatId()));
                    }
                    case HELP_COMMAND -> {
                        sendMessage(commandService.help(update.getMessage().getChatId()));
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
                    case TODAY_BIRTHDAYS_ADMIN_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.getTodayBirthdaysAmin(update.getMessage().getChatId()));
                        }
                    }
                    case ADMIN_HELP_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.adminHelp(update.getMessage().getChatId()));
                        }
                    }
                    case PERIOD_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            if (data.isEmpty()) {
                                sendMessage(commandService.getPeriod(update.getMessage().getChatId()));
                            } else {
                                sendMessage(commandService.setPeriod(update.getMessage().getChatId(), data));
                            }
                        }
                    }
                    case PEOPLE_LIST_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessages(commandService.getPeopleList(update.getMessage().getChatId()));
                        }
                    }
                    case FIND_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessages(commandService.getPeopleList(update.getMessage().getChatId(), data));
                        }
                    }
                    case STOP_NOTIFY_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.stopNotify(update.getMessage().getChatId()));
                        }
                    }
                    case START_NOTIFY_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.startNotify(update.getMessage().getChatId()));
                        }
                    }
                    case DELETE_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.deletePerson(update.getMessage().getChatId(), data));
                        }
                    }
                    case ADD_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.addPerson(update.getMessage().getChatId(), data));
                        }
                    }
                    case EXPORT_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendDocument(commandService.export(update.getMessage().getChatId()));
                        }
                    }
                    case STOP_ADMIN_NOTIFY_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.stopAdminNotify(update.getMessage().getChatId()));
                        }
                    }
                    case START_ADMIN_NOTIFY_COMMAND -> {
                        if (commandService.checkAdmin(update.getMessage().getFrom().getUserName())) {
                            sendMessage(commandService.startAdminNotify(update.getMessage().getChatId()));
                        }
                    }
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasDocument()) {
            switch (update.getMessage().getCaption()) {
                case UPLOAD_CAPTION -> {
                    if (commandService.checkAdmin(update.getMessage().getFrom().getUserName()) && update.getMessage().hasDocument()) {
                        File file = getFile(update.getMessage().getDocument().getFileId());
                        sendMessage(commandService.addPersonsFromCSV(update.getMessage().getChatId(), file));
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            DeleteMessage deleteMessage = new DeleteMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId());
            deleteMessage(deleteMessage);
            switch (CallbackTypeEnum.values()[Integer.parseInt(update.getCallbackQuery().getData().split(SEMICOLON)[0])]) {
                case CallbackTypeEnum.DELETE -> {
                    if (commandService.checkAdmin(update.getCallbackQuery().getFrom().getUserName())) {
                        sendMessage(commandService.deletePersonCallback(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getData()));
                    }
                }
                case CallbackTypeEnum.LANGUAGE -> {
                    sendMessage(commandService.chooseLanguageCallback(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getData()));
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

    public void sendDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessages(List <SendMessage> sendMessageList) {
        sendMessageList.forEach(this::sendMessage);
    }

    private void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private File getFile(String fileId) {
        File file = null;
        try {
            GetFile getFile = new GetFile(fileId);
            String filePath = execute(getFile).getFilePath();
            file = downloadFile(filePath);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return file;
    }
}
