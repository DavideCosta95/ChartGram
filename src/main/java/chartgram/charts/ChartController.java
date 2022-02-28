package chartgram.charts;

import chartgram.charts.model.Chart;
import chartgram.charts.model.ChartType;
import chartgram.persistence.entity.Message;
import chartgram.persistence.service.ServicesWrapper;
import chartgram.persistence.utils.MessageSentTimeBasedComparator;
import chartgram.telegram.model.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChartController {
	private final ChartRenderer chartRenderer;
	private final ServicesWrapper servicesWrapper;

	@Autowired
	private ChartController(ChartRenderer chartRenderer, ServicesWrapper servicesWrapper) {
		this.chartRenderer = chartRenderer;
		this.servicesWrapper = servicesWrapper;
	}

	public Chart getChart(ChartType chartType, String groupId) {
		// TODO: test
		groupId = "-1001246028586";

		JFreeChart chart = null;
		String caption = "";

		switch (chartType) {
			case MESSAGES_DISTRIBUTION_BY_TYPE:
				chart = makeMessagesDistributionByTypeChart(groupId);
				// TODO: caption
				caption = "PIE";
				break;
			case MESSAGES_WITH_RESPECT_TIME:
				// TODO: input granularità
				chart = makeMessagesWithRespectTimeChart(groupId, 24);
				// TODO: caption
				caption = "LINE";
				break;
			default:
				break;
		}
		InputStream image = chartRenderer.createPng(chart);
		return new Chart(image, caption);
	}

	private JFreeChart makeMessagesDistributionByTypeChart(String groupId) {
		List<Message> messages = servicesWrapper.getMessageService().getAllByGroupTelegramId(groupId);
		EnumMap<MessageType, Long> messageType2Count = new EnumMap<>(MessageType.class);
		for (Message message : messages) {
			MessageType currentKey = MessageType.getTypeById(message.getType());
			messageType2Count.putIfAbsent(currentKey, 0L);
			messageType2Count.put(currentKey, messageType2Count.get(currentKey) + 1);
		}
		Map<String, Long> datasetMap = messageType2Count.entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getKey().toString().toLowerCase() + ": " + e.getValue(),
						Map.Entry::getValue
				));
		DefaultPieDataset<String> dataset = createPieDataset(datasetMap);
		return ChartFactory.createPieChart("Messages distribution by type", dataset, false, true, false);
	}

	private JFreeChart makeMessagesWithRespectTimeChart(String groupId, int granularityInHours) {
		List<Message> messages = servicesWrapper.getMessageService().getAllByGroupTelegramId(groupId);
		DefaultCategoryDataset dataset = createLineDataset(messages, granularityInHours);
		return ChartFactory.createLineChart("Messages sent with respect time", "Time", "Number of messages", dataset, PlotOrientation.VERTICAL,true,true,false);
	}

	private DefaultPieDataset<String> createPieDataset(Map<String, Long> values) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		values.forEach(dataset::setValue);
		return dataset;
	}

	private DefaultCategoryDataset createLineDataset(List<Message> messages, int granularityInHours) {
		messages.sort(new MessageSentTimeBasedComparator());
		LocalDateTime earliestMessageTime = messages.get(0).getSentAt();
		LocalDateTime threshold = earliestMessageTime.plus(Duration.ofHours(granularityInHours)).minus(Duration.ofMinutes(earliestMessageTime.getMinute()));
		SortedMap<LocalDateTime, Long> time2messagesNumber = new TreeMap<>();

		for (Message message : messages) {
			LocalDateTime currentMessageTime = message.getSentAt();
			if (!currentMessageTime.isBefore(threshold)) {
				threshold = threshold.plus(Duration.ofHours(granularityInHours));
			}
			time2messagesNumber.putIfAbsent(threshold, 0L);
			time2messagesNumber.computeIfPresent(threshold, (k, v) -> v + 1);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		for (Map.Entry<LocalDateTime, Long> entry : time2messagesNumber.entrySet()) {
			// TODO
			String currentValue = "";
			if (granularityInHours == 24) {
				currentValue = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
			}
			if (granularityInHours == 1) {
				currentValue = entry.getKey().getHour() + ":" + entry.getKey().getMinute();
			}
			dataset.addValue(entry.getValue(), "messages", currentValue);
		}
		return dataset;
	}
}
