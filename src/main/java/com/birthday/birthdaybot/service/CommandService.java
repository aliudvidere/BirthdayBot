package com.birthday.birthdaybot.service;

import com.birthday.birthdaybot.constants.CallbackTypeEnum;
import com.birthday.birthdaybot.constants.RoleEnum;
import com.birthday.birthdaybot.model.entity.BirthdayEntity;
import com.birthday.birthdaybot.model.entity.BotChatEntity;
import com.birthday.birthdaybot.model.entity.BotUserEntity;
import com.birthday.birthdaybot.model.entity.ConfigEntity;
import com.birthday.birthdaybot.repository.BirthdayEntityRepository;
import com.birthday.birthdaybot.repository.BotChatEntityRepository;
import com.birthday.birthdaybot.repository.BotUserEntityRepository;
import com.birthday.birthdaybot.repository.ConfigEntityRepository;
import com.birthday.birthdaybot.utils.DateTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.birthday.birthdaybot.constants.MessageConstants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {

    private final BotUserEntityRepository botUserEntityRepository;

    private final BotChatEntityRepository botChatEntityRepository;

    private final BirthdayEntityRepository birthdayEntityRepository;

    private final ConfigEntityRepository configEntityRepository;

    private final DateTransformer dateTransformer;

    public void register(Message message) {
        if (!message.getFrom().getUserName().isEmpty()) {
            Optional<BotUserEntity> botUserEntityOptional = botUserEntityRepository.findByUsername(message.getFrom().getUserName());
            if (botUserEntityOptional.isEmpty()) {
                BotUserEntity botUserEntity = new BotUserEntity(message.getFrom().getUserName());
                botUserEntityRepository.save(botUserEntity);
            }
        }
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(message.getChatId().toString());
        BotChatEntity botChatEntity;
        if (botChatEntityOptional.isEmpty()) {
            botChatEntity = new BotChatEntity(message.getChatId().toString());
        }
        else {
            botChatEntity = botChatEntityOptional.get();
            botChatEntity.setNeedNotify(true);
        }
        botChatEntityRepository.save(botChatEntity);
    }

    public SendMessage stopNotify(Long chatId) {
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        BotChatEntity botChatEntity;
        if (botChatEntityOptional.isEmpty()) {
            botChatEntity = BotChatEntity
                    .builder()
                    .chatId(chatId.toString())
                    .needNotify(false)
                    .build();
        }
        else {
            botChatEntity = botChatEntityOptional.get();
            botChatEntity.setNeedNotify(false);
        }
        botChatEntityRepository.save(botChatEntity);
        return new SendMessage(chatId.toString(), STOP_NOTIFY);
    }

    public SendMessage startNotify(Long chatId) {
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        BotChatEntity botChatEntity;
        if (botChatEntityOptional.isEmpty()) {
            botChatEntity = new BotChatEntity(chatId.toString());
        }
        else {
            botChatEntity = botChatEntityOptional.get();
            botChatEntity.setNeedNotify(true);
        }
        botChatEntityRepository.save(botChatEntity);
        return new SendMessage(chatId.toString(), START_NOTIFY);
    }

    public boolean checkAdmin(String username) {
        if (username.isEmpty()) {
            return false;
        }
        return botUserEntityRepository.findByUsername(username).map(t -> t.getRole().equals(RoleEnum.ADMIN)).orElse(false);
    }


    public SendMessage getNearestBirthdays(Long chatId) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(Long.parseLong(configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("10"))));
        String messageText = birthdayEntityList.isEmpty() ? NO_NEAREST_BIRTHDAYS : NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam(), t.getBirthday().getDayOfMonth(), t.getBirthday().getMonth().getValue())).collect(Collectors.joining(NEW_LINE)));
        return new SendMessage(chatId.toString(), messageText);
    }

    public SendMessage getPeriod(Long chatId) {
        return new SendMessage(chatId.toString(), configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("-1"));
    }

    public SendMessage setPeriod(Long chatId, String data) {
        String messageText;
        try {
            ConfigEntity configEntity = configEntityRepository.findById("birthday_period").orElseThrow();
            int period = Integer.parseInt(data);
            if (period <= 0) {
                throw new Exception(PERIOD_FORMAT_ERROR);
            }
            configEntity.setValue(data);
            configEntityRepository.save(configEntity);
            messageText = PERIOD_WAS_SET.formatted(period);
        }
        catch (NumberFormatException e) {
            messageText = PERIOD_FORMAT_ERROR;
        }
        catch (Exception e) {
            messageText = e.getMessage();
        }
        return new SendMessage(chatId.toString(), messageText);
    }

    public List<SendMessage> getPeopleList(Long chatId) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAll();
        return getPeopleList(chatId, birthdayEntityList);
    }

    public List<SendMessage> getTodayBirthdays() {
        String messageText = getTodayMessage();
        return botChatEntityRepository.findByNeedNotifyTrue().stream().map(t -> {
            SendMessage sendMessage = new SendMessage(t.getChatId(), messageText);
            sendMessage.setParseMode(HTML);
            return sendMessage;
        }).toList();
    }

    public List<SendMessage> getThisWeekBirthdays() {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(7));
        if (birthdayEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        String messageText = NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam(), t.getBirthday().getDayOfMonth(), t.getBirthday().getMonth().getValue())).collect(Collectors.joining(NEW_LINE)));
        return botChatEntityRepository.findByNeedNotifyTrue().stream().map(t -> new SendMessage(t.getChatId(), messageText)).toList();
    }

    public SendMessage getTodayBirthdays(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), getTodayMessage());
        sendMessage.setParseMode(HTML);
        return sendMessage;
    }

    public SendMessage getThisWeekBirthdays(Long chatId) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(7));
        String messageText = birthdayEntityList.isEmpty() ? NO_NEAREST_BIRTHDAYS: NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam(), t.getBirthday().getDayOfMonth(), t.getBirthday().getMonth().getValue())).collect(Collectors.joining(NEW_LINE)));
        return new SendMessage(chatId.toString(), messageText);
    }

    public List<SendMessage> getPeopleList(Long chatId, String data) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAllByFullNameIgnoreCaseLikeOrTeamIgnoreCaseLike(FIND_TEMPLATE.formatted(data), FIND_TEMPLATE.formatted(data));
        return getPeopleList(chatId, birthdayEntityList);
    }

    public SendMessage deletePerson(Long chatId, String data) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAllByFullNameIgnoreCaseLikeOrTeamIgnoreCaseLike(FIND_TEMPLATE.formatted(data), FIND_TEMPLATE.formatted(data));
        if (birthdayEntityList.size() > 10) {
            sendMessage.setText(TOO_MANY_RESULTS);
        }
        else {
            sendMessage.setText(CHOOSE_PERSON_TO_DELETE.formatted(birthdayEntityList.stream().map(BirthdayEntity::toStringForDelete).collect(Collectors.joining(NEW_LINE))));
            sendMessage.setParseMode(HTML);
            addKeyboardForDelete(sendMessage, birthdayEntityList);
        }
        return sendMessage;

    }

    public SendMessage deletePersonCallback(Long chatId, String data) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Optional<BirthdayEntity> birthdayEntityOptional = birthdayEntityRepository.findById(Integer.parseInt(data.split(SEMICOLON)[1]));
        if (birthdayEntityOptional.isPresent()) {
            sendMessage.setText(PERSON_WAS_DELETED.formatted(birthdayEntityOptional.get().toString()));
            birthdayEntityRepository.delete(birthdayEntityOptional.get());
        }
        else {
            sendMessage.setText(PERSON_WAS_NOT_DELETED);
        }
        return sendMessage;

    }

    public SendMessage addPerson(Long chatId, String data) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String[] fields = data.split(COMMA);
        try {
            BirthdayEntity birthdayEntity = BirthdayEntity
                    .builder()
                    .fullName(fields[0].trim())
                    .login(fields[1].trim())
                    .team(fields[2].trim())
                    .birthday(LocalDate.parse(fields[3].trim()))
                    .build();
            birthdayEntityRepository.save(birthdayEntity);
            sendMessage.setText(PERSON_WAS_CREATED.formatted(birthdayEntity.toString()));
        } catch (Exception e) {
            sendMessage.setText(e.getMessage());
        }
        return sendMessage;
    }

    public SendMessage addPersonsFromCSV(Long chatId, File file) {
        List<List<String>> records = List.of();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String messageText;
        try {
            records = Files.readAllLines(file.toPath())
                    .stream()
                    .map(line -> Arrays.asList(line.split(COMMA)))
                    .toList();
            List<BirthdayEntity> birthdayEntityList = records.stream().map(t -> BirthdayEntity
                    .builder()
                    .fullName(t.get(0).trim())
                    .login(t.get(1).trim())
                    .team(t.get(2).trim())
                    .birthday(LocalDate.parse(t.get(3).trim()))
                    .build())
                    .toList();
            birthdayEntityRepository.saveAll(birthdayEntityList);
            messageText = ADDED_NEW_PERSONS.formatted(birthdayEntityList.size());
        } catch (Exception e) {
            messageText = e.getMessage();
        }
        return new SendMessage(chatId.toString(), messageText);
    }

    private List<SendMessage> getPeopleList(Long chatId, List<BirthdayEntity> birthdayEntityList) {
        List<SendMessage> messageList = new ArrayList<>();
        int i = 0;
        while (i < birthdayEntityList.size()) {
            messageList.add(new SendMessage(chatId.toString(),birthdayEntityList.subList(i, Math.min(i + 10, birthdayEntityList.size())).stream().map(BirthdayEntity::toString).collect(Collectors.joining(NEW_LINE))));
            i += 10;
        }
        return messageList;
    }

    private void addKeyboardForDelete(SendMessage sendMessage, List<BirthdayEntity> birthdayEntityList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        for (BirthdayEntity birthdayEntity: birthdayEntityList) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(birthdayEntity.getId().toString());
            inlineKeyboardButton.setCallbackData(CallbackTypeEnum.DELETE.ordinal() + SEMICOLON +birthdayEntity.getId());
            keyboardButtons.add(inlineKeyboardButton);
        }
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        keyboardButtons.forEach(t -> rowList.add(List.of(t)));
        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    private String getTodayMessage() {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(1));
        String messageText = birthdayEntityList.isEmpty() ? NO_BIRTHDAYS_TODAY: TODAY_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> TODAY_BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
        birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now().plusDays(1), LocalDate.now().plusDays(Long.parseLong(configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("10"))));
        messageText += birthdayEntityList.isEmpty() ? NO_NEAREST_BIRTHDAYS : NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getBirthday().getDayOfMonth(), dateTransformer.transformToRussian(t.getBirthday().getMonth().getValue()), t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
        return messageText;
    }
}
