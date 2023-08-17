package ru.practicum.shareit.user.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    @Transactional
    UserDto create(UserDto userDto);

    @Transactional(readOnly = true)
    UserDto findById(long id);

    @Transactional
    UserDto update(UserDto userDto, long userId);

    @Transactional
    void deleteById(long id);

    @Transactional(readOnly = true)
    List<UserDto> findAll();
}
