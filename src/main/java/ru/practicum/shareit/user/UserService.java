package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public UserDto create(UserDto userDto) {
        if (findByEmail(userDto.getEmail()).isPresent()) {
            throw new AlreadyExistsException("User with email:" + userDto.getEmail() + " already exists");
        }
        return toUserDto(storage.create(toUser(userDto)));
    }

    public UserDto findById(long id) {
        return Optional.ofNullable(storage.findById(id))
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotExistException("User with id=" + id + " not exists"));
    }

    public UserDto update(UserDto userDto, long userId) {
        var updatedUser = findById(userId);
        if (userDto.getName() != null) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            checkForDuplicateEmail(userDto.getEmail(), userId);
            updatedUser.setEmail(userDto.getEmail());
        }
        return toUserDto(storage.update(toUser(updatedUser)));
    }

    public void deleteById(long id) {
        storage.deleteById(id);
    }

    public List<UserDto> findAll() {
        return storage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    private void checkForDuplicateEmail(String email, long userId) {
        var otherUser = findByEmail(email).orElse(null);
        if (otherUser != null && otherUser.getEmail().equals(email) && otherUser.getId() != userId) {
            throw new AlreadyExistsException("User with email=" + email + " already exists");
        }
    }
}
