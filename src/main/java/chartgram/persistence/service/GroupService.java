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

	public List<Group> list() {
		List<Group> result = new ArrayList<>();
		groupRepository.findAll().forEach(result::add);
		return result;
	}

	public long add(Group group) {
		Group persistedGroup = groupRepository.save(group);
		return persistedGroup.getId();
	}
}
