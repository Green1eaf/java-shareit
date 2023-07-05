package ru.practicum.shareit.user.dto;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> storage = new HashMap<>();
    private long counter = 0;

    @Override
    public User create(User user) {
        if (user.getId() == null) {
            user.setId(++counter);
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(long id) {
        return storage.get(id);
    }

    @Override
    public User update(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }
}
