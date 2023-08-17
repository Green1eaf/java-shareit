package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, long userId);

    List<ItemRequestDto> findAllByUser(long userId);

    ItemRequestDto findById(long id, long userId);

    List<ItemRequestDto> findAllByParams(long userId, Pageable pageable);
}
