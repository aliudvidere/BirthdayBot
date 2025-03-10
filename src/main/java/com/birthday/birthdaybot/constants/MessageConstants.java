package com.birthday.birthdaybot.constants;

public interface MessageConstants {

    String HELP = """
            /help - commands information
            """;
    String ADMIN_HELP = HELP + """
            /period - to get period send /period, to set period send /period days, fore example /period 10 will find people with birthday in next 10 days
            """;
    String COMMAND_FORMAT = "Command should start with \"/\"";

    String WHITE_SPACE = " ";

    String EMPTY_STRING = "";

    String SLASH = "/";

    String SEMICOLON = ";";

    String NEAREST_BIRTHDAYS = " \uD83D\uDCC5 Ближайшие дни рождения:\n%s";

    String TODAY_BIRTHDAYS = "Сегодня день рождения отмечают %s,\n поздравляем!";

    String NO_NEAREST_BIRTHDAYS = "В заданный период нет дней рождений";

    String BIRTHDAY_FORMAT = "%s - %s - (День рождения: %s-%s)";

    String NEW_LINE = "\n";

    String PERIOD_FORMAT_ERROR = "Period value should be number more than 0";

    String PERIOD_WAS_SET = "Period was set to %d days";

    String FIND_TEMPLATE = "%%%s%%";

    String STOP_NOTIFY = "Notification about birthdays were stopped in this chat";

    String START_NOTIFY = "Notification about birthdays were started in this chat";
}

