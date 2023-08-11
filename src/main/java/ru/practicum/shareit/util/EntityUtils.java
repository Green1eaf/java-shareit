package ru.practicum.shareit.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class EntityUtils {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final RequestRepository requestRepository;

    /**
     * Фильтр для определения статуса бронирования
     * EnumMap для быстрого доступа и статический утильный метод
     */
    private static final Map<State, Predicate<Booking>> STATE_FILTER = new EnumMap<>(State.class);

    static {
        STATE_FILTER.put(State.ALL, b -> true);
        STATE_FILTER.put(State.PAST, b -> b.getEnd().isBefore(LocalDateTime.now()));
        STATE_FILTER.put(State.FUTURE, b -> b.getStart().isAfter(LocalDateTime.now()));
        STATE_FILTER.put(State.CURRENT, b -> b.getStart().isBefore(LocalDateTime.now())
                && b.getEnd().isAfter(LocalDateTime.now()));
        STATE_FILTER.put(State.WAITING, b -> b.getStatus().equals(Status.WAITING));
        STATE_FILTER.put(State.REJECTED, b -> b.getStatus().equals(Status.REJECTED));
    }

    public static Predicate<Booking>
    stateBy(State state) {
        return STATE_FILTER.get(state);
    }

    /**
     * Методы проверки наличия сущности
     * возвращают либо сущность, либо выбрасывают исключение
     */
    public User getUserIfExists(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotExistException("User with id=" + userId + " not exists"));
    }

    public Item getItemIfExists(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotExistException("Item with id=" + itemId + " not exists"));
    }

    public Booking getBookingIfExists(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotExistException("Booking with id=" + bookingId + " not exists"));
    }

    public ItemRequest getItemRequestIfExists(long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotExistException("Request with id=" + id + " not exists"));
    }
}
