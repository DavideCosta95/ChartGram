package chartgram.persistence.service;

import chartgram.persistence.entity.Group;
import chartgram.persistence.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupService {
	private final GroupRepository groupRepository;

	@Autowired
	private GroupService(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	public List<Group> getAll() {
		List<Group> result = new ArrayList<>();
		groupRepository.findAll().forEach(result::add);
		return result;
	}

	public Group getByTelegramId(String telegramId) {
		return groupRepository.getGroupByTelegramId(telegramId);
	}

	public Group add(Group group) {
		Long id = group.getId();
		Group persistedGroup = groupRepository.save(group);
		if (id == null) {
			log.info("Inserted group={} in DB", persistedGroup);
		} else {
			log.info("Updated group={} in DB", persistedGroup);
		}
		return persistedGroup;
	}
}
