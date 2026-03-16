package molip.server.chat.dto.response;

import java.util.List;

public record VideoOnlineParticipantsResponse(
        Long roomId,
        Integer participantsCount,
        List<VideoOnlineParticipantItemResponse> participants) {

    public static VideoOnlineParticipantsResponse of(
            Long roomId,
            Integer participantsCount,
            List<VideoOnlineParticipantItemResponse> participants) {
        return new VideoOnlineParticipantsResponse(roomId, participantsCount, participants);
    }
}
