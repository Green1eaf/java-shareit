package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingDto create(BookingDto bookingDto, long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotExistException("User with id=" + userId + " not exists"));
        var item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotExistException("Item with id=" + bookingDto.getItemId() + " not exists"));
        if (!item.getAvailable()) {
            throw new NotAvailableException("Item is not available");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new BadRequestException("Date can't be a null");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Start date is after or equals to end date");
        }
        var booking = bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item));
        return BookingMapper.toBookingDto(booking);
    }

    public BookingDto update(long userId, Long bookingId, Boolean approved) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotExistException("Booking with id=" + bookingId + " not exists"));
        var item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(()->new NotExistException("Item with id=" +booking.getItem().getId()+ " not exists"));
        if (item.getOwner().getId() == userId) {
            booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    public BookingDto findById(Long bookingId, Long userId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotExistException("Booking with id=" + bookingId + " not exists"));
        if (Objects.equals(booking.getBooker().getId(), userId)
                || Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            return BookingMapper.toBookingDto(booking);
        }
        throw new NotExistException("Booking with id=" + bookingId + " not exists");
    }
}
