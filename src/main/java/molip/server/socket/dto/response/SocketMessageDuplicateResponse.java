package molip.server.socket.dto.response;

public record SocketMessageDuplicateResponse(String idempotencyKey, Long messageId, String status) {

    public static SocketMessageDuplicateResponse of(
            String idempotencyKey, Long messageId, String status) {
        return new SocketMessageDuplicateResponse(idempotencyKey, messageId, status);
    }
}
