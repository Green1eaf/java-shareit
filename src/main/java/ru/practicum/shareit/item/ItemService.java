package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemService(ItemStorage itemStorage, UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    public ItemDto create(Item item, long userId) {
        var owner = userService.findById(userId);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.create(item));
    }

    public ItemDto update(Item item, long itemId, long userId) {
        var updatedItem = checkIfExists(itemStorage.findById(itemId));
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

    public ItemDto findById(long itemId) {
        var item = checkIfExists(itemStorage.findById(itemId));
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> findAllByUserId(long userId) {
        return findAllItemDtoByFilter(item -> item.getOwner() != null && item.getOwner().getId() == userId);
    }

    public List<ItemDto> searchByText(String text) {
        return text.isEmpty() ? Collections.emptyList() :
                findAllItemDtoByFilter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable());
    }

    private List<ItemDto> findAllItemDtoByFilter(Predicate<Item> filter) {
        return itemStorage.findAll().stream()
                .filter(filter)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private <T> T checkIfExists(T obj) {
        if (obj == null) {
            throw new NotExistException("Item not exists");
        }
        return obj;
    }
}
