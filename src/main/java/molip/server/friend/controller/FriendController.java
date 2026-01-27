package molip.server.friend.controller;

import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.friend.dto.request.FriendRequestStatusUpdateRequest;
import molip.server.friend.dto.response.FriendItemResponse;
import molip.server.friend.dto.response.FriendRequestItemResponse;
import molip.server.friend.dto.response.FriendRequestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FriendController implements FriendApi {

    @PostMapping("/friend-requests/{targetUserId}")
    @Override
    public ResponseEntity<ServerResponse<FriendRequestResponse>> sendFriendRequest(
            @PathVariable Long targetUserId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/friend-requests")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<FriendRequestItemResponse>>>
            getFriendRequests(
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @DeleteMapping("/friend-requests/{requestId}")
    @Override
    public ResponseEntity<Void> deleteFriendRequest(@PathVariable Long requestId) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/friend-requests/{requestId}")
    @Override
    public ResponseEntity<Void> updateFriendRequestStatus(
            @PathVariable Long requestId, @RequestBody FriendRequestStatusUpdateRequest request) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/friends/{friendUserId}")
    @Override
    public ResponseEntity<Void> deleteFriend(@PathVariable Long friendUserId) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/friends")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<FriendItemResponse>>> getFriends(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }
}
