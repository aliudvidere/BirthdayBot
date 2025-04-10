package com.birthday.birthdaybot.constants;

public interface MessageConstants {

    String HELP_EN = """
            /help - commands information
            /nearest_birthdays - nearest birthdays information
            /this_week_birthdays - this week birthdays information
            /today_birthdays - today birthdays information
            """;

    String HELP_RU = """
            /help - commands information
            /nearest_birthdays - nearest birthdays information
            /this_week_birthdays - this week birthdays information
            /today_birthdays - today birthdays information
            """;

    String ADMIN_HELP_EN = HELP_EN + """
            /period - to get period send /period, to set period send /period days, for example <b>/period 10</b> will find people with birthday in next 10 days;
            /people_list - get all people information;
            /find - to find person send /find name or team, for example <b>/find nttm</b> return all people with nttm in name or team;
            /stop_notify - stop birthday notification in this chat;
            /start_notify - start birthday notification in this chat;
            /delete - to delete person send /delete name or team, for example <b>/delete test</b> return buttons with all people with test in name or team (limit - 10 buttons);
            /add - to add person send /add name, login, team, birthday in format YYYY-MM-DD format, for example <b>/add test, login, team, 1999-01-01</b>;
            /upload - to import CSV file use this structure: name,login,team, birthday in format YYYY-MM-DD format, for example <b>/test, login, team, 1999-01-01</b>;
            /export - to export the birthdays table in excel format.
            """;

    String ADMIN_HELP_RU = HELP_RU + """
            /period - to get period send /period, to set period send /period days, for example <b>/period 10</b> will find people with birthday in next 10 days;
            /people_list - get all people information;
            /find - to find person send /find name or team, for example <b>/find nttm</b> return all people with nttm in name or team;
            /stop_notify - stop birthday notification in this chat;
            /start_notify - start birthday notification in this chat;
            /delete - to delete person send /delete name or team, for example <b>/delete test</b> return buttons with all people with test in name or team (limit - 10 buttons);
            /add - to add person send /add name, login, team, birthday in format YYYY-MM-DD format, for example <b>/add test, login, team, 1999-01-01</b>;
            /upload - to import CSV file use this structure: name,login,team, birthday in format YYYY-MM-DD format, for example <b>/test, login, team, 1999-01-01</b>;
            /export - to export the birthdays table in excel format;
            /today_birthdays_admin - get today birthdays extra information;
            /stop_admin_notify - stop birthday admin notification in this chat;
            /start_admin_notify - start birthday admin notification in this chat.
            """;
    String COMMAND_FORMAT = "Command should start with \"/\"";

    String WHITE_SPACE = " ";

    String EMPTY_STRING = "";

    String SLASH = "/";

    String SEMICOLON = ";";

    String COMMA = ",";

    String NEAREST_BIRTHDAYS = "\uD83D\uDCC5 Ближайшие дни рождения \uD83C\uDF89\n%s";

    String SOON_BIRTHDAYS = "\uD83D\uDCC5 Скоро день рождения \uD83C\uDF89\n%s";

    String TODAY_BIRTHDAYS = "\uD83C\uDF89 Сегодня день рождения! \uD83C\uDF82\uD83C\uDF88\n%s\n";

    String TOMORROW_BIRTHDAYS = "\uD83C\uDF89 Завтра день рождения! \uD83C\uDF82\uD83C\uDF88\n%s\n";

    String NO_NEAREST_BIRTHDAYS = "В заданный период нет дней рождений";

    String BIRTHDAY_FORMAT = "\uD83C\uDF82 <b>%s %s</b> — %s, <i>%s</i>";

    String NEW_LINE = "\n";

    String PERIOD_FORMAT_ERROR = "Period value should be number more than 0";

    String PERIOD_WAS_SET = "Period was set to %d days";

    String FIND_TEMPLATE = "%%%s%%";

    String STOP_NOTIFY = "Notifications about birthdays were stopped in this chat";

    String START_NOTIFY = "Notifications about birthdays were started in this chat";

    String STOP_ADMIN_NOTIFY = "Admin notifications about birthdays were stopped in this chat";

    String START_ADMIN_NOTIFY = "Admin notifications about birthdays were started in this chat";

    String TOO_MANY_RESULTS = "Too many results";

    String CHOOSE_PERSON_TO_DELETE = "Choose person to delete: \n%s";

    String HTML = "html";

    String PERSON_WAS_DELETED = "%s was deleted";

    String PERSON_WAS_NOT_DELETED = "Person was not deleted due to some error";

    String PERSON_WAS_CREATED = "%s was created";

    String ADDED_NEW_PERSONS = "Added %s new persons";

    String NO_BIRTHDAYS_TODAY = "\uD83D\uDCC5 Сегодня нет именинников\n";

    String TODAY_BIRTHDAY_FORMAT =  "\uD83E\uDD73 %s, %s";

    String TOMORROW_BIRTHDAY_FORMAT =  "\uD83E\uDD73 %s, %s";

    String CHOOSE_LANGUAGE = "Choose language";

    String LANGUAGE_CHANGED_TO_RUSSIAN = "Язык изменён на русский";

    String LANGUAGE_CHANGED_TO_ENGLISH = "Language changed to English";

    String PLEASE_USE_START_COMMAND = "Please use /start command";
}

