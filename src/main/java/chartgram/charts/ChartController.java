package chartgram.charts;

import chartgram.charts.model.Chart;
import chartgram.persistence.entity.Message;
import chartgram.persistence.service.ServicesWrapper;
import chartgram.telegram.model.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChartController {
	private final ChartRenderer chartRenderer;
	private final ServicesWrapper servicesWrapper;

	@Autowired
	private ChartController(ChartRenderer chartRenderer, ServicesWrapper servicesWrapper){
		this.chartRenderer = chartRenderer;
		this.servicesWrapper = servicesWrapper;
	}

	public Chart getChart(String type, String groupId) {
		// TODO: test
		groupId = "-1001246028586";

		if (type.equals("pie")) {
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
			JFreeChart chart = ChartFactory.createPieChart("Chart", dataset, false, true, false);
			InputStream image = chartRenderer.createPng(chart);
			String caption = "PIE";
			return new Chart(image, caption);
		}
		return null;
	}

	private DefaultPieDataset<String> createPieDataset(Map<String, Long> values) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		values.forEach(dataset::setValue);
		return dataset;
	}
}
