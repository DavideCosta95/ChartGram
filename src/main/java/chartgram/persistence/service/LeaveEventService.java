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

	public List<LeaveEvent> getAll() {
		List<LeaveEvent> result = new ArrayList<>();
		leaveEventRepository.findAll().forEach(result::add);
		return result;
	}

	public List<LeaveEvent> getAllByGroup(String groupId) {
		return leaveEventRepository.getAllByGroupTelegramId(groupId);
	}

	public LeaveEvent add(LeaveEvent leaveEvent) {
			return leaveEventRepository.save(leaveEvent);
	}
}
