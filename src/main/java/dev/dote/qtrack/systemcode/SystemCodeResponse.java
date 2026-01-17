package dev.dote.qtrack.systemcode;

public class SystemCodeResponse {
    public record List(Long id, String codeGroup, String codeKey, String codeValue, String description, Boolean isActive) {
    }
}
