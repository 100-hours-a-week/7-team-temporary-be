package molip.server.auth.dto.response;

public record AuthResponse(String accessToken, String refreshToken, String deviceId) {}
