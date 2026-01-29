package molip.server.user.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.terms.event.UserTermsAgreedEvent;
import molip.server.user.dto.request.TermsAgreementRequest;
import molip.server.user.entity.Users;
import molip.server.user.event.UserProfileImageLinkedEvent;
import molip.server.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,20}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Users registerUser(
            String email,
            String password,
            String nickname,
            Gender gender,
            LocalDate birth,
            FocusTimeZone focusTimeZone,
            LocalTime dayEndTime,
            String profileImageKey,
            List<TermsAgreementRequest> terms) {

        validateEmail(email);
        validatePassword(password);
        validateDuplicatedEmail(email);

        String encodedPassword = passwordEncoder.encode(password);

        Users savedUser =
                userRepository.save(
                        new Users(
                                email,
                                encodedPassword,
                                nickname,
                                gender,
                                birth,
                                focusTimeZone,
                                dayEndTime));

        publishProfileImageEvent(savedUser.getId(), profileImageKey);

        publishTermsAgreementEvent(savedUser.getId(), terms);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public boolean checkEmailDuplicated(String email) {
        validateEmail(email);

        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Transactional
    public void modifyUserDetails(
            Long userId,
            Gender gender,
            LocalDate birth,
            FocusTimeZone focusTimeZone,
            LocalTime dayEndTime,
            String nickname) {
        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.modifyUserDetails(gender, birth, focusTimeZone, dayEndTime, nickname);
    }

    @Transactional(readOnly = true)
    public Users getUser(Long userId) {

        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void modifyPassword(Long userId, String passwowrd) {
        validatePassword(passwowrd);

        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.modifyPassword(passwowrd);
    }

    @Transactional
    public void deleteUser(Long userId) {
        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        user.deleteUser();
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_EMAIL_POLICY);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        if (password.length() > 20) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_PASSWORD_TOO_LONG);
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_PASSWORD_POLICY);
        }
    }

    private void validateDuplicatedEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BaseException(ErrorCode.EMAIL_CONFLICT);
        }
    }

    private void publishProfileImageEvent(Long userId, String profileImageKey) {

        if (profileImageKey == null || profileImageKey.isBlank()) {
            return;
        }
        eventPublisher.publishEvent(new UserProfileImageLinkedEvent(userId, profileImageKey));
    }

    private void publishTermsAgreementEvent(Long userId, List<TermsAgreementRequest> terms) {

        eventPublisher.publishEvent(new UserTermsAgreedEvent(userId, terms));
    }
}
