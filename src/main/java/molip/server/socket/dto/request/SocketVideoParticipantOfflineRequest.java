package molip.server.socket.dto.request;

import jakarta.validation.constraints.NotNull;

public record SocketVideoParticipantOfflineRequest(
        @NotNull Long roomId, @NotNull Long participantId, @NotNull String sessionId) {}
