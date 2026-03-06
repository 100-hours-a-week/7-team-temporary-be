package molip.server.chat.event;

import java.util.List;
import molip.server.socket.dto.response.SocketRoomUpdatedResponse;

public record ChatRoomUpdatedCommittedEvent(
        List<Long> targetUserIds, SocketRoomUpdatedResponse roomUpdated) {}
