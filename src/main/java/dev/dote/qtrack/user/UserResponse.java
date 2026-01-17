package dev.dote.qtrack.user;

public class UserResponse {
    public record Signup(Long id, String username, Role role) {
    }

    public record Login(String token, Long id, String username, Role role) {
    }

    public record Get(Long id, String username, Role role) {
    }
}
