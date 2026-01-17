package dev.dote.qtrack.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRequest {
    public record Signup(
            @NotBlank(message = "사용자명은 필수입니다") String username,
            @NotBlank(message = "비밀번호는 필수입니다") String password,
            @NotNull(message = "권한은 필수입니다") Role role
    ) {}

    public record Login(
            @NotBlank(message = "사용자명은 필수입니다") String username,
            @NotBlank(message = "비밀번호는 필수입니다") String password
    ) {}
}
