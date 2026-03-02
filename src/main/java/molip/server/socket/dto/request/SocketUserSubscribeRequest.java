package molip.server.socket.dto.request;

import java.time.OffsetDateTime;

public record SocketUserSubscribeRequest(OffsetDateTime requestedAt) {}
