package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingDto {
    private Long id;

    @Future
    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;

    private Item item;

    private Status status;

    private User booker;
}
