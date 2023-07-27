package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.EntityUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final EntityUtils utils;

    @Transactional
    public ItemDto create(ItemDto itemDto, long userId) {
        log.info("Created item with id={} from user with id={}", itemDto, userId);
        return toItemDto(itemRepository.save(toItem(itemDto, utils.getUserIfExists(userId))));
    }

    @Transactional
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {
        var updatedItem = utils.getItemIfExists(itemId);
        if (updatedItem.getOwner() != null && updatedItem.getOwner().getId() != userId) {
            throw new UserOwnershipException("User with id=" + userId +
                    " is not the owner of the item with id=" + itemId);
        }

        Optional.ofNullable(itemDto.getName()).ifPresent(updatedItem::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(updatedItem::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(updatedItem::setAvailable);

        updatedItem.setOwner(utils.getUserIfExists(userId));
        itemRepository.save(updatedItem);

        log.info("Item with id={} updated by user with id={}", itemId, userId);
        return toItemDto(updatedItem);
    }

    @Transactional(readOnly = true)
    public ItemDto findById(long itemId, long userId) {
        var item = utils.getItemIfExists(itemId);
        var itemDto = ItemMapper.toItemDto(item);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            addBookings(itemDto);
        }

        addCommentsDto(itemDto);
        log.info("Get item by id={}", itemId);
        return itemDto;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllByUserId(long userId) {
        log.info("Get all items by user with id={}", userId);
        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .map(this::addCommentsDto)
                .map(this::addBookings)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemDto> searchByText(String text) {
        log.info("Get list by search by text='{}'", text);
        return text.isEmpty() ? Collections.emptyList() :
                itemRepository.findAll().stream()
                        .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase())
                                && item.getAvailable())
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(long itemId, long userId, CommentDto commentDto) {
        if (isBookingExists(itemId, userId)) {
            throw new BadRequestException("User with id=" + userId + " never booked item with id=" + itemId);
        }

        var comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(utils.getUserIfExists(userId));
        comment.setItem(utils.getItemIfExists(itemId));
        comment.setCreated(LocalDateTime.now());

        log.info("Comment from user with id={} to item with id={} added", userId, itemId);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    /**
     * Метод проверяет наличие бронирования вещи пользователем,
     * создан для увеличения читаемости кода
     */
    private boolean isBookingExists(long itemId, long userId) {
        return bookingRepository.findAllByItemIdAndBookerId(itemId, userId).stream()
                .noneMatch(booking -> booking.getStatus() == Status.APPROVED
                        && booking.getStart().isBefore(LocalDateTime.now()));
    }

    private ItemDto addCommentsDto(ItemDto itemDto) {
        var comments = commentRepository.findAllByItemId(itemDto.getId());
        itemDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    private ItemDto addBookings(ItemDto itemDto) {
        var bookings = bookingRepository.findAllByItemId(itemDto.getId());

        var prevBooking = bookings.stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        var nextBooking = bookings.stream()
                .filter(b -> b.getStatus().equals(Status.APPROVED))
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        itemDto.setLastBooking(prevBooking != null ? ItemDto.NearByBooking.builder()
                .id(prevBooking.getId())
                .bookerId(prevBooking.getBooker().getId())
                .build() : null);

        itemDto.setNextBooking(nextBooking != null ? ItemDto.NearByBooking.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getBooker().getId())
                .build() : null);
        return itemDto;
    }
}
