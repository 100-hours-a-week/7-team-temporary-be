package molip.server.report.controller;

import molip.server.common.response.CursorResponse;
import molip.server.common.response.ServerResponse;
import molip.server.report.dto.request.ReportMessageCreateRequest;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.dto.response.ReportMessageItemResponse;
import molip.server.report.dto.response.ReportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class ReportController implements ReportApi {

    @GetMapping("/reports")
    @Override
    public ResponseEntity<ServerResponse<ReportResponse>> getReportByStartDate(
            @RequestParam String startDate) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/reports/{reportId}/messages")
    @Override
    public ResponseEntity<ServerResponse<CursorResponse<ReportMessageItemResponse>>>
            getReportMessages(
                    @PathVariable Long reportId,
                    @RequestParam(required = false) Long cursor,
                    @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PostMapping("/reports/{reportId}/message")
    @Override
    public ResponseEntity<ServerResponse<ReportMessageCreateResponse>> createReportMessage(
            @PathVariable Long reportId, @RequestBody ReportMessageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping(
            value = "/reports/{reportId}/message/{messageId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public ResponseEntity<SseEmitter> streamReportMessage(
            @PathVariable Long reportId, @PathVariable Long messageId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @DeleteMapping("/reports/{reportId}/message/{messageId}")
    @Override
    public ResponseEntity<Void> cancelReportMessage(
            @PathVariable Long reportId, @PathVariable Long messageId) {
        return ResponseEntity.noContent().build();
    }
}
