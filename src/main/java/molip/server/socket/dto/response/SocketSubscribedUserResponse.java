package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketSubscribedUserResponse(Long userId, OffsetDateTime subscribedAt) {

    public static SocketSubscribedUserResponse of(Long userId, OffsetDateTime subscribedAt) {
        return new SocketSubscribedUserResponse(userId, subscribedAt);
    }
}
