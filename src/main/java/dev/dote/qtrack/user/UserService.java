package dev.dote.qtrack.user;

import dev.dote.qtrack._core.errors.ex.Exception400;
import dev.dote.qtrack._core.errors.ex.Exception401;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new Exception400("이미 존재하는 사용자명입니다: " + username);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, role);
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception401("비밀번호가 일치하지 않습니다");
        }

        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다: " + username));
    }
}
