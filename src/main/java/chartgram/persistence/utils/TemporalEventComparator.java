package chartgram.persistence.utils;

import chartgram.persistence.entity.TemporalEvent;

import java.util.Comparator;

public class TemporalEventComparator implements Comparator<TemporalEvent> {
	@Override
	public int compare(TemporalEvent e1, TemporalEvent e2) {
		return e1.getAt().compareTo(e2.getAt());
	}
}
