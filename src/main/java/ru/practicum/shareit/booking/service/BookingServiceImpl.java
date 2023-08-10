package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.util.EntityUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.shareit.util.EntityUtils.stateBy;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EntityUtils utils;

    @Override
    @Transactional
    public BookingDto create(BookingDto bookingDto, long userId) {
        var user = utils.getUserIfExists(userId);   //Возвращаем пользователя, если он существует
        var item = utils.getItemIfExists(bookingDto.getItemId());   //Возвращаем вещь, если она существует

        if (!item.getAvailable()) {     //Вещь должна быть доступна для бронирования
            throw new NotAvailableException("Item is not available");
        }

        //Время бронирования должно быть консистентно
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new BadRequestException("Date can't be a null");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Start date is after or equals to end date");
        }

        if (userId == item.getOwner().getId()) {    //Владелец не может забронировать свою вещь
            throw new NotExistException("Owner can't booked his own item");
        }

        log.info("Booking for user with id={} was created", userId);
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item)));
    }

    @Override
    @Transactional
    public BookingDto updateStatus(long userId, Long bookingId, Boolean approved) {
        var booking = utils.getBookingIfExists(bookingId);  // Проверяем наличие бронирования по id

        if (booking.getStatus() == (Status.APPROVED) && approved) {  // Статус не должен быть APPROVE
            throw new BadRequestException("Booking is already approved");
        }

        var item = utils.getItemIfExists(booking.getItem().getId());    //Проверяем наличие вещи по id

        if (item.getOwner().getId() == userId) {    //Подтверждать бронирование может только владелец вещи
            booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        } else {
            throw new NotExistException("User with id=" + userId + " is not the owner");
        }

        log.info("Status for booking with id={} was updated", bookingId);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(Long bookingId, Long userId) {
        var booking = utils.getBookingIfExists(bookingId);

        //бронирование доступно для просмотра только для владельца вещи или владельца бронирования
        if (!Objects.equals(booking.getBooker().getId(), userId)
                && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotExistException("Booking with id=" + bookingId + " not available for view");
        }
        log.info("Get booking with id={}", bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByBookerAndState(long userId, String state, int from, int size) {
        utils.getUserIfExists(userId);
        log.info("Get all bookings for booker with id={} and with state: {}", userId, state);
        return pagination(from, size, findAllByState(bookingRepository.findAllByBookerId(userId), state));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findAllItemsByOwnerAndState(long userId, String state, int from, int size) {
        utils.getUserIfExists(userId);
        log.info("Get all bookings for owner with id={} and with state: {}", userId, state);
        return pagination(from, size, findAllByState(bookingRepository.findAllByItem_OwnerId(userId), state));
    }

    private List<BookingDto> findAllByState(List<Booking> bookings, String state) {
        return bookings.stream()
                .filter(stateBy(parseState(state)))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private List<BookingDto> pagination(int from, int size, List<BookingDto> list) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Bad params from or size for request");
        }
        return list.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * Метод парсит String в State
     */
    private static State parseState(String state) {
        if (state == null || state.isBlank()) {
            return State.ALL;
        }
        if (Stream.of(State.values()).anyMatch(s -> s.name().equals(state))) {
            return State.valueOf(state);
        } else {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
