package molip.server.issue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.issue.dto.request.IssueCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Issue", description = "이슈 제보 API")
public interface IssueApi {

    @Operation(summary = "이슈 제보")
    @SecurityRequirement(name = "JWT")
    @RequestBody(
            description = "이슈 제보 요청",
            required = true,
            content =
                    @Content(
                            schema = @Schema(implementation = IssueCreateRequest.class),
                            examples = @ExampleObject(value = "{\n  \"content\": \"이슈 내용\"\n}")))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "이슈 제보 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "필수 값 누락",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "회원 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<Void> createIssue(
            @AuthenticationPrincipal UserDetails userDetails, IssueCreateRequest request);
}
