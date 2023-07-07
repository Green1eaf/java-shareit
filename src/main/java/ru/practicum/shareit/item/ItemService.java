package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemService(ItemStorage itemStorage, UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    public Item create(Item item, long userId) {
        userService.findById(userId);
        return itemStorage.create(item);
    }

    public ItemDto update(Item item, long itemId, long userId) {
        var updatedItem = findById(itemId);
        var owner = userService.findById(userId);
        if (updatedItem.getOwner() != null && updatedItem.getOwner().getId() != userId) {
            throw new UserOwnershipException("User with id=" + userId +
                    " is not the owner of the item with id=" + itemId);
        }
        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }
        updatedItem.setOwner(owner);
        itemStorage.update(updatedItem);

        return ItemMapper.toItemDto(updatedItem);
    }

    public Item findById(long itemId) {
        var item = itemStorage.findById(itemId);
        if (item == null) {
            throw new NotExistException("Item with id=" + itemId + " not exists");
        }
        return item;
    }

    public List<Item> findAll() {
        return itemStorage.findAll();
    }

    public List<Item> searchByText(String text) {
        return findAll().stream()
                .filter(item -> item.getName().contains(text) || item.getDescription().contains(text))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }
}
