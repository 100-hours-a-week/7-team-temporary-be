package molip.server.issue.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 제보 요청")
public record IssueCreateRequest(
        @Schema(description = "이슈 내용", example = "이슈 내용") String content) {}
