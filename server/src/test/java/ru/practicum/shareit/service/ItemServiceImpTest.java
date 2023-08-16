package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.exception.UserOwnershipException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.EntityUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImpTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void createItem() {
        var itemDto = new ItemDto();
        var item = Item.builder().id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        var createdItem = itemService.create(itemDto, item.getOwner().getId());
        assertEquals(ItemMapper.toItem(createdItem, item.getOwner()), item);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void updateNotExistsItem() {
        when(entityUtils.getItemIfExists(anyLong())).thenThrow(new NotExistException("Item with id=" + 1L + " not exists"));
        var exception = assertThrows(NotExistException.class, () -> itemService.update(new ItemDto(), 1L, 1L));
        assertEquals("Item with id=1 not exists", exception.getMessage());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void updateItemByNotItemOwner() {
        var updatedItem = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(updatedItem);
        assertThrows(UserOwnershipException.class, () -> itemService.update(new ItemDto(), 1L, 2L));
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void updateItemName() {
        var updatedItem = Item.builder()
                .owner(User.builder().id(1L).build())
                .name("name")
                .build();
        var updatedDto = ItemDto.builder()
                .name("test")
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(updatedItem);
        var resultItem = itemService.update(updatedDto, 1L, 1L);
        assertEquals(updatedDto.getName(), resultItem.getName());
        assertEquals(updatedDto, resultItem);
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void updateItemDescription() {
        var updatedItem = Item.builder()
                .owner(User.builder().id(1L).build())
                .description("desc")
                .build();
        var updatedDto = ItemDto.builder()
                .description("test")
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(updatedItem);
        var resultItem = itemService.update(updatedDto, 1L, 1L);
        assertEquals(updatedDto.getDescription(), resultItem.getDescription());
        assertEquals(updatedDto, resultItem);
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void updateItemAvailable() {
        var updatedItem = Item.builder()
                .owner(User.builder().id(1L).build())
                .available(false)
                .build();
        var updatedDto = ItemDto.builder()
                .available(true)
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(updatedItem);
        var resultItem = itemService.update(updatedDto, 1L, 1L);
        assertEquals(updatedDto.getAvailable(), resultItem.getAvailable());
        assertEquals(updatedDto, resultItem);
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void findByIdWIthOutBookings() {
        var item = Item.builder()
                .owner(User.builder().id(1L).build())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var resultItem = itemService.findById(1L, 2L);
        assertNull(resultItem.getLastBooking());
        assertNull(resultItem.getNextBooking());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void findByIdWithBookings() {
        var prevBooking = Booking.builder()
                .start(LocalDateTime.now().minus(2, ChronoUnit.HOURS))
                .end(LocalDateTime.now().minus(1, ChronoUnit.HOURS))
                .status(Status.APPROVED)
                .id(1L)
                .booker(User.builder().id(1L).build())
                .build();
        var nextBooking = Booking.builder()
                .status(Status.APPROVED)
                .start(LocalDateTime.now().plus(1, ChronoUnit.HOURS))
                .end(LocalDateTime.now().plus(2, ChronoUnit.HOURS))
                .id(2L)
                .booker(User.builder().id(2L).build())
                .build();
        var item = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        when(bookingRepository.findAllByItemId(anyLong())).thenReturn(List.of(prevBooking, nextBooking));
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        ItemDto result = itemService.findById(1L, 1L);
        assertEquals(ItemDto.NearByBooking.builder()
                .id(1L)
                .bookerId(1L)
                .build(), result.getLastBooking());
        assertEquals(ItemDto.NearByBooking.builder()
                .id(2L)
                .bookerId(2L)
                .build(), result.getNextBooking());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
        verify(bookingRepository, times(1)).findAllByItemId(anyLong());
    }

    @Test
    public void findByIdWithComments() {
        var item = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var comment = Comment.builder()
                .id(1L)
                .text("text")
                .created(LocalDateTime.of(2020, 1, 1, 1, 1))
                .item(Item.builder().id(1L).build())
                .author(User.builder().id(1L).name("name").build())
                .build();
        var commentDto = CommentDto.builder()
                .id(1L)
                .authorName("name")
                .text("text")
                .created(LocalDateTime.of(2020, 1, 1, 1, 1))
                .build();
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(comment));
        var result = itemService.findById(1L, 1L);
        assertArrayEquals(List.of(commentDto).toArray(), result.getComments().toArray());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
        verify(commentRepository, times(1)).findAllByItemId(anyLong());
    }

    @Test
    public void findByIdWithOutComments() {
        var item = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var result = itemService.findById(1L, 1L);
        assertArrayEquals(Collections.emptyList().toArray(), result.getComments().toArray());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void findAllByUserId() {
        var item1 = Item.builder().id(1L).build();
        var item2 = Item.builder().id(2L).build();
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item1, item2));
        var result = itemService.findAllByUserId(1L);
        var itemDto1 = ItemMapper.toItemDto(item1);
        itemDto1.setComments(Collections.emptyList());
        var itemDto2 = ItemMapper.toItemDto(item2);
        itemDto2.setComments(Collections.emptyList());
        assertArrayEquals(List.of(itemDto1, itemDto2).toArray(), result.toArray());
        verify(itemRepository, times(1)).findAllByOwnerId(anyLong());
    }

    @Test
    public void searchByText() {
        var item1 = Item.builder()
                .description("text")
                .name("name")
                .available(true)
                .build();
        var item2 = Item.builder()
                .description("test")
                .name("name2")
                .available(true)
                .build();
        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
        var itemDtoList = itemService.searchByText("test");
        assertArrayEquals(List.of(ItemMapper.toItemDto(item2)).toArray(), itemDtoList.toArray());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    public void searchByEmptyText() {
        assertArrayEquals(Collections.emptyList().toArray(), itemService.searchByText("").toArray());
    }

    @Test
    public void addCommentWithOutBookingExists() {
        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        assertThrows(BadRequestException.class,
                () -> itemService.addComment(1L, 1L, CommentDto.builder().text("comment").build()));
    }

    @Test
    public void addComment() {
        var user = User.builder()
                .id(1L)
                .name("user")
                .email("ya@ya.com")
                .build();
        var item = Item.builder()
                .id(1L)
                .name("item")
                .description("desc")
                .owner(User.builder().id(2L).build())
                .available(true)
                .build();
        var booking = Booking.builder()
                .id(1L)
                .status(Status.APPROVED)
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 2, 1, 1, 1))
                .build();
        var comment = Comment.builder()
                .id(1L)
                .item(item)
                .author(user)
                .text("comment")
                .build();
        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of(booking));
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        var expected = CommentDto.builder()
                .id(1L)
                .text("comment")
                .authorName(user.getName())
                .build();
        var actual = itemService.addComment(1L, 1L, expected);
        assertEquals(expected, actual);
        verify(bookingRepository, times(1)).findAllByItemIdAndBookerId(anyLong(), anyLong());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}
