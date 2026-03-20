package molip.server.chat.event;

import molip.server.chat.dto.response.VideoParticipantPresenceResponse;

public record VideoParticipantOnlineEvent(Long roomId, VideoParticipantPresenceResponse payload) {}
