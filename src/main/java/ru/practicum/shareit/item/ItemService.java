package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ItemDto create(ItemDto itemDto, long userId) {

        return toItemDto(itemRepository.save(toItem(itemDto, toUser(checkUserExist(userId)))));
    }

    @Transactional
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {

        var updatedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotExistException("Item with id=" + itemId + " not exists"));
        if (updatedItem.getOwner() != null && updatedItem.getOwner().getId() != userId) {
            throw new UserOwnershipException("User with id=" + userId +
                    " is not the owner of the item with id=" + itemId);
        }
        if (itemDto.getName() != null) {
            updatedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updatedItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }
        updatedItem.setOwner(toUser(checkUserExist(userId)));
        itemRepository.save(updatedItem);

        return toItemDto(updatedItem);
    }

    @Transactional(readOnly = true)
    public ItemDto findById(long itemId, long userId) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotExistException("Item with id=" + itemId + " not exists"));
        var itemDto = ItemMapper.toItemDto(item);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            addBookings(itemDto);
        }

        addCommentsDto(itemDto);
        return itemDto;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllByUserId(long userId) {
        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .map(this::addCommentsDto)
                .map(this::addBookings)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    private ItemDto addCommentsDto(ItemDto itemDto) {
        var comments = commentRepository.findAllByItemId(itemDto.getId());
        itemDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> searchByText(String text) {
        return text.isEmpty() ? Collections.emptyList() :
                findAllItemDtoByFilter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable());
    }

    private List<ItemDto> findAllItemDtoByFilter(Predicate<Item> filter) {
        return itemRepository.findAll().stream()
                .filter(filter)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private UserDto checkUserExist(long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotExistException("User with id=" + userId + " not exists"));
    }

    @Transactional
    public CommentDto addComment(long itemId, long userId, CommentDto commentDto) {
        var bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, userId).stream()
                .filter(booking -> booking.getStatus().equals(Status.APPROVED)
                && booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new BadRequestException("User with id=" + userId + " never booked item with id=" + itemId);
        }
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotExistException("Item with id=" + itemId + " not exists"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotExistException("User with id=" + userId + " not exitst"));
        var comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        var savedComment = commentRepository.save(comment);
        var savedCommentDto = CommentMapper.toCommentDto(savedComment);
        return savedCommentDto;
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
