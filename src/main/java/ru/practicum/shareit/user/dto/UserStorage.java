package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User findById(long id);

    User update(User user);

    void deleteById(long id);

    List<User> findAll();
}
