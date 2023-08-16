package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/requests")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @ResponseBody
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID) long userId,
                                         @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.create(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUser(@RequestHeader(USER_ID) long userId) {
        return itemRequestClient.findAllByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_ID) long userId,
                                           @PathVariable long id) {
        return itemRequestClient.findById(userId, id);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllByParams(@RequestHeader(USER_ID) long userId,
                                                  @RequestParam(value = "from", required = false, defaultValue = "0") int from,
                                                  @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return itemRequestClient.findAllByParams(userId, from, size);
    }
}
