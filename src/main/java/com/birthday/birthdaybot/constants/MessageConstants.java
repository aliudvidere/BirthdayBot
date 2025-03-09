package com.schedule.scheduledtaskbot.constants;

public interface MessageConstants {

    String HELP = """
            /help - commands information
            
            /register - to work with bot you should be registered, just send command /register
            
            /activity_dates - get interested activity dates""";

    String COMMAND_FORMAT = "Command should start with \"/\"";

    String WHITE_SPACE = " ";

    String EMPTY_STRING = "";

    String SLASH = "/";

    String SEMICOLON = ";";

    String YOU_HAVE_ALREADY_REGISTERED = "You have already registered as %s";

    String YOU_HAVE_SUCCESSFULLY_REGISTERED = "You have successfully registered as %s";
}

