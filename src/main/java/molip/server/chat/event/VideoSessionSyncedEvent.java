package molip.server.chat.event;

import molip.server.chat.dto.response.VideoPublishChangedResponse;

public record VideoSessionSyncedEvent(
        Long roomId, String eventType, VideoPublishChangedResponse payload) {}
