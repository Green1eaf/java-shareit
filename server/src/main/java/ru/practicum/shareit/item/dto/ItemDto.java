package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private NearByBooking lastBooking;
    private NearByBooking nextBooking;
    private Long requestId;
    private List<CommentDto> comments;

    @Data
    @Builder
    public static class NearByBooking {
        private Long id;
        private Long bookerId;
    }
}
