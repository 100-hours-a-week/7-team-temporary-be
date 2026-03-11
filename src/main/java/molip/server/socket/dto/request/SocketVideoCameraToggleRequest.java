package molip.server.socket.dto.request;

public record SocketVideoCameraToggleRequest(
        Long roomId, Long participantId, Boolean cameraEnabled, String requestId) {}
