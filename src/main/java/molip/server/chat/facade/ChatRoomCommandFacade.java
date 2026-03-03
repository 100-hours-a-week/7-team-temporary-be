package molip.server.chat.facade;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
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
    private final ChatMessageService chatMessageService;
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

    @Transactional
    public void updateLastSeenMessage(
            Long userId, Long participantId, UpdateLastReadMessageRequest request) {
        ChatRoomParticipant participant = getOwnedParticipant(userId, participantId);

        chatMessageService.validateMessageInRoom(
                participant.getChatRoom().getId(), request.lastSeenMessageId());

        chatRoomParticipantService.updateLastSeenMessageId(
                participant, request.lastSeenMessageId());
    }

    private ChatRoomParticipant getOwnedParticipant(Long userId, Long participantId) {
        if (userId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService
                        .findById(participantId)
                        .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));

        if (!participant.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        return participant;
    }
}
