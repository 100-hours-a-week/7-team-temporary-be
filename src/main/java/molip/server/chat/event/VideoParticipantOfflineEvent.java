package molip.server.chat.event;

import molip.server.chat.dto.response.VideoParticipantPresenceResponse;

public record VideoParticipantOfflineEvent(Long roomId, VideoParticipantPresenceResponse payload) {}
