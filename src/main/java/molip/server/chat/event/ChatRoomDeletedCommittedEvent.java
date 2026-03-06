package molip.server.chat.event;

import java.util.List;
import molip.server.socket.dto.response.SocketRoomDeletedResponse;

public record ChatRoomDeletedCommittedEvent(
        List<Long> targetUserIds, SocketRoomDeletedResponse roomDeleted) {}
