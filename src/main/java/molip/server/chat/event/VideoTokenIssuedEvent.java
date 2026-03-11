package molip.server.chat.event;

import molip.server.chat.dto.response.VideoTokenIssuedResponse;

public record VideoTokenIssuedEvent(Long roomId, VideoTokenIssuedResponse payload) {}
