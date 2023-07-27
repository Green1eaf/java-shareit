package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.EntityUtils;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final EntityUtils utils;

    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("User created");
        return toUserDto(repository.save(toUser(userDto)));
    }

    @Transactional(readOnly = true)
    public UserDto findById(long id) {
        log.info("Get user by id={}", id);
        return UserMapper.toUserDto(utils.getUserIfExists(id));
    }

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

    @Transactional
    public void deleteById(long id) {
        repository.deleteById(id);
        log.info("User with id={} deleted", id);
    }

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
