package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestServiceImpl service;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return service.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> findAllByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findAllByUser(userId);
    }

    @GetMapping("/{id}")
    public ItemRequestDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable long id) {
        return service.findById(id, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllByParams(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(value = "from", required = false, defaultValue = "0") int from,
                                        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return service.findAllByParams(userId, PageRequest.of(from, size, Sort.by("created").descending()));
    }
}
