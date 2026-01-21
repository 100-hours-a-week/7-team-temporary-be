package molip.server.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.enums.ImageType;
import molip.server.common.response.ServerResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.dto.response.ImageUploadUrlResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Image", description = "이미지 API")
public interface ImageApi {

  @Operation(summary = "이미지 업로드 URL 발급")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "발급 성공",
        content = @Content(schema = @Schema(implementation = ImageUploadUrlResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "이미지 업로드 URL 발급 실패",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<ImageUploadUrlResponse>> issueUploadUrl(ImageType type);

  @Operation(summary = "이미지 조회 URL 발급")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "발급 성공",
        content = @Content(schema = @Schema(implementation = ImageGetUrlResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "이미지 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "이미지 조회 URL 발급 실패",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<ImageGetUrlResponse>> issueGetUrl(String imageKey, ImageType type);
}
