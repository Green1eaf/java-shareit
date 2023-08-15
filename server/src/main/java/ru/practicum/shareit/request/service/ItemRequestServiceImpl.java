package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.util.EntityUtils;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.ItemRequestMapper.toItemRequest;
import static ru.practicum.shareit.request.dto.ItemRequestMapper.toItemRequestDto;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final RequestRepository requestRepository;
    private final EntityUtils entityUtils;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, long userId) {
        var user = entityUtils.getUserIfExists(userId);

        var itemRequest = toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        var itemFromRepo = requestRepository.save(itemRequest);
        return toItemRequestDto(itemFromRepo, null);
    }

    @Override
    public List<ItemRequestDto> findAllByUser(long userId) {
        entityUtils.getUserIfExists(userId);
        return requestRepository.findAllByRequestorId(userId).stream()
                .map(itemRequest -> toItemRequestDto(itemRequest,
                        itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                                .map(ItemMapper::toItemDto)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto findById(long id, long userId) {
        entityUtils.getUserIfExists(userId);

        var itemRequest = entityUtils.getItemRequestIfExists(id);

        var items = itemRepository.findAllByRequestId(id).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return toItemRequestDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestDto> findAllByParams(long userId, Pageable pageable) {
        return requestRepository.findAllByRequestorIdNot(userId, pageable).stream()
                .map(itemRequest -> toItemRequestDto(itemRequest,
                        getItemDtoListByRequestId(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Метод по requestsId достает список вещей и преобразует их в Dto
     * нужен для упрощения чтения кода
     */
    private List<ItemDto> getItemDtoListByRequestId(long requestsId) {
        return itemRepository.findAllByRequestId(requestsId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
