package molip.server.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.ChatRoomParticipantCameraUpdateRequest;
import molip.server.chat.dto.request.VideoSessionSyncRequest;
import molip.server.chat.dto.request.WebRtcTokenIssueRequest;
import molip.server.chat.dto.response.VideoOnlineParticipantsResponse;
import molip.server.chat.dto.response.WebRtcTokenIssueResponse;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.chat.facade.WebRtcCommandFacade;
import molip.server.chat.facade.WebRtcQueryFacade;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebRtcController implements WebRtcApi {

    private final WebRtcCommandFacade webRtcCommandFacade;
    private final WebRtcQueryFacade webRtcQueryFacade;
    private final ChatRoomCommandFacade chatRoomCommandFacade;

    @PostMapping("/chat-rooms/{roomId}/webrtc/token")
    @Override
    public ResponseEntity<ServerResponse<WebRtcTokenIssueResponse>> issueWebRtcToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @Valid @RequestBody WebRtcTokenIssueRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        WebRtcTokenIssueResponse response =
                webRtcCommandFacade.issueToken(userId, roomId, request.participantId());

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.WEBRTC_TOKEN_ISSUED, response));
    }

    @PostMapping("/chat-rooms/{roomId}/video/sessions")
    @Override
    public ResponseEntity<Void> syncVideoSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @Valid @RequestBody VideoSessionSyncRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        webRtcCommandFacade.syncVideoSession(
                userId, roomId, request.participantId(), request.sessionId(), request.published());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chat-rooms/participants/{participantId}")
    @Override
    public ResponseEntity<Void> updateParticipantCamera(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long participantId,
            @RequestBody ChatRoomParticipantCameraUpdateRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        chatRoomCommandFacade.updateParticipantCamera(
                userId, participantId, request.cameraEnabled());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chat-rooms/{roomId}/video/participants/online")
    @Override
    public ResponseEntity<ServerResponse<VideoOnlineParticipantsResponse>> getOnlineParticipants(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long roomId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        VideoOnlineParticipantsResponse response =
                webRtcQueryFacade.getOnlineParticipants(userId, roomId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.WEBRTC_ONLINE_PARTICIPANTS_FETCHED, response));
    }
}
