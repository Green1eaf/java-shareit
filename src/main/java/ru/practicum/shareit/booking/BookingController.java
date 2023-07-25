package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping
    public BookingDto create(@RequestBody @Valid BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        return service.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findById(bookingId, userId);
    }
}
