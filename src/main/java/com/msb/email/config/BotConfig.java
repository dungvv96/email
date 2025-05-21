package com.msb.email.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.msb.email.bot.ForismaticBot;

@Configuration
public class BotConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotConfig.class);

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            return new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            LOGGER.error("Error creating TelegramBotsApi", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ForismaticBot forismaticBot(TelegramBotsApi telegramBotsApi) {
        ForismaticBot bot = new ForismaticBot();
        try {
            telegramBotsApi.registerBot(bot);
            LOGGER.info("Bot registered successfully.");
        } catch (TelegramApiException e) {
            LOGGER.error("Error registering bot", e);
        }
        return bot;
    }
}
