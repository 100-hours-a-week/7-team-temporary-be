package molip.server.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.ServerResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.dto.response.ImageUploadUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image", description = "이미지 API")
@RestController
@RequestMapping("/images")
public class ImageController {
  @Operation(summary = "이미지 업로드 URL 발급")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "발급 성공",
        content = @Content(schema = @Schema(implementation = ImageUploadUrlResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "이미지 업로드 URL 발급 실패",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ServerResponse<ImageUploadUrlResponse>> issueUploadUrl() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "이미지 조회 URL 발급")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "발급 성공",
        content = @Content(schema = @Schema(implementation = ImageGetUrlResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "이미지 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "이미지 조회 URL 발급 실패",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/{imageKey}")
  public ResponseEntity<ServerResponse<ImageGetUrlResponse>> issueGetUrl(
      @PathVariable String imageKey) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
