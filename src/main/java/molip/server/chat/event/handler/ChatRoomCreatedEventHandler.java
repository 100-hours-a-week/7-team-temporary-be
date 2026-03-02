package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.chat.event.ChatRoomCreatedEvent;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomCreatedEventHandler {

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final UserService userService;

    @EventListener
    public void handle(ChatRoomCreatedEvent event) {
        Users owner = userService.getUser(event.ownerId());

        chatRoomParticipantService.createOwnerParticipant(owner, event.chatRoom());
    }
}
