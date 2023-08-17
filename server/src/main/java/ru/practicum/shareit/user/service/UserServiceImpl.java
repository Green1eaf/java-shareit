package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.EntityUtils;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final EntityUtils utils;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("User created");
        return toUserDto(repository.save(toUser(userDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(long id) {
        log.info("Get user by id={}", id);
        return UserMapper.toUserDto(utils.getUserIfExists(id));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, long userId) {
        var updatedUser = utils.getUserIfExists(userId);

        if (userDto.getName() != null) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            checkForDuplicateEmail(userDto.getEmail(), userId);
            updatedUser.setEmail(userDto.getEmail());
        }
        log.info("User with id={} updated", userId);
        return toUserDto(repository.save(updatedUser));
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        repository.deleteById(id);
        log.info("User with id={} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.info("Get all users");
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void checkForDuplicateEmail(String email, long userId) {
        var otherUser = repository.findByEmail(email)
                .map(UserMapper::toUserDto)
                .orElse(null);
        if (otherUser != null && otherUser.getEmail().equals(email) && otherUser.getId() != userId) {
            throw new AlreadyExistsException("User with email=" + email + " already exists");
        }
    }
}
