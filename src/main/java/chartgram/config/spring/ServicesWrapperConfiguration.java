package chartgram.config.spring;

import chartgram.persistence.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServicesWrapperConfiguration {
	@Bean
	public ServicesWrapper servicesWrapper(GroupService groupService, JoinEventService joinEventService, LeaveEventService leaveEventService, MessageService messageService, UserInGroupService userInGroupService, UserService userService) {
		return new ServicesWrapper(groupService, joinEventService, leaveEventService, messageService, userInGroupService, userService);
	}
}