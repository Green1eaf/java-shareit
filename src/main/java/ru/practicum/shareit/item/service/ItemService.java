package ru.practicum.shareit.item.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    @Transactional
    ItemDto create(ItemDto itemDto, long userId);

    @Transactional
    ItemDto update(ItemDto itemDto, long itemId, long userId);

    @Transactional(readOnly = true)
    ItemDto findById(long itemId, long userId);

    @Transactional(readOnly = true)
    List<ItemDto> findAllByUserId(long userId);

    @Transactional(readOnly = true)
    List<ItemDto> searchByText(String text);

    @Transactional
    CommentDto addComment(long itemId, long userId, CommentDto commentDto);
}
