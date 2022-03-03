package chartgram.charts;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
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

	public InputStream createPng(JFreeChart chart) {
		return createPng(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public InputStream createPng(JFreeChart chart, int width, int height) {
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

	public void saveToFile(String fileName, JFreeChart chart) {
		try {
			String path = "";
			OutputStream out = new FileOutputStream(path + "/" + fileName);
			ChartUtils.writeChartAsPNG(out, chart, 500, 500);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}
}
