package chartgram.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class Pair<A, B> {
	@NonNull
	private A first;
	@NonNull
	private B second;
}