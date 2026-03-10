package molip.server.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.WebRtcTokenIssueRequest;
import molip.server.chat.dto.response.WebRtcTokenIssueResponse;
import molip.server.chat.facade.WebRtcCommandFacade;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebRtcController implements WebRtcApi {

    private final WebRtcCommandFacade webRtcCommandFacade;

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
}
