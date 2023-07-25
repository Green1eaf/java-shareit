package ru.practicum.shareit.comment;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.shareit.AbstractBaseEntity;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "comments")
public class Comment extends AbstractBaseEntity {

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "start_date")
    private LocalDateTime date;
}
