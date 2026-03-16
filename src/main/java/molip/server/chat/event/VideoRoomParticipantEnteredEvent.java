package molip.server.chat.event;

import molip.server.chat.dto.response.ChatParticipantJoinedResponse;

public record VideoRoomParticipantEnteredEvent(
        Long roomId, ChatParticipantJoinedResponse payload) {}
