package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage storage;

    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public User create(User user) {
        return storage.create(user);
    }

    public User findById(long id) {
        return storage.findById(id);
    }

    public User update(User user) {
        return storage.update(user);
    }

    public void deleteById(long id) {
        storage.deleteById(id);
    }

    public List<User> findAll() {
        return storage.findAll();
    }
}
