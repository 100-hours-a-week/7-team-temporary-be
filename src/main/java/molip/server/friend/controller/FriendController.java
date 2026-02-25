package molip.server.friend.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.friend.dto.request.FriendRequestStatusUpdateRequest;
import molip.server.friend.dto.response.FriendItemResponse;
import molip.server.friend.dto.response.FriendRequestItemResponse;
import molip.server.friend.dto.response.FriendRequestResponse;
import molip.server.friend.facade.FriendRequestCommandFacade;
import molip.server.friend.facade.FriendRequestQueryFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FriendController implements FriendApi {

    private final FriendRequestCommandFacade friendRequestCommandFacade;
    private final FriendRequestQueryFacade friendRequestQueryFacade;

    @PostMapping("/friend-requests/{targetUserId}")
    @Override
    public ResponseEntity<ServerResponse<FriendRequestResponse>> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long targetUserId) {

        Long fromUserId = Long.valueOf(userDetails.getUsername());

        FriendRequestResponse response =
                friendRequestCommandFacade.sendFriendRequest(fromUserId, targetUserId);

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.FRIEND_REQUEST_SENT, response));
    }

    @GetMapping("/friend-requests")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<FriendRequestItemResponse>>>
            getFriendRequests(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {

        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<FriendRequestItemResponse> response =
                friendRequestQueryFacade.getFriendRequests(userId, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.FRIEND_REQUEST_LIST_SUCCESS, response));
    }

    @DeleteMapping("/friend-requests/{requestId}")
    @Override
    public ResponseEntity<Void> deleteFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long requestId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        friendRequestCommandFacade.deleteFriendRequest(userId, requestId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/friend-requests/{requestId}")
    @Override
    public ResponseEntity<Void> updateFriendRequestStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @RequestBody FriendRequestStatusUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        friendRequestCommandFacade.updateFriendRequestStatus(userId, requestId, request.status());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/friends/{friendUserId}")
    @Deprecated
    @Override
    public ResponseEntity<Void> deleteFriend(@PathVariable Long friendUserId) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/friends")
    @Deprecated
    @Override
    public ResponseEntity<ServerResponse<PageResponse<FriendItemResponse>>> getFriends(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }
}
