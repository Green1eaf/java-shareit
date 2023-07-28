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

    private final BookingService service;

    @PostMapping
    public BookingDto create(@RequestBody @Valid BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("POST: create booking for user with id={}", userId);
        return service.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        log.info("PATCH/id: update status of booking with id={} and user with id={}", bookingId, userId);
        return service.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET/id: find by id booking with id={}", bookingId);
        return service.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findAllForBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestParam(required = false) String state) {
        log.info("GET: find all bookings for booker with id={} and state: {}", userId, state);
        return service.findByBookerAndState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllItemsForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(required = false) String state) {
        log.info("GET/owner: find all bookings for item's owner with id={} and state{}", userId, state);
        return service.findAllItemsByOwnerAndState(userId, state);
    }
}