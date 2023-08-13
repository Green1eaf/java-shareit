package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.util.EntityUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@ExtendWith(MockitoExtension.class)
public class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityUtils entityUtils;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void createUser() {
        var userDto = new UserDto();
        var user = User.builder().id(1L).build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        var createdUser = userService.create(userDto);
        assertEquals(user.getId(), createdUser.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void findByIdUser() {
        var userDto = UserDto.builder()
                .name("test")
                .email("ya@ya.com")
                .build();
        var user = User.builder()
                .id(1L)
                .name("test")
                .email("ya@ya.com")
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);

        var findUser = userService.findById(1L);
        userDto.setId(1L);
        assertNotNull(findUser);
        assertEquals(userDto, findUser);
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
    }

    @Test
    public void findByIdAndNotExist() {
        when(entityUtils.getUserIfExists(anyLong())).thenThrow(new NotExistException("User with id=" + 1L + " not exists"));
        var exception = assertThrows(NotExistException.class, () -> userService.findById(1L));
        assertEquals("User with id=" + 1L + " not exists", exception.getMessage());
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
    }

    @Test
    public void updateNameNullAndEmailNull() {
        var userDto = new UserDto();
        var updatedUser = User.builder()
                .id(1L)
                .name("name")
                .email("ya@ya.com")
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(updatedUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        assertEquals(toUserDto(updatedUser), userService.update(userDto, 1L));
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateNameNonNullAndEmailNull() {
        var userDto = UserDto.builder()
                .name("test")
                .build();
        var user = User.builder()
                .id(1L)
                .name("name")
                .email("ya@ya.com")
                .build();
        var updatedUser = User.builder()
                .id(user.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        assertEquals(toUserDto(updatedUser), userService.update(userDto, 1L));
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateNameNullAndEmailNonNullAndNotDuplicate() {
        var userDto = UserDto.builder()
                .email("test@ya.com")
                .build();
        var user = User.builder()
                .id(1L)
                .name("name")
                .email("ya@ya.com")
                .build();
        var updatedUser = User.builder()
                .id(user.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        assertEquals(toUserDto(updatedUser), userService.update(userDto, 1L));
        verify(entityUtils, times(1)).getUserIfExists(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateNameNullAndEmailDuplicate() {
        var userDto = UserDto.builder()
                .email("test@ya.com")
                .build();
        var user = new User();
        var sameUser = User.builder()
                .id(2L)
                .name("name")
                .email("test@ya.com")
                .build();
        when(entityUtils.getUserIfExists(anyLong())).thenReturn(user);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sameUser));
        assertThrows(AlreadyExistsException.class, () -> userService.update(userDto, 1L));
    }

    @Test
    public void deleteById() {
        long id = 1L;
        userService.deleteById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    public void findAll() {
        var userOne = User.builder()
                .id(1L)
                .name("user1")
                .email("ya@ya.com")
                .build();
        var userTwo = User.builder()
                .id(2L)
                .name("user2")
                .email("test@ya.com")
                .build();
        var userFromRepository = List.of(userOne, userTwo);
        when(userRepository.findAll()).thenReturn(userFromRepository);
        assertArrayEquals(userFromRepository.stream()
                .map(UserMapper::toUserDto).toArray(), userService.findAll().toArray());
        verify(userRepository, times(1)).findAll();
    }

}
