package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item create(Item item);

    Item findById(long itemId);

    Item update(Item item);

    List<Item> findAll();
}
