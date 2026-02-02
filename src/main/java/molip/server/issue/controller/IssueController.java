package molip.server.issue.controller;

import lombok.RequiredArgsConstructor;
import molip.server.issue.dto.request.IssueCreateRequest;
import molip.server.issue.service.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IssueController implements IssueApi {

    private final IssueService issueService;

    @PostMapping("/issue")
    @Override
    public ResponseEntity<Void> createIssue(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody IssueCreateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        issueService.createIssue(userId, request.content());

        return ResponseEntity.noContent().build();
    }
}
