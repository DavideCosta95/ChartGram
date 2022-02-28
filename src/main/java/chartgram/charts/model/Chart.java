package chartgram.charts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class Chart {
	@NonNull
	private InputStream image;

	private String caption;
}
