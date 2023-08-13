package ru.practicum.shareit.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntityUtilTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private EntityUtils entityUtils;

    @Test
    public void getUserNotExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        var exception = assertThrows(NotExistException.class, () -> entityUtils.getUserIfExists(1L));
        assertEquals("User with id=1 not exists", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getUserExists() {
        var user = User.builder()
                .id(1L)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        assertEquals(User.builder().id(1L).build(), entityUtils.getUserIfExists(1L));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getItemNotExists() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        var exception = assertThrows(NotExistException.class, () -> entityUtils.getItemIfExists(1L));
        assertEquals("Item with id=1 not exists", exception.getMessage());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getItemExists() {
        var item = Item.builder()
                .id(1L)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        assertEquals(Item.builder().id(1L).build(), entityUtils.getItemIfExists(1L));
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getBookingNotExists() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        var exception = assertThrows(NotExistException.class, () -> entityUtils.getBookingIfExists(1L));
        assertEquals("Booking with id=1 not exists", exception.getMessage());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    public void betBookingExists() {
        var booking = Booking.builder()
                .id(1L)
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        assertEquals(Booking.builder().id(1L).build(), entityUtils.getBookingIfExists(1L));
        verify(bookingRepository, times(1)).findById(anyLong());
    }
}
