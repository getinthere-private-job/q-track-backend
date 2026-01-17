package dev.dote.qtrack.item;

import dev.dote.qtrack._core.errors.ex.Exception400;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemResponse.List> findAll() {
        return itemRepository.findAll().stream()
                .map(i -> new ItemResponse.List(
                        i.getId(),
                        i.getCode(),
                        i.getName(),
                        i.getDescription(),
                        i.getCategory()))
                .toList();
    }

    public ItemResponse.Get findById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new Exception400("부품을 찾을 수 없습니다: " + id));
        return new ItemResponse.Get(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getDescription(),
                item.getCategory());
    }

    @Transactional
    public ItemResponse.Create create(String code, String name, String description, String category) {
        if (itemRepository.existsByCode(code)) {
            throw new Exception400("이미 존재하는 부품 코드입니다: " + code);
        }

        Item item = new Item(code, name, description, category);
        Item savedItem = itemRepository.save(item);
        return new ItemResponse.Create(
                savedItem.getId(),
                savedItem.getCode(),
                savedItem.getName(),
                savedItem.getDescription(),
                savedItem.getCategory());
    }

    @Transactional
    public ItemResponse.Update update(Long id, String name, String description, String category) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new Exception400("부품을 찾을 수 없습니다: " + id));
        
        item.update(name, description, category);
        Item updatedItem = itemRepository.save(item);
        return new ItemResponse.Update(
                updatedItem.getId(),
                updatedItem.getCode(),
                updatedItem.getName(),
                updatedItem.getDescription(),
                updatedItem.getCategory());
    }

    @Transactional
    public ItemResponse.Delete delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new Exception400("부품을 찾을 수 없습니다: " + id));
        itemRepository.delete(item);
        return new ItemResponse.Delete(id);
    }
}
