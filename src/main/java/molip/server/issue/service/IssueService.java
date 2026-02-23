package molip.server.issue.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.issue.entity.Issue;
import molip.server.issue.repository.IssueRepository;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public Issue createIssue(Long userId, String content) {

        validateContent(content);

        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Issue issue = issueRepository.save(new Issue(user, content));
        outboxEventService.recordCreated(
                AggregateType.ISSUE, issue.getId(), OutboxPayloadMapper.issue(issue));
        return issue;
    }

    private void validateContent(String content) {

        if (content == null || content.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }
}
