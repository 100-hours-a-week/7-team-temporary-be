package molip.server.socket.dto.request;

import jakarta.validation.constraints.NotNull;

public record SocketVideoParticipantOnlineRequest(
        @NotNull Long roomId,
        @NotNull Long participantId,
        @NotNull String sessionId,
        @NotNull Boolean cameraEnabled) {}
