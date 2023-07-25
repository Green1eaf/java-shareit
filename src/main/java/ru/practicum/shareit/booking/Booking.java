package ru.practicum.shareit.booking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.shareit.AbstractBaseEntity;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "bookings")
public class Booking extends AbstractBaseEntity {

    @Column(name = "start_date", nullable = false)
    @NotNull
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    @NotNull
    private LocalDateTime end;

    @OneToOne
    @JoinColumn(name = "item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @NotNull
    private Item item;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @NotNull
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
