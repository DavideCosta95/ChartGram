package chartgram.config.spring;

import chartgram.exceptions.BotStartupException;
import chartgram.telegram.ITelegramBot;
import chartgram.telegram.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramBotConfiguration {
	@Bean
	public ITelegramBot bot(chartgram.config.Configuration configuration) throws BotStartupException {
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
			log.info("Telegram bot disabled: using bot mock");
			return TelegramBot.Null;
		}
	}
}
