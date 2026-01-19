package molip.server.reflection.controller;

import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReflectionController implements ReflectionApi {
  @PostMapping("/day-plan/{dayPlanId}/reflection")
  @Override
  public ResponseEntity<ServerResponse<ReflectionCreateResponse>> createReflection(
      @PathVariable Long dayPlanId, @RequestBody ReflectionCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/day-plan/{dayPlanId}/reflection")
  @Override
  public ResponseEntity<ServerResponse<ReflectionExistResponse>> existsReflection(
      @PathVariable Long dayPlanId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/reflections")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>> getMyReflections(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping(value = "/reflections", params = "isOpen")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>>
      getOpenReflections(
          @RequestParam boolean isOpen,
          @RequestParam(required = false, defaultValue = "1") int page,
          @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/reflections/{reflectionId}")
  @Override
  public ResponseEntity<ServerResponse<ReflectionDetailResponse>> getReflectionDetail(
      @PathVariable Long reflectionId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PutMapping("/reflections/{reflectionId}")
  @Override
  public ResponseEntity<Void> updateReflection(
      @PathVariable Long reflectionId, @RequestBody ReflectionUpdateRequest request) {
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reflections/{reflectionId}/like")
  @Override
  public ResponseEntity<Void> likeReflection(@PathVariable Long reflectionId) {
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/reflections/{reflectionId}/like")
  @Override
  public ResponseEntity<Void> unlikeReflection(@PathVariable Long reflectionId) {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reflections/{reflectionId}/like")
  @Override
  public ResponseEntity<ServerResponse<ReflectionLikeResponse>> getLikeStatus(
      @PathVariable Long reflectionId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
