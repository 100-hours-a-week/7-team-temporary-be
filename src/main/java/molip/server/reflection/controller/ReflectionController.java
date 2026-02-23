package molip.server.reflection.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.reflection.dto.request.ReflectionCreateRequest;
import molip.server.reflection.dto.request.ReflectionOpenUpdateRequest;
import molip.server.reflection.dto.request.ReflectionUpdateRequest;
import molip.server.reflection.dto.response.ReflectionCreateResponse;
import molip.server.reflection.dto.response.ReflectionDetailResponse;
import molip.server.reflection.dto.response.ReflectionExistResponse;
import molip.server.reflection.dto.response.ReflectionLikeResponse;
import molip.server.reflection.dto.response.ReflectionListItemResponse;
import molip.server.reflection.facade.ReflectionCommandFacade;
import molip.server.reflection.facade.ReflectionLikeCommandFacade;
import molip.server.reflection.facade.ReflectionQueryFacade;
import molip.server.reflection.service.ReflectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReflectionController implements ReflectionApi {

    private final ReflectionCommandFacade reflectionCommandFacade;
    private final ReflectionLikeCommandFacade reflectionLikeCommandFacade;
    private final ReflectionQueryFacade reflectionQueryFacade;
    private final ReflectionService reflectionService;

    @PostMapping("/day-plan/{dayPlanId}/reflection")
    @Override
    public ResponseEntity<ServerResponse<ReflectionCreateResponse>> createReflection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long dayPlanId,
            @RequestBody ReflectionCreateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        ReflectionCreateResponse response =
                reflectionCommandFacade.createReflection(userId, dayPlanId, request);

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.REFLECTION_CREATED, response));
    }

    @GetMapping("/day-plan/{dayPlanId}/reflection")
    @Override
    public ResponseEntity<ServerResponse<ReflectionExistResponse>> existsReflection(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long dayPlanId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        ReflectionExistResponse response =
                reflectionQueryFacade.existsReflection(userId, dayPlanId);

        SuccessCode successCode =
                response.alreadyWrote()
                        ? SuccessCode.REFLECTION_EXISTS
                        : SuccessCode.REFLECTION_NOT_EXISTS;

        return ResponseEntity.ok(ServerResponse.success(successCode, response));
    }

    @GetMapping("/reflections/me")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>>
            getMyReflections(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<ReflectionListItemResponse> response =
                reflectionQueryFacade.getMyReflections(userId, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.MY_REFLECTION_LIST_SUCCESS, response));
    }

    @GetMapping("/reflections")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ReflectionListItemResponse>>>
            getOpenReflections(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @RequestParam boolean isOpen,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        Long viewerId = null;
        if (userDetails != null) {
            viewerId = Long.valueOf(userDetails.getUsername());
        }

        PageResponse<ReflectionListItemResponse> response =
                reflectionQueryFacade.getOpenReflections(viewerId, isOpen, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.OPEN_REFLECTION_LIST_SUCCESS, response));
    }

    @GetMapping("/reflections/{reflectionId}")
    @Override
    public ResponseEntity<ServerResponse<ReflectionDetailResponse>> getReflectionDetail(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long reflectionId) {

        Long viewerId = null;
        if (userDetails != null) {
            viewerId = Long.valueOf(userDetails.getUsername());
        }

        ReflectionDetailResponse response =
                reflectionQueryFacade.getOpenReflectionDetail(viewerId, reflectionId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.REFLECTION_DETAIL_SUCCESS, response));
    }

    @PatchMapping("/reflections/{reflectionId}")
    @Override
    public ResponseEntity<Void> updateOpen(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reflectionId,
            @RequestBody ReflectionOpenUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        reflectionService.updateOpen(userId, reflectionId, request.isOpen());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reflections/{reflectionId}")
    @Override
    public ResponseEntity<Void> updateReflection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reflectionId,
            @RequestBody ReflectionUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        reflectionCommandFacade.updateReflection(
                userId, reflectionId, request.reflectionImageKeys(), request.content());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reflections/{reflectionId}")
    @Override
    public ResponseEntity<Void> deleteReflection(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long reflectionId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        reflectionCommandFacade.deleteReflection(userId, reflectionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reflections/{reflectionId}/like")
    @Override
    public ResponseEntity<Void> likeReflection(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long reflectionId) {
        Long userId = Long.valueOf(userDetails.getUsername());

        reflectionLikeCommandFacade.likeReflection(userId, reflectionId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reflections/{reflectionId}/like")
    @Override
    public ResponseEntity<Void> unlikeReflection(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long reflectionId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        reflectionLikeCommandFacade.unlikeReflection(userId, reflectionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reflections/{reflectionId}/like")
    @Override
    @Deprecated
    public ResponseEntity<ServerResponse<ReflectionLikeResponse>> getLikeStatus(
            @PathVariable Long reflectionId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }
}
