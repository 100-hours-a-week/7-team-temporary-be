package molip.server.chat.facade;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatRoomCommandFacade {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoomEnterResponse enterChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        Users user = userService.getUser(userId);

        ChatRoomParticipant participant =
                chatRoomParticipantService.createParticipant(user, chatRoom);

        eventPublisher.publishEvent(
                new ChatRoomParticipantEnteredEvent(chatRoom, participant, user));

        return ChatRoomEnterResponse.from(participant.getId());
    }
}
