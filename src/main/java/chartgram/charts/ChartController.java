package chartgram.charts;

import chartgram.charts.model.Chart;
import chartgram.charts.model.ChartType;
import chartgram.model.Pair;
import chartgram.persistence.entity.JoinEvent;
import chartgram.persistence.entity.LeaveEvent;
import chartgram.persistence.entity.Message;
import chartgram.persistence.entity.TemporalEvent;
import chartgram.persistence.service.ServicesWrapper;
import chartgram.persistence.utils.TemporalEventComparator;
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
		JFreeChart chart = null;
		String caption = "";

		switch (chartType) {
			case MESSAGES_DISTRIBUTION_BY_TYPE:
				chart = makeMessagesDistributionByTypeChart(groupId);
				// TODO: aggiungere nome gruppo
				caption = "Messages distribution by type.";
				break;
			case MESSAGES_WITH_RESPECT_TIME:
				// TODO: input granularità
				chart = makeMessagesWithRespectTimeChart(groupId, 24);
				caption = "Messages with respect time.";
				break;
			case JOINS_DISTRIBUTION_WITH_RESPECT_TIME:
				chart = makeJoinsWithRespectTimeChart(groupId, 24);
				caption = "Joins distribution with respect time.";
				break;
			case LEAVINGS_DISTRIBUTION_WITH_RESPECT_TIME:
				chart = makeLeavingsWithRespectTimeChart(groupId, 24);
				caption = "Leavings distribution with respect time.";
				break;
			case JOINS_VS_LIVINGS:
				chart = makeJoinsVsLeavingsWithRespectTimeChart(groupId, 24);
				caption = "Joins vs leavings with respect time.";
				break;
			default:
				break;
		}
		// TODO: vedere caratteristiche estetiche del grafico
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
		DefaultCategoryDataset dataset = createLineDataset(messages, granularityInHours, "messages");
		return ChartFactory.createLineChart("Messages sent with respect time", "Time", "Number of messages", dataset, PlotOrientation.VERTICAL, true, true, false);
	}

	private JFreeChart makeJoinsWithRespectTimeChart(String groupId, int granularityInHours) {
		List<JoinEvent> joins = servicesWrapper.getJoinEventService().getAllByGroupTelegramId(groupId);
		DefaultCategoryDataset dataset = createLineDataset(joins, granularityInHours, "joins");
		return ChartFactory.createLineChart("Group joins with respect time", "Time", "Number of joins", dataset, PlotOrientation.VERTICAL, true, true, false);
	}

	private JFreeChart makeLeavingsWithRespectTimeChart(String groupId, int granularityInHours) {
		List<LeaveEvent> leavings = servicesWrapper.getLeaveEventService().getAllByGroupTelegramId(groupId);
		DefaultCategoryDataset dataset = createLineDataset(leavings, granularityInHours, "leavings");
		return ChartFactory.createLineChart("Groups leavings with respect time", "Time", "Number of leavings", dataset, PlotOrientation.VERTICAL, true, true, false);
	}

	private JFreeChart makeJoinsVsLeavingsWithRespectTimeChart(String groupId, int granularityInHours) {
		List<LeaveEvent> leavings = servicesWrapper.getLeaveEventService().getAllByGroupTelegramId(groupId);
		List<JoinEvent> joins = servicesWrapper.getJoinEventService().getAllByGroupTelegramId(groupId);
		DefaultCategoryDataset dataset = createMultilineDataset(List.of(new Pair<>(leavings, "leavings"), new Pair<>(joins, "joins")), granularityInHours);
		return ChartFactory.createLineChart("Groups leavings with respect time", "Time", "Number of leavings", dataset, PlotOrientation.VERTICAL, true, true, false);
	}

	private DefaultPieDataset<String> createPieDataset(Map<String, Long> values) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		values.forEach(dataset::setValue);
		return dataset;
	}

	private DefaultCategoryDataset createLineDataset(List<? extends TemporalEvent> events, int granularityInHours, String eventNameInLegend) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		if (events.isEmpty()) {
			dataset.addValue(100, eventNameInLegend, "");
			return dataset;
		}

		SortedMap<LocalDateTime, Long> time2eventsNumber = getDatasetByTemporalEvents(events, granularityInHours);
		for (Map.Entry<LocalDateTime, Long> entry : time2eventsNumber.entrySet()) {
			// TODO: default
			String currentValue = "";
			if (granularityInHours == 24) {
				currentValue = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
			}
			if (granularityInHours == 1) {
				currentValue = entry.getKey().getHour() + ":" + entry.getKey().getMinute();
			}
			dataset.addValue(entry.getValue(), eventNameInLegend, currentValue);
		}
		return dataset;
	}

	private DefaultCategoryDataset createMultilineDataset(List<Pair<List<? extends TemporalEvent>, String>> eventsDatasets, int granularityInHours) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		if (eventsDatasets.stream().map(Pair::getFirst).allMatch(List::isEmpty)) {
			eventsDatasets.forEach( e -> dataset.addValue(100, e.getSecond(), ""));
		}

		for(Pair<List<? extends TemporalEvent>, String> eventsDataset : eventsDatasets) {
			if (eventsDataset.getFirst().isEmpty()) {
				dataset.addValue(1, eventsDataset.getSecond(), "");
			} else {
				SortedMap<LocalDateTime, Long> time2eventsNumberA = getDatasetByTemporalEvents(eventsDataset.getFirst(), granularityInHours);
				for (Map.Entry<LocalDateTime, Long> entry : time2eventsNumberA.entrySet()) {
					// TODO: default + duplicazione codice con metodo sopra
					String currentValue = "";
					if (granularityInHours == 24) {
						currentValue = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
					}
					if (granularityInHours == 1) {
						currentValue = entry.getKey().getHour() + ":" + entry.getKey().getMinute();
					}
					dataset.addValue(entry.getValue(), eventsDataset.getSecond(), currentValue);
				}
			}
		}
		return dataset;
	}

	private SortedMap<LocalDateTime, Long> getDatasetByTemporalEvents(List<? extends TemporalEvent> events, int granularityInHours) {
		events.sort(new TemporalEventComparator());
		LocalDateTime earliestEventTime = events.get(0).getAt();
		LocalDateTime threshold = earliestEventTime
				.plus(Duration.ofHours(granularityInHours))
				.minus(Duration.ofHours(earliestEventTime.getHour()))
				.minus(Duration.ofMinutes(earliestEventTime.getMinute()))
				.minus(Duration.ofSeconds(earliestEventTime.getSecond()));

		SortedMap<LocalDateTime, Long> time2eventsNumber = new TreeMap<>();
		for (TemporalEvent event : events) {
			LocalDateTime currentEventTime = event.getAt();
			if (!currentEventTime.isBefore(threshold)) {
				threshold = threshold.plus(Duration.ofHours(granularityInHours));
			}
			time2eventsNumber.putIfAbsent(threshold.minus(Duration.ofHours(granularityInHours)), 0L);
			time2eventsNumber.computeIfPresent(threshold.minus(Duration.ofHours(granularityInHours)), (k, v) -> v + 1);
		}
		return time2eventsNumber;
	}
}
