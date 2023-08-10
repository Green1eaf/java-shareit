package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.EntityUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    public void createWithItemNotAvailable() {
        var item = Item.builder()
                .available(false)
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var exception = assertThrows(NotAvailableException.class,
                () -> bookingService.create(BookingDto.builder().itemId(1L).build(), 1L));
        assertEquals("Item is not available", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void createWithStartNull() {
        var bookingDto = BookingDto.builder()
                .itemId(1L)
                .end(LocalDateTime.now())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(Item.builder().available(true).build());
        var exception = assertThrows(BadRequestException.class, () -> bookingService.create(bookingDto, 1L));
        assertEquals("Date can't be a null", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void createWithEndNull() {
        var bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(Item.builder().available(true).build());
        var exception = assertThrows(BadRequestException.class, () -> bookingService.create(bookingDto, 1L));
        assertEquals("Date can't be a null", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void createWithStartAfterEnd() {
        var bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2020, 2, 1, 1, 1))
                .end(LocalDateTime.of(2020, 1, 1, 1, 1))
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(Item.builder().available(true).build());
        var exception = assertThrows(BadRequestException.class, () -> bookingService.create(bookingDto, 1L));
        assertEquals("Start date is after or equals to end date", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void createWithStartEqualsEnd() {
        var bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 1, 1, 1, 1))
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(Item.builder().available(true).build());
        var exception = assertThrows(BadRequestException.class, () -> bookingService.create(bookingDto, 1L));
        assertEquals("Start date is after or equals to end date", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void createByItemOwner() {
        var bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plus(1, ChronoUnit.HOURS))
                .end(LocalDateTime.now().plus(2, ChronoUnit.HOURS))
                .build();
        var item = Item.builder()
                .available(true)
                .owner(User.builder().id(1L).build())
                .build();
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var exception = assertThrows(NotExistException.class, () -> bookingService.create(bookingDto, 1L));
        assertEquals("Owner can't booked his own item", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void create() {
        var item = Item.builder()
                .available(true)
                .owner(User.builder().id(1L).build())
                .build();
        var user = User.builder().id(2L).build();
        var booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().plus(1, ChronoUnit.HOURS))
                .end(LocalDateTime.now().plus(2, ChronoUnit.HOURS))
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        var bookingDto = BookingMapper.toBookingDto(booking);
        bookingDto.setItemId(1L);
        var resultBookingDto = bookingService.create(bookingDto, 2L);
        booking.setId(1L);
        resultBookingDto.setId(1L);
        assertEquals(BookingMapper.toBookingDto(booking), resultBookingDto);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void updateStatusAlreadyApproved() {
        var booking = Booking.builder()
                .status(Status.APPROVED)
                .build();
        when(entityUtils.getBookingIfExists(anyLong())).thenReturn(booking);
        var exception = assertThrows(BadRequestException.class, () -> bookingService.updateStatus(1L, 1L, true));
        assertEquals("Booking is already approved", exception.getMessage());
        verify(entityUtils, times(1)).getBookingIfExists(anyLong());
    }

    @Test
    public void updateStatusWithApproveByOnlyItemOwner() {
        var item = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        var booking = Booking.builder()
                .status(Status.REJECTED)
                .item(item)
                .build();
        when(entityUtils.getBookingIfExists(anyLong())).thenReturn(booking);
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        var exception = assertThrows(NotExistException.class, () -> bookingService.updateStatus(2L, 1L, false));
        assertEquals("User with id=2 is not the owner", exception.getMessage());
        verify(entityUtils, times(1)).getBookingIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
    }

    @Test
    public void updateStatus() {
        var item = Item.builder()
                .id(1L)
                .owner(User.builder().id(1L).build())
                .build();
        var booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 2, 1, 1, 1))
                .status(Status.REJECTED)
                .item(item)
                .booker(User.builder().id(2L).build())
                .build();
        when(entityUtils.getBookingIfExists(anyLong())).thenReturn(booking);
        when(entityUtils.getItemIfExists(anyLong())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        var actualDto = bookingService.updateStatus(1L, 1L, true);
        assertEquals(Status.APPROVED, actualDto.getStatus());
        verify(entityUtils, times(1)).getBookingIfExists(anyLong());
        verify(entityUtils, times(1)).getItemIfExists(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void findByIdNotAvailableForView() {
        var booking = Booking.builder()
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();
        when(entityUtils.getBookingIfExists(anyLong())).thenReturn(booking);
        var exception = assertThrows(NotExistException.class, () -> bookingService.findById(1L, 3L));
        assertEquals("Booking with id=1 not available for view", exception.getMessage());
        verify(entityUtils, times(1)).getBookingIfExists(anyLong());
    }

    @Test
    public void findById() {
        var booking = Booking.builder()
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 2, 1, 1, 1))
                .status(Status.APPROVED)
                .build();
        when(entityUtils.getBookingIfExists(anyLong())).thenReturn(booking);
        assertEquals(BookingMapper.toBookingDto(booking), bookingService.findById(1L, 1L));
        verify(entityUtils, times(1)).getBookingIfExists(anyLong());
    }

    @Test
    public void findByBookerAndState() {
        var booking1 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1L).build())
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 1, 2, 1, 1))
                .build();
        var booking2 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1l).build())
                .start(LocalDateTime.of(2021, 1, 1, 1, 1))
                .end(LocalDateTime.of(2021, 1, 2, 1, 1))
                .build();
        var booking3 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1L).build())
                .start(LocalDateTime.of(2024, 1, 1, 1, 1))
                .end(LocalDateTime.of(2024, 1, 2, 1, 1))
                .build();
        when(bookingRepository.findAllByBookerId(anyLong())).thenReturn(List.of(booking1, booking3, booking2));
        var expected = List.of(BookingMapper.toBookingDto(booking2), BookingMapper.toBookingDto(booking1)).toArray();
        var actual = bookingService.findByBookerAndState(1L, "PAST").toArray();
        assertArrayEquals(expected, actual);
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerId(anyLong());
    }

    @Test
    public void findAllByOwnerAndState() {
        var booking1 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1L).build())
                .start(LocalDateTime.of(2020, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 1, 2, 1, 1))
                .build();
        var booking2 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1l).build())
                .start(LocalDateTime.of(2021, 1, 1, 1, 1))
                .end(LocalDateTime.of(2021, 1, 2, 1, 1))
                .build();
        var booking3 = Booking.builder()
                .booker(User.builder().id(1L).build())
                .status(Status.APPROVED)
                .item(Item.builder().id(1L).build())
                .start(LocalDateTime.of(2024, 1, 1, 1, 1))
                .end(LocalDateTime.of(2024, 1, 2, 1, 1))
                .build();
        when(bookingRepository.findAllByItem_OwnerId(anyLong())).thenReturn(List.of(booking1, booking3, booking2));
        var expected = List.of(BookingMapper.toBookingDto(booking2), BookingMapper.toBookingDto(booking1)).toArray();
        var actual = bookingService.findAllItemsByOwnerAndState(1L, "PAST").toArray();
        assertArrayEquals(expected, actual);
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(bookingRepository, times(1)).findAllByItem_OwnerId(anyLong());
    }
}
