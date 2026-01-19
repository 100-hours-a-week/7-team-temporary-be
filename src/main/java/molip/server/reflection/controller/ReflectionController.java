package molip.server.reflection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.common.response.PageResponse;
import molip.server.reflection.dto.request.ReflectionCreateRequest;
import molip.server.reflection.dto.request.ReflectionUpdateRequest;
import molip.server.reflection.dto.response.ReflectionCreateResponse;
import molip.server.reflection.dto.response.ReflectionDetailResponse;
import molip.server.reflection.dto.response.ReflectionExistResponse;
import molip.server.reflection.dto.response.ReflectionLikeResponse;
import molip.server.reflection.dto.response.ReflectionListItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reflection", description = "회고 API")
@RestController
@RequestMapping
public class ReflectionController {
  @Operation(summary = "회고 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "생성 성공",
        content = @Content(schema = @Schema(implementation = ReflectionCreateResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이미지 조건 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일자 플랜 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 회고 작성됨",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/day-plan/{dayPlanId}/reflection")
  public ResponseEntity<ServerResponse<ReflectionCreateResponse>> createReflection(
      @PathVariable Long dayPlanId, @RequestBody ReflectionCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "특정 일자 회고 작성 여부 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ReflectionExistResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일자 플랜 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/day-plan/{dayPlanId}/reflection")
  public ResponseEntity<ServerResponse<ReflectionExistResponse>> existsReflection(
      @PathVariable Long dayPlanId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "로그인한 유저의 개인 회고 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/reflections")
  public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>> getMyReflections(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "공개된 전체 회고 조회")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping(value = "/reflections", params = "isOpen")
  public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>> getOpenReflections(
      @RequestParam boolean isOpen,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "회고 상세 조회")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ReflectionDetailResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회고 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/reflections/{reflectionId}")
  public ResponseEntity<ServerResponse<ReflectionDetailResponse>> getReflectionDetail(
      @PathVariable Long reflectionId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "회고 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이미지 조건 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회고 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PutMapping("/reflections/{reflectionId}")
  public ResponseEntity<Void> updateReflection(
      @PathVariable Long reflectionId, @RequestBody ReflectionUpdateRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회고 좋아요 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회고 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 좋아요 누름",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/reflections/{reflectionId}/like")
  public ResponseEntity<Void> likeReflection(@PathVariable Long reflectionId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회고 좋아요 삭제")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회고 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping("/reflections/{reflectionId}/like")
  public ResponseEntity<Void> unlikeReflection(@PathVariable Long reflectionId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회고 좋아요 여부 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ReflectionLikeResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회고 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/reflections/{reflectionId}/like")
  public ResponseEntity<ServerResponse<ReflectionLikeResponse>> getLikeStatus(
      @PathVariable Long reflectionId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
