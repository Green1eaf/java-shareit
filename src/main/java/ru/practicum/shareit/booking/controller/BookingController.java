package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private static final String USER = "X-Sharer-User-Id";

    private final BookingService service;

    @PostMapping
    public BookingDto create(@RequestBody @Valid BookingDto bookingDto, @RequestHeader(USER) long userId) {
        log.info("POST: create booking for user with id={}", userId);
        return service.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader(USER) long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        log.info("PATCH/id: update status of booking with id={} and user with id={}", bookingId, userId);
        return service.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId,
                               @RequestHeader(USER) long userId) {
        log.info("GET/id: find by id booking with id={}", bookingId);
        return service.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findAllForBooker(@RequestHeader(USER) long userId,
                                             @RequestParam(required = false, defaultValue = "ALL") String state,
                                             @RequestParam(value = "from", required = false, defaultValue = "0") int from,
                                             @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        log.info("GET: find all bookings for booker with id={} and state: {}", userId, state);
        return service.findByBookerAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllItemsForOwner(@RequestHeader(USER) long userId,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state,
                                                 @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("GET/owner: find all bookings for item's owner with id={} and state{}", userId, state);
        return service.findAllItemsByOwnerAndState(userId, state, from, size);
    }
}