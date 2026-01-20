package molip.server.user.service;

import lombok.RequiredArgsConstructor;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
}
