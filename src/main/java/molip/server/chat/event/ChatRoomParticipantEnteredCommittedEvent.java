package molip.server.chat.event;

import java.util.List;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatParticipantJoinedResponse;
import molip.server.common.enums.ChatRoomType;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatRoomParticipantEnteredCommittedEvent(
        Long roomId,
        ChatRoomType roomType,
        ChatParticipantJoinedResponse participantJoined,
        ChatMessageCreatedResponse messageCreated,
        List<SocketUnreadChangedResponse> unreadChanges) {}
