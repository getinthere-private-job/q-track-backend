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
    public UserResponse.Signup signup(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new Exception400("이미 존재하는 사용자명입니다: " + username);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, role);
        User savedUser = userRepository.save(user);
        return new UserResponse.Signup(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    public UserResponse.Get login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception401("사용자명 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception401("사용자명 또는 비밀번호가 잘못되었습니다.");
        }

        return new UserResponse.Get(user.getId(), user.getUsername(), user.getRole());
    }

    public UserResponse.Get findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다: " + id));
        return new UserResponse.Get(user.getId(), user.getUsername(), user.getRole());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다: " + username));
    }
}
