package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        if (userId == item.getOwner().getId()) {
            throw new NotExistException("Owner can't booked own item");
        }
        var booking = bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item));
        return BookingMapper.toBookingDto(booking);
    }

    public BookingDto update(long userId, Long bookingId, Boolean approved) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotExistException("Booking with id=" + bookingId + " not exists"));
        if (booking.getStatus().equals(Status.APPROVED) && approved) {
            throw new BadRequestException("Booking is already approved");
        }
        var item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotExistException("Item with id=" + booking.getItem().getId() + " not exists"));
        if (item.getOwner().getId() == userId) {
            booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        } else {
            throw new NotExistException("User with id=" + userId + " is not the owner");
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

    public List<BookingDto> findByBookerAndState(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotExistException("User with id=" + userId + " not exists");
        }
        State bookingState;
        if (state == null || state.isBlank()) {
            bookingState = State.ALL;
        } else {
            try {
                bookingState = State.valueOf(state);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Unknown state: " + state);
            }
        }
        return bookingRepository.findAllByBookerId(userId).stream()
                .filter(filter(bookingState))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> findAllItemsByOwnerAndState(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotExistException("User with id=" + userId + " not exists");
        }
        State bookingState;
        if (state == null || state.isBlank()) {
            bookingState = State.ALL;
        } else {
            try {
                bookingState = State.valueOf(state);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Unknown state: " + state);
            }
        }
        var bookings = bookingRepository.findAllByItem_OwnerId(userId);
        return bookings.stream()
                .filter(filter(bookingState))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    private Predicate<Booking> filter(State state) {
        switch (state) {
            case ALL:
                return b -> true;
            case PAST:
                return b -> b.getEnd().isBefore(LocalDateTime.now());
            case FUTURE:
                return b -> b.getStart().isAfter(LocalDateTime.now());
            case CURRENT:
                return b -> b.getStart().isBefore(LocalDateTime.now())
                        && b.getEnd().isAfter(LocalDateTime.now());
            case WAITING:
                return b -> b.getStatus().equals(Status.WAITING);
            case REJECTED:
                return b -> b.getStatus().equals(Status.REJECTED);
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
