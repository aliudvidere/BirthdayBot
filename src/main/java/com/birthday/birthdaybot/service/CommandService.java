package com.birthday.birthdaybot.service;

import com.birthday.birthdaybot.constants.CallbackTypeEnum;
import com.birthday.birthdaybot.constants.LangugeEnum;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.birthday.birthdaybot.constants.MessageConstants.*;
import static java.util.Collections.emptyList;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {

    private final BotUserEntityRepository botUserEntityRepository;

    private final BotChatEntityRepository botChatEntityRepository;

    private final BirthdayEntityRepository birthdayEntityRepository;

    private final ConfigEntityRepository configEntityRepository;

    private final DateTransformer dateTransformer;

    public SendMessage register(Message message) {
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
        return new SendMessage(botChatEntity.getChatId(), getHelpMessage(botChatEntity));
    }

    public SendMessage help(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        if (botChatEntityOptional.isPresent()) {
            sendMessage.setText(getHelpMessage(botChatEntityOptional.get()));
        }
        else {
            sendMessage = startBot(chatId);
        }
        return sendMessage;
    }

    public SendMessage adminHelp(Long chatId) {
        SendMessage sendMessage;
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        if (botChatEntityOptional.isEmpty()) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage(chatId.toString(), getAdminHelpMessage(botChatEntityOptional.get()));
            sendMessage.setParseMode(HTML);
        }
        return sendMessage;
    }

    public SendMessage stopNotify(Long chatId) {
        SendMessage sendMessage;
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        if (botChatEntityOptional.isEmpty()) {
            sendMessage = startBot(chatId);
        }
        else {
            botChatEntityOptional.get().setNeedNotify(false);
            botChatEntityRepository.save(botChatEntityOptional.get());
            sendMessage = new SendMessage(chatId.toString(), STOP_NOTIFY);
        }
        return sendMessage;
    }


    public SendMessage startNotify(Long chatId) {
        SendMessage sendMessage;
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        if (botChatEntityOptional.isEmpty()) {
            sendMessage = startBot(chatId);
        }
        else {
            botChatEntityOptional.get().setNeedNotify(true);
            botChatEntityRepository.save(botChatEntityOptional.get());
            sendMessage = new SendMessage(chatId.toString(), START_NOTIFY);
        }
        return sendMessage;
    }

    public boolean checkAdmin(String username) {
        if (username.isEmpty()) {
            return false;
        }
        return botUserEntityRepository.findByUsername(username).map(t -> t.getRole().equals(RoleEnum.ADMIN)).orElse(false);
    }


    public SendMessage getNearestBirthdays(Long chatId) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage(chatId.toString(), getBirthdayMessage(Long.parseLong(configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("10"))));
            sendMessage.setParseMode(HTML);
        }
        return sendMessage;
    }

    public SendMessage getPeriod(Long chatId) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage(chatId.toString(), configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("-1"));
        }
        return sendMessage;
    }

    public SendMessage setPeriod(Long chatId, String data) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
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
            } catch (NumberFormatException e) {
                messageText = PERIOD_FORMAT_ERROR;
            } catch (Exception e) {
                messageText = e.getMessage();
            }
            sendMessage = new SendMessage(chatId.toString(), messageText);
        }
        return sendMessage;
    }

    public List<SendMessage> getPeopleList(Long chatId) {
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            return List.of(startBot(chatId));
        }
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAll();
        return getPeopleList(chatId, birthdayEntityList);
    }

    public List<SendMessage> getTodayBirthdays() {
        String messageText = getDailyMessage();
        return messageText.isEmpty() ? emptyList() : botChatEntityRepository.findByNeedNotifyTrue().stream().map(t -> {
            SendMessage sendMessage = new SendMessage(t.getChatId(), messageText);
            sendMessage.setParseMode(HTML);
            return sendMessage;
        }).toList();
    }

    public SendMessage getTodayBirthdays(Long chatId) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            String dailyMessage =  getDailyMessage();
            if (!dailyMessage.isEmpty()) {
                sendMessage = new SendMessage(chatId.toString(), dailyMessage);
                sendMessage.setParseMode(HTML);
            }
            else {
                sendMessage = new SendMessage(chatId.toString(), NO_NEAREST_BIRTHDAYS);
            }
        }
        return sendMessage;
    }

    public SendMessage getThisWeekBirthdays(Long chatId) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage(chatId.toString(), getBirthdayMessage(7L - LocalDate.now().getDayOfWeek().getValue() + 1));
            sendMessage.setParseMode(HTML);
        }
        return sendMessage;
    }

    public List<SendMessage> getPeopleList(Long chatId, String data) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAllByFullNameIgnoreCaseLikeOrTeamIgnoreCaseLike(FIND_TEMPLATE.formatted(data), FIND_TEMPLATE.formatted(data));
        return getPeopleList(chatId, birthdayEntityList);
    }

    public SendMessage deletePerson(Long chatId, String data) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAllByFullNameIgnoreCaseLikeOrTeamIgnoreCaseLike(FIND_TEMPLATE.formatted(data), FIND_TEMPLATE.formatted(data));
            if (birthdayEntityList.size() > 10) {
                sendMessage.setText(TOO_MANY_RESULTS);
            } else {
                sendMessage.setText(CHOOSE_PERSON_TO_DELETE.formatted(birthdayEntityList.stream().map(BirthdayEntity::toStringForDelete).collect(Collectors.joining(NEW_LINE))));
                sendMessage.setParseMode(HTML);
                addKeyboardForDelete(sendMessage, birthdayEntityList);
            }
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
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage();
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
        }
        return sendMessage;
    }

    public SendMessage addPersonsFromCSV(Long chatId, File file) {
        SendMessage sendMessage;
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            sendMessage = startBot(chatId);
        }
        else {
            List<List<String>> records;
            sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
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
                sendMessage.setText(ADDED_NEW_PERSONS.formatted(birthdayEntityList.size()));
            } catch (Exception e) {
                sendMessage.setText(e.getMessage());
            }
        }
        return sendMessage;
    }

    public SendDocument export(Long chatId) {
        if (!botChatEntityRepository.existsByChatId(chatId.toString())) {
            BotChatEntity botChatEntity = new BotChatEntity(chatId.toString());
            botChatEntityRepository.save(botChatEntity);
        }
        SendDocument sendDocument = new SendDocument();
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Birthdays");
        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        headerStyle.setFont(font);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Fullname");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Login");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(2);
        headerCell.setCellValue("Team");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(3);
        headerCell.setCellValue("Birthday");
        headerCell.setCellStyle(headerStyle);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setWrapText(true);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        for (int i = 0; i < birthdayEntityList.size(); i++) {
            Row row = sheet.createRow(i + 2);
            Cell cell = row.createCell(0);
            cell.setCellValue(birthdayEntityList.get(i).getFullName());
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(birthdayEntityList.get(i).getLogin());
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(birthdayEntityList.get(i).getTeam());
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue(birthdayEntityList.get(i).getBirthday());
            cell.setCellStyle(dateStyle);
        }
        sheet.autoSizeColumn(3);
        InputFile inputFile = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();

            byte[] fileBytes = outputStream.toByteArray();

            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            inputFile = new InputFile(inputStream, "birthday.xlsx");

            sendDocument.setDocument(inputFile);
        } catch (IOException e) {
            sendDocument.setCaption(e.getMessage());
        }
        if (inputFile != null) {
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(inputFile);
        }
        return sendDocument;
    }

    public SendMessage chooseLanguage(Long chatId) {
        SendMessage sendMessage;
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        if (botChatEntityOptional.isEmpty()) {
            sendMessage = startBot(chatId);
        }
        else {
            sendMessage = new SendMessage(chatId.toString(), CHOOSE_LANGUAGE);
            addKeyboardForLanguage(sendMessage);
        }
        return sendMessage;
    }

    public SendMessage chooseLanguageCallback(Long chatId, String data) {
        Optional<BotChatEntity> botChatEntityOptional = botChatEntityRepository.findByChatId(chatId.toString());
        String messageText = "";
        if (botChatEntityOptional.isPresent()) {
            switch (LangugeEnum.values()[Integer.parseInt(data.split(SEMICOLON)[1])]) {
                case RU -> {
                    messageText = LANGUAGE_CHANGED_TO_RUSSIAN;
                    botChatEntityOptional.get().setLanguage(LangugeEnum.RU);
                    botChatEntityRepository.save(botChatEntityOptional.get());
                }
                case EN -> {
                    messageText = LANGUAGE_CHANGED_TO_ENGLISH;
                    botChatEntityOptional.get().setLanguage(LangugeEnum.EN);
                    botChatEntityRepository.save(botChatEntityOptional.get());
                }
            }
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


    private void addKeyboardForLanguage(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        for (LangugeEnum langugeEnum: LangugeEnum.values()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(langugeEnum.getLanguage());
            inlineKeyboardButton.setCallbackData(CallbackTypeEnum.LANGUAGE.ordinal() + SEMICOLON + langugeEnum.ordinal());
            keyboardButtons.add(inlineKeyboardButton);
        }
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        keyboardButtons.forEach(t -> rowList.add(List.of(t)));
        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    @Deprecated
    private String getTodayMessage() {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(1));
        String messageText = birthdayEntityList.isEmpty() ? NO_BIRTHDAYS_TODAY : TODAY_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> TODAY_BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
        birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now().plusDays(1), LocalDate.now().plusDays(Long.parseLong(configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("10"))));
        messageText += birthdayEntityList.isEmpty() ? NO_NEAREST_BIRTHDAYS : NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getBirthday().getDayOfMonth(), dateTransformer.transformToRussian(t.getBirthday().getMonth().getValue()), t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
        return messageText;
    }

    private String getDailyMessage() {
        long period = Long.parseLong(configEntityRepository.findById("birthday_period").map(ConfigEntity::getValue).orElse("7"));
        List<BirthdayEntity> todayBirthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(1));
        List<BirthdayEntity> tomorrowBirthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        List<BirthdayEntity> soonPeriodBirthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now().plusDays(period), LocalDate.now().plusDays(period + 1));
        String messageText = todayBirthdayEntityList.isEmpty() ? EMPTY_STRING : TODAY_BIRTHDAYS.formatted(todayBirthdayEntityList.stream().map(t -> TODAY_BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE))) + NEW_LINE;
        messageText += tomorrowBirthdayEntityList.isEmpty() ? EMPTY_STRING : TOMORROW_BIRTHDAYS.formatted(tomorrowBirthdayEntityList.stream().map(t -> TOMORROW_BIRTHDAY_FORMAT.formatted(t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE))) + NEW_LINE;
        messageText += soonPeriodBirthdayEntityList.isEmpty() ? EMPTY_STRING : SOON_BIRTHDAYS.formatted(soonPeriodBirthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getBirthday().getDayOfMonth(), dateTransformer.transformToRussian(t.getBirthday().getMonth().getValue()), t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
        return messageText;
    }

    private String  getBirthdayMessage(Long days) {
        List<BirthdayEntity> birthdayEntityList = birthdayEntityRepository.findUpcomingBirthdays(LocalDate.now(), LocalDate.now().plusDays(days));
        return birthdayEntityList.isEmpty() ? NO_NEAREST_BIRTHDAYS : NEAREST_BIRTHDAYS.formatted(birthdayEntityList.stream().map(t -> BIRTHDAY_FORMAT.formatted(t.getBirthday().getDayOfMonth(), dateTransformer.transformToRussian(t.getBirthday().getMonth().getValue()), t.getFullName(), t.getTeam())).collect(Collectors.joining(NEW_LINE)));
    }

    private String getHelpMessage(BotChatEntity botChatEntity) {
        return switch (botChatEntity.getLanguage()) {
            case EN -> HELP_EN;
            case RU -> HELP_RU;
        };
    }

    private String getAdminHelpMessage(BotChatEntity botChatEntity) {
        return switch (botChatEntity.getLanguage()) {
            case EN -> ADMIN_HELP_EN;
            case RU -> ADMIN_HELP_RU;
        };
    }

    private SendMessage startBot(Long chatId) {
        return new SendMessage(chatId.toString(), PLEASE_USE_START_COMMAND);
    }
}
