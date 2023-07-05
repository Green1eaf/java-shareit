package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    void create(Item item);

    Item findById(long itemId);

    void update(Item item);

    List<Item> findAll();
}
