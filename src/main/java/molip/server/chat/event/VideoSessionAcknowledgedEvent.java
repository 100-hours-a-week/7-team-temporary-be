package molip.server.chat.event;

import molip.server.chat.dto.response.VideoSessionSyncedResponse;

public record VideoSessionAcknowledgedEvent(Long roomId, VideoSessionSyncedResponse payload) {}
