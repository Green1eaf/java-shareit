package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto create(ItemDto itemDto, long userId) {
        return toItemDto(itemRepository.save(toItem(itemDto, toUser(checkUserExist(userId)))));
    }

    public ItemDto update(ItemDto itemDto, long itemId, long userId) {

        var updatedItem = itemRepository.findById(itemId)
                .orElseThrow(()-> new NotExistException("Item with id=" +itemId+" not exists"));
        if (updatedItem.getOwner() != null && updatedItem.getOwner().getId() != userId) {
            throw new UserOwnershipException("User with id=" + userId +
                    " is not the owner of the item with id=" + itemId);
        }
        if (itemDto.getName() != null) {
            updatedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updatedItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }
        updatedItem.setOwner(toUser(checkUserExist(userId)));
        itemRepository.save(updatedItem);

        return toItemDto(updatedItem);
    }

    public ItemDto findById(long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotExistException("Item with id=" + itemId + " not exists"));
    }

    public List<ItemDto> findAllByUserId(long userId) {
        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchByText(String text) {
        return text.isEmpty() ? Collections.emptyList() :
                findAllItemDtoByFilter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable());
    }

    private List<ItemDto> findAllItemDtoByFilter(Predicate<Item> filter) {
        return itemRepository.findAll().stream()
                .filter(filter)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private UserDto checkUserExist(long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotExistException("User with id=" + userId + " not exists"));
    }
}
