package dev.dote.qtrack.item;

import jakarta.validation.constraints.NotBlank;

public class ItemRequest {
    public record Create(
            @NotBlank(message = "부품 코드는 필수입니다") String code,
            @NotBlank(message = "부품명은 필수입니다") String name,
            String description,
            String category) {
    }

    public record Update(
            @NotBlank(message = "부품명은 필수입니다") String name,
            String description,
            String category) {
    }
}
