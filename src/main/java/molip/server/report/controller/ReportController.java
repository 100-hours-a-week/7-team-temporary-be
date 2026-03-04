package molip.server.report.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.ServerResponse;
import molip.server.report.dto.request.ReportMessageCreateRequest;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.dto.response.ReportMessageItemResponse;
import molip.server.report.dto.response.ReportResponse;
import molip.server.report.facade.ReportQueryFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportApi {

    private final ReportQueryFacade reportQueryFacade;

    @GetMapping("/reports")
    @Override
    public ResponseEntity<ServerResponse<ReportResponse>> getReportByStartDate(
            @AuthenticationPrincipal UserDetails userDetails, @RequestParam String startDate) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ReportResponse response = reportQueryFacade.getReportByStartDate(userId, startDate);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.REPORT_WEEKLY_SUCCESS, response));
    }

    @GetMapping("/reports/{reportId}/messages")
    @Deprecated
    @Override
    public ResponseEntity<ServerResponse<CursorResponse<ReportMessageItemResponse>>>
            getReportMessages(
                    @PathVariable Long reportId,
                    @RequestParam(required = false) Long cursor,
                    @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PostMapping("/reports/{reportId}/message")
    @Deprecated
    @Override
    public ResponseEntity<ServerResponse<ReportMessageCreateResponse>> createReportMessage(
            @PathVariable Long reportId, @RequestBody ReportMessageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping(
            value = "/reports/{reportId}/message/{messageId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Deprecated
    @Override
    public ResponseEntity<SseEmitter> streamReportMessage(
            @PathVariable Long reportId, @PathVariable Long messageId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @DeleteMapping("/reports/{reportId}/message/{messageId}")
    @Deprecated
    @Override
    public ResponseEntity<Void> cancelReportMessage(
            @PathVariable Long reportId, @PathVariable Long messageId) {
        return ResponseEntity.noContent().build();
    }
}
