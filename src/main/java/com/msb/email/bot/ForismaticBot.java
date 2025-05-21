package com.msb.email.bot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.msb.email.model.ForismaticResponse;

import jakarta.annotation.PostConstruct;

@Component
public class ForismaticBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForismaticBot.class);

	private static final String BOT_NAME = "zz6unp_bot";
	private static final String BOT_TOKEN = "7767428585:AAFmyfdSazKXBKTD3VnPqv6iK8TFGalmfgQ";

	private static final String FORISMATIC_API_URL = "https://api.forismatic.com/api/1.0/";
	private static final String FORISMATIC_API_METHOD = "?method=getQuote&format=json&lang=en";

	@Override
	public void onUpdateReceived(Update update) {
		LOGGER.info("Received message: {}", update);
		if (update.hasMessage() && update.getMessage().hasText()) {
			String chatId = update.getMessage().getChatId().toString();
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			String text = update.getMessage().getText();
			if ("/quote".equals(text)) {
				try {
					String quote = getQuote();
					message.setText(quote);
				} catch (IOException e) {
					LOGGER.error("Error getting quote", e);
					message.setText("Sorry, I cannot get a quote for you now.");
				}
			} else {
				message.setText("I'm sorry, I don't understand that command.");
			}
			try {
				execute(message);
			} catch (TelegramApiException e) {
				LOGGER.error("Error sending message", e);
			}
		}
	}

	private void sendMessage(String chatId, String messageText) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText(messageText);

		try {
			execute(message);
			LOGGER.info("Sent welcome message to chat: {}", chatId);
		} catch (TelegramApiException e) {
			LOGGER.error("Error sending welcome message", e);
		}
	}

	private String getQuote() throws IOException {
		String url = FORISMATIC_API_URL + FORISMATIC_API_METHOD;
		try {
			RestTemplate restTemplate = new RestTemplate();
			ForismaticResponse response = restTemplate.getForObject(url, ForismaticResponse.class);
			if (response != null) {
				return "\"" + response.getQuoteText() + "\" - "
						+ (response.getQuoteAuthor() != null ? response.getQuoteAuthor() : "Unknown");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Not found quote";
	}

	@Override
	public String getBotUsername() {
		return BOT_NAME;
	}

	@Override
	public String getBotToken() {
		return BOT_TOKEN;
	}

	@PostConstruct
	public void start() {
		LOGGER.info("ForismaticBot started");
		String chatId = "7258773752"; // Thay thế bằng chat ID của bạn
		String welcomeMessage = "Hello! The bot has started and is ready to provide quotes. Type /quote to get a quote.";

		sendMessage(chatId, welcomeMessage);

		LOGGER.info("ForismaticBot send successfully");
	}

}
