package molip.server.issue.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.issue.entity.Issue;
import molip.server.issue.repository.IssueRepository;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    @Transactional
    public Issue createIssue(Long userId, String content) {

        validateContent(content);

        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return issueRepository.save(new Issue(user, content));
    }

    private void validateContent(String content) {

        if (content == null || content.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }
}
