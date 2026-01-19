package molip.server.terms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.common.response.PageResponse;
import molip.server.terms.dto.request.TermsSignRequest;
import molip.server.terms.dto.response.TermsItemResponse;
import molip.server.terms.dto.response.TermsSignItemResponse;
import molip.server.terms.dto.response.TermsSignResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Terms", description = "약관 API")
@RestController
@RequestMapping
public class TermsController {
  @Operation(summary = "활성화된 약관 전체 조회")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/terms")
  public ResponseEntity<ServerResponse<PageResponse<TermsItemResponse>>> getTerms() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "약관 동의 내역 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "생성 성공",
        content = @Content(schema = @Schema(implementation = TermsSignResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "약관 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 동의 내역 존재",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/terms-sign/{termsId}")
  public ResponseEntity<ServerResponse<TermsSignResponse>> createTermsSign(
      @PathVariable Long termsId, @RequestBody TermsSignRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "약관 동의 내역 변경")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "변경 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "약관 동의 내역 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "필수 약관 철회 불가",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/terms-sign/{termsId}")
  public ResponseEntity<Void> updateTermsSign(
      @PathVariable Long termsId, @RequestBody TermsSignRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "로그인한 유저의 약관 동의 내역 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/terms-sign")
  public ResponseEntity<ServerResponse<PageResponse<TermsSignItemResponse>>> getMyTermsSigns() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "특정 약관에 대한 로그인한 유저의 동의 내역 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = TermsSignItemResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "조회 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "약관 동의 내역 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/terms-sign/{termsSignId}")
  public ResponseEntity<ServerResponse<TermsSignItemResponse>> getTermsSign(
      @PathVariable Long termsSignId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
