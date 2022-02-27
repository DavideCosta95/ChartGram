package chartgram.config.spring;

import chartgram.charts.ChartRenderer;
import chartgram.config.Localization;
import chartgram.exceptions.BotStartupException;
import chartgram.persistence.service.*;
import chartgram.telegram.model.ITelegramBot;
import chartgram.telegram.TelegramBot;
import chartgram.telegram.TelegramController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramConfiguration {
	@Bean
	public ITelegramBot telegramBot(chartgram.config.Configuration configuration) throws BotStartupException {
		boolean botEnabled = configuration.getBotConfiguration().getEnabled();
		if (botEnabled) {
			try {
				TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
				TelegramBot telegramBot = new TelegramBot(configuration);
				botsApi.registerBot(telegramBot);
				log.info("Telegram bot started");
				return telegramBot;
			} catch (TelegramApiException e) {
				throw new BotStartupException(e);
			}
		} else {
			log.warn("Telegram bot disabled: using bot mock");
			return TelegramBot.Null;
		}
	}

	@Bean
	public TelegramController telegramController(chartgram.config.Configuration configuration, ChartRenderer chartRenderer, ITelegramBot bot, Localization localization, ServicesWrapper servicesWrapper) {
		boolean botEnabled = configuration.getBotConfiguration().getEnabled();
		TelegramController telegramController = new TelegramController(configuration, chartRenderer, bot, localization, servicesWrapper);
		if (botEnabled) {
			telegramController.startup();
		}
		return telegramController;
	}
}
