package chartgram.charts;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

@Component
@Slf4j
public class ChartRenderer {
	public static final int DEFAULT_WIDTH = 1000;
	public static final int DEFAULT_HEIGHT = 1000;

	private ChartRenderer(){}

	public InputStream createPng() {
		return createPng(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public InputStream createPng(int width, int height) {
		DefaultPieDataset<String> dataset = createDataset();
		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		chart.draw(g2, new Rectangle2D.Double(0, 0, width, height), null, null);

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", os);
			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}

	private void saveToFile(String fileName, JFreeChart chart) {
		try {
			String path = "";
			OutputStream out = new FileOutputStream(path + "/" + fileName);
			ChartUtils.writeChartAsPNG(out, chart, 500, 500);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	private DefaultPieDataset<String> createDataset() {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		dataset.setValue("Apache", 52);
		dataset.setValue("Nginx", 31);
		dataset.setValue("IIS", 12);
		dataset.setValue("LiteSpeed", 2);
		dataset.setValue("Google server", 1);
		dataset.setValue("Others", 2);
		return dataset;
	}

	private JFreeChart createChart(DefaultPieDataset<String> dataset) {
		return ChartFactory.createPieChart("Web servers market share", dataset, false, true, false);
	}
}
