package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @ResponseBody
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("GATEWAY: POST /users is here");
        return userClient.create(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable long id) {
        log.info("GATEWAY: GET /users/{} is here", id);
        return userClient.findById(id);
    }

    @ResponseBody
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@RequestBody UserDto userDto, @PathVariable long userId) {
        log.info("GATEWAY: PATCH /users/{} is here for update user", userId);
        return userClient.update(userDto, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable long id) {
        log.info("GATEWAY: DELETE /users/{} is here", id);
        return userClient.deleteById(id);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("GATEWAY: GET /users is here for get all users");
        return userClient.findAll();
    }

}
