package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.util.EntityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl {
    private final RequestRepository requestRepository;
    private final EntityUtils entityUtils;
    private final ItemRepository itemRepository;

    public ItemRequestDto create(ItemRequestDto itemRequestDto, long userId) {
        var user = entityUtils.getUserIfExists(userId);

        var itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        var itemFromRepo = requestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemFromRepo, null);
    }

    public List<ItemRequestDto> findAllByUser(long userId) {
        entityUtils.getUserIfExists(userId);
        return requestRepository.findAllByRequestorId(userId).stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(itemRequest,
                        itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                                .map(ItemMapper::toItemDto)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public ItemRequestDto findById(long id, long userId) {
        entityUtils.getUserIfExists(userId);

        var itemRequest = requestRepository.findById(id)
                .orElseThrow(() -> new NotExistException("Request with id=" + id + " not exists"));

        var items = itemRepository.findAllByRequestId(id).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return ItemRequestMapper.toItemRequestDto(itemRequest, items);
    }

    public List<ItemRequestDto> findAllByParams(long userId, Pageable pageable) {
        return requestRepository.findAllByRequestorIdNot(userId, pageable).stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(itemRequest,
                        itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                                .map(ItemMapper::toItemDto)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
