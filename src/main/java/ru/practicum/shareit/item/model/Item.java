package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotBlank
    @NotNull
    private String description;

    @NotNull
    private Boolean available;

    private User owner;

    private ItemRequest request;
}
