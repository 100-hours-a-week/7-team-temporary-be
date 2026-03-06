package molip.server.report.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.ServerResponse;
import molip.server.report.dto.request.ReportMessageCreateRequest;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.dto.response.ReportMessageItemResponse;
import molip.server.report.dto.response.ReportMessageStreamResumeResponse;
import molip.server.report.dto.response.ReportResponse;
import molip.server.report.facade.ReportChatStreamFacade;
import molip.server.report.facade.ReportCommandFacade;
import molip.server.report.facade.ReportQueryFacade;
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

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportApi {

    private final ReportCommandFacade reportCommandFacade;
    private final ReportQueryFacade reportQueryFacade;
    private final ReportChatStreamFacade reportChatStreamFacade;

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
    @Override
    public ResponseEntity<ServerResponse<CursorResponse<ReportMessageItemResponse>>>
            getReportMessages(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @PathVariable Long reportId,
                    @RequestParam(required = false) Long cursor,
                    @RequestParam(required = false, defaultValue = "5") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());

        CursorResponse<ReportMessageItemResponse> response =
                reportQueryFacade.getReportMessages(userId, reportId, cursor, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.REPORT_MESSAGE_LIST_SUCCESS, response));
    }

    @PostMapping("/reports/{reportId}/message")
    @Override
    public ResponseEntity<ServerResponse<ReportMessageCreateResponse>> createReportMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reportId,
            @RequestBody ReportMessageCreateRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ReportMessageCreateResponse response =
                reportCommandFacade.createReportMessage(userId, reportId, request.inputMessage());

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.REPORT_MESSAGE_CREATED, response));
    }

    @GetMapping("/reports/{reportId}/message/{messageId}")
    @Override
    public ResponseEntity<ServerResponse<ReportMessageStreamResumeResponse>> streamReportMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reportId,
            @PathVariable("messageId") Long streamMessageId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        ReportMessageStreamResumeResponse response =
                reportChatStreamFacade.resumeStream(userId, reportId, streamMessageId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.REPORT_MESSAGE_STREAM_RESUME_SUCCESS, response));
    }

    @DeleteMapping("/reports/{reportId}/message/{messageId}")
    @Override
    public ResponseEntity<Void> cancelReportMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reportId,
            @PathVariable("messageId") Long streamMessageId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        reportCommandFacade.cancelReportMessage(userId, reportId, streamMessageId);

        return ResponseEntity.noContent().build();
    }
}
