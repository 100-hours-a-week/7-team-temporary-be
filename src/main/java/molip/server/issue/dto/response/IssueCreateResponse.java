package molip.server.issue.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 제보 응답")
public record IssueCreateResponse(@Schema(description = "이슈 ID", example = "1") Long issueId) {

    public static IssueCreateResponse from(Long issueId) {

        return new IssueCreateResponse(issueId);
    }
}
