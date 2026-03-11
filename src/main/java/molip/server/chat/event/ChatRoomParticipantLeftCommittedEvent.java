package molip.server.chat.event;

import java.util.List;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatParticipantLeftResponse;
import molip.server.common.enums.ChatRoomType;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatRoomParticipantLeftCommittedEvent(
        Long roomId,
        ChatRoomType roomType,
        ChatParticipantLeftResponse participantLeft,
        ChatMessageCreatedResponse messageCreated,
        List<SocketUnreadChangedResponse> unreadChanges) {}
