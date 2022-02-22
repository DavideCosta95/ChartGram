package chartgram.persistence.service;

import chartgram.persistence.entity.JoinEvent;
import chartgram.persistence.repository.JoinEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class JoinEventService {
	private final JoinEventRepository joinEventRepository;

	@Autowired
	private JoinEventService(JoinEventRepository joinEventRepository) {
		this.joinEventRepository = joinEventRepository;
	}

	public List<JoinEvent> getAll() {
		List<JoinEvent> result = new ArrayList<>();
		joinEventRepository.findAll().forEach(result::add);
		return result;
	}

	public JoinEvent add(JoinEvent joinEvent) {
		JoinEvent alreadyPersistedJoinEvent = joinEventRepository.findByJoiningUser(joinEvent.getJoiningUser());
		if (alreadyPersistedJoinEvent == null) {
			log.info("Inserted join event={} in DB", joinEvent);
			return joinEventRepository.save(joinEvent);
		}
		log.debug("Join event already present in DB: join event={}", joinEvent);
		return alreadyPersistedJoinEvent;
	}

	public void addAll(Collection<JoinEvent> joinEvents) {
		joinEventRepository.saveAll(joinEvents);
	}
}
