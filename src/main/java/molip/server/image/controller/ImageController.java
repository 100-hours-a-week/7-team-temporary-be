package molip.server.image.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.enums.ImageType;
import molip.server.common.response.ServerResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.dto.response.ImageUploadUrlResponse;
import molip.server.image.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {
  private final ImageService imageService;

  @PostMapping("/images")
  @Override
  public ResponseEntity<ServerResponse<ImageUploadUrlResponse>> issueUploadUrl(
      @RequestParam ImageType type) {
    ImageUploadUrlResponse response = imageService.issueUploadUrl(type);
    return ResponseEntity.ok(ServerResponse.success(SuccessCode.IMAGE_UPLOAD_URL_ISSUED, response));
  }

  @GetMapping("/images/{imageKey}")
  @Override
  public ResponseEntity<ServerResponse<ImageGetUrlResponse>> issueGetUrl(
      @PathVariable String imageKey, @RequestParam ImageType type) {
    ImageGetUrlResponse response = imageService.issueGetUrl(type, imageKey);
    return ResponseEntity.ok(ServerResponse.success(SuccessCode.IMAGE_GET_URL_ISSUED, response));
  }
}
