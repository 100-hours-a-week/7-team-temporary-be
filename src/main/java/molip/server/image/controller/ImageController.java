package molip.server.image.controller;

import molip.server.common.response.ServerResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.dto.response.ImageUploadUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController implements ImageApi {

  @PostMapping("/images")
  @Override
  public ResponseEntity<ServerResponse<ImageUploadUrlResponse>> issueUploadUrl() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/images/{imageKey}")
  @Override
  public ResponseEntity<ServerResponse<ImageGetUrlResponse>> issueGetUrl(
      @PathVariable String imageKey) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
