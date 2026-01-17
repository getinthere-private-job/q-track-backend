package dev.dote.qtrack.process;

public class ProcessResponse {
    public record List(Long id, String code, String name, String description, Integer sequence) {
    }

    public record Get(Long id, String code, String name, String description, Integer sequence) {
    }
}
