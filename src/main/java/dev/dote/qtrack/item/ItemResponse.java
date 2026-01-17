package dev.dote.qtrack.item;

public class ItemResponse {
    public record List(Long id, String code, String name, String description, String category) {
    }

    public record Get(Long id, String code, String name, String description, String category) {
    }

    public record Create(Long id, String code, String name, String description, String category) {
    }

    public record Update(Long id, String code, String name, String description, String category) {
    }

    public record Delete(Long id) {
    }
}
