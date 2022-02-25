package chartgram.persistence.service;

import chartgram.persistence.entity.User;
import chartgram.persistence.entity.UserInGroup;
import chartgram.persistence.repository.UserInGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserInGroupService {
	private final UserInGroupRepository userInGroupRepository;

	@Autowired
	private UserInGroupService(UserInGroupRepository userInGroupRepository) {
		this.userInGroupRepository = userInGroupRepository;
	}

	public List<User> getUsersByGroupId(String groupId) {
		return userInGroupRepository.getAllByGroupTelegramId(groupId).stream()
				.map(UserInGroup::getUser)
				.collect(Collectors.toList());
	}
}
