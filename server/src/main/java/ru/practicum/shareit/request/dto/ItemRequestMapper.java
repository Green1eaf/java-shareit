package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest ir, List<ItemDto> items) {
        return ItemRequestDto.builder()
                .id(ir.getId())
                .description(ir.getDescription())
                .created(ir.getCreated())
                .items(items == null ? Collections.emptyList() : items)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .created(dto.getCreated())
                .build();
    }
}
