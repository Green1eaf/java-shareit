package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> storage = new HashMap<>();
    private long counter = 0;

    @Override
    public void create(Item item) {
        if (item.getId() == null) {
            item.setId(++counter);
        }
        storage.put(item.getId(), item);
    }

    @Override
    public Item findById(long itemId) {
        return storage.get(itemId);
    }

    @Override
    public void update(Item item) {
        storage.put(item.getId(), item);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }
}
