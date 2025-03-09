package com.schedule.scheduledtaskbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedule.scheduledtaskbot.client.LunaClient;
import com.schedule.scheduledtaskbot.model.entity.BotUserEntity;
import com.schedule.scheduledtaskbot.model.entity.PeriodicTaskEntity;
import com.schedule.scheduledtaskbot.repository.BotUserEntityRepository;
import com.schedule.scheduledtaskbot.repository.PeriodicTaskEntityRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.schedule.scheduledtaskbot.constants.MessageConstants.YOU_HAVE_ALREADY_REGISTERED;
import static com.schedule.scheduledtaskbot.constants.MessageConstants.YOU_HAVE_SUCCESSFULLY_REGISTERED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {

    private final BotUserEntityRepository botUserEntityRepository;

    private final LunaClient lunaClient;

    private final PeriodicTaskEntityRepository periodicTaskEntityRepository;

    @Value("${app.lunaToken}")
    private String lunaToken;

    public SendMessage register(Message registerMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(registerMessage.getChatId());
        Optional<BotUserEntity> botUserEntityOptional = botUserEntityRepository.findByTgCode(registerMessage.getChatId().toString());
        if (botUserEntityOptional.isPresent()) {
            String lastName = registerMessage.getFrom().getLastName();
            String firstName = registerMessage.getFrom().getFirstName();
            BotUserEntity botUserEntity = botUserEntityOptional.get();
            botUserEntity.setUsername(lastName != null ? lastName + " " + firstName : firstName);
            botUserEntityRepository.save(botUserEntity);
            message.setText(YOU_HAVE_ALREADY_REGISTERED.formatted(botUserEntity.getUsername()));
        }
        else {
            BotUserEntity botUserEntity = new BotUserEntity();
            String lastName = registerMessage.getFrom().getLastName();
            String firstName = registerMessage.getFrom().getFirstName();
            botUserEntity.setUsername(lastName != null ? lastName + " " + firstName : firstName);
            botUserEntity.setTgCode(registerMessage.getChatId().toString());
            botUserEntityRepository.save(botUserEntity);
            message.setText(YOU_HAVE_SUCCESSFULLY_REGISTERED.formatted(botUserEntity.getUsername()));
        }
        return message;
    }

    public SendMessage sendLunaMessage(String chatId) {
        String messageText;
        String lunaStaffResponse;
        String lunaActivityResponse;
        try {
            lunaStaffResponse = lunaClient.getStaff(lunaToken);
            lunaActivityResponse = lunaClient.getActivity(lunaToken);
        }
        catch (FeignException feignClientException) {
            log.error(feignClientException.getMessage());
            return new SendMessage(chatId, feignClientException.getLocalizedMessage());
        }
        Map<String, Object> activities;
        try {
            activities = new ObjectMapper().readValue(lunaActivityResponse, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String staffNeededId = getNeededStaffId(lunaStaffResponse);
        if (staffNeededId != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) activities.get("data");
            data = data.stream().filter(t -> t.get("staff_id").toString().equals(staffNeededId)).toList();
            if (data.isEmpty()) {
                messageText = "No interested activities found";
            }
            else {
                messageText = data.stream().map(t -> t.get("date") + "--" + t.get("records_count") + "/" + t.get("capacity") + "\n").collect(Collectors.joining("\n"));
            }
        }
        else {
            messageText = "No interested instructors found";
        }
        return new SendMessage(chatId, messageText);
    }

    public List<SendMessage> sendLunaMessage() {
        String messageText;
        String lunaStaffResponse;
        String lunaActivityResponse;
        List<BotUserEntity> botUserEntityList = botUserEntityRepository.findAll();
        botUserEntityList.forEach(botUserEntity -> {
            botUserEntity.setLastNotify(LocalDateTime.now());
            botUserEntityRepository.save(botUserEntity);
        });
        try {
            lunaStaffResponse = lunaClient.getStaff(lunaToken);
            lunaActivityResponse = lunaClient.getActivity(lunaToken);
        }
        catch (FeignException feignClientException) {
            log.error(feignClientException.getMessage());
            return botUserEntityList.stream().map(t -> new SendMessage(t.getTgCode(), feignClientException.getLocalizedMessage())).toList();
        }
        Map<String, Object> activities;
        try {
            activities = new ObjectMapper().readValue(lunaActivityResponse, HashMap.class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String staffNeededId = getNeededStaffId(lunaStaffResponse);
        if (staffNeededId != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) activities.get("data");
            data = data.stream().filter(t -> t.get("staff_id").toString().equals(staffNeededId) && Integer.parseInt(t.get("capacity").toString()) - Integer.parseInt(t.get("records_count").toString()) > 0).toList();
            if (data.isEmpty()) {
                return new ArrayList<>();
            }
            else {
                messageText = data.stream().map(t -> t.get("date") + ":" + t.get("capacity") + ":" + t.get("records_count") + "\n").collect(Collectors.joining("\n"));
            }
        }
        else {
            return new ArrayList<>();
        }
        return botUserEntityList.stream().map(t -> new SendMessage(t.getTgCode(), messageText)).toList();
    }

    public SendMessage getPeriodicTasks(String chatId) {
        return new SendMessage(chatId, periodicTaskEntityRepository.findAll().stream().map(PeriodicTaskEntity::toString).collect(Collectors.joining("\n")));
    }

    private String getNeededStaffId(String lunaStaffResponse) {
        List<Object> staff;
        try {
            staff = new ObjectMapper().readValue(lunaStaffResponse, ArrayList.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return staff.stream().filter(t -> ((Map<String, Object>)t).get("name").toString().equals("Ольга Кремень")).findFirst().map(t -> ((Map<String, Object>)t).get("id").toString()).orElse(null);
    }
}
