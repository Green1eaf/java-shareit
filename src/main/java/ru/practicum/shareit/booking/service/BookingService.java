package ru.practicum.shareit.booking.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    @Transactional
    BookingDto create(BookingDto bookingDto, long userId);

    @Transactional
    BookingDto updateStatus(long userId, Long bookingId, Boolean approved);

    @Transactional(readOnly = true)
    BookingDto findById(Long bookingId, Long userId);

    @Transactional(readOnly = true)
    List<BookingDto> findByBookerAndState(long userId, String state);

    @Transactional(readOnly = true)
    List<BookingDto> findAllItemsByOwnerAndState(long userId, String state);
}
