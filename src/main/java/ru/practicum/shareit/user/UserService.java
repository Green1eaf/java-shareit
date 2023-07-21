package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserDto create(UserDto userDto) {
        return toUserDto(repository.save(toUser(userDto)));
    }

    public UserDto findById(long id) {
        return repository.findById(id)
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
        return toUserDto(repository.save(toUser(updatedUser)));
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public List<UserDto> findAll() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(UserMapper::toUserDto);
    }

    private void checkForDuplicateEmail(String email, long userId) {
        var otherUser = findByEmail(email).orElse(null);
        if (otherUser != null && otherUser.getEmail().equals(email) && otherUser.getId() != userId) {
            throw new AlreadyExistsException("User with email=" + email + " already exists");
        }
    }
}
