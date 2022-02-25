package chartgram.persistence.service;

import lombok.Data;
import lombok.NonNull;

@Data
public class ServicesWrapper {
	@NonNull
	private GroupService groupService;

	@NonNull
	private JoinEventService joinEventService;

	@NonNull
	private LeaveEventService leaveEventService;

	@NonNull
	private MessageService messageService;

	@NonNull
	private UserInGroupService userInGroupService;

	@NonNull
	private UserService userService;
}
