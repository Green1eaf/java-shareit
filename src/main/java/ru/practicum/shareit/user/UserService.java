package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotExistException;
import ru.practicum.shareit.user.dto.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserStorage storage;

    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public User create(User user) {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new AlreadyExistsException("User with email:" + user.getEmail() + " already exists");
        }
        return storage.create(user);
    }

    public User findById(long id) {
        return Optional.ofNullable(storage.findById(id))
                .orElseThrow(() -> new NotExistException("User with id=" + id + " not exists"));
    }

    public User update(User user, long userId) {

        var updatedUser = findById(userId);
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            var otherUser = findByEmail(user.getEmail()).orElse(null);
            if (otherUser != null && otherUser.getEmail().equals(user.getEmail()) && otherUser.getId() != userId) {
                throw new AlreadyExistsException("User with email=" + user.getEmail() + " already exists");
            }
            updatedUser.setEmail(user.getEmail());
        }
        return storage.update(updatedUser);
    }

    public void deleteById(long id) {
        storage.deleteById(id);
    }

    public List<User> findAll() {
        return storage.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }
}
