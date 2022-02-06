package chartgram.persistence.service;

import chartgram.persistence.entity.LeaveEvent;
import chartgram.persistence.repository.LeaveEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LeaveEventService {
	private final LeaveEventRepository leaveEventRepository;

	@Autowired
	private LeaveEventService(LeaveEventRepository leaveEventRepository) {
		this.leaveEventRepository = leaveEventRepository;
	}

	public List<LeaveEvent> list() {
		List<LeaveEvent> result = new ArrayList<>();
		leaveEventRepository.findAll().forEach(result::add);
		return result;
	}

	public LeaveEvent add(LeaveEvent leaveEvent) {
		LeaveEvent alreadyPersistedLeaveEvent = leaveEventRepository.findByLeavingUser(leaveEvent.getLeavingUser());
		if (alreadyPersistedLeaveEvent == null) {
			log.info("Inserted leave event={} in DB", leaveEvent);
			return leaveEventRepository.save(leaveEvent);
		}
		log.debug("Leave event already present in DB: leave event={}", leaveEvent);
		return alreadyPersistedLeaveEvent;
	}
}
