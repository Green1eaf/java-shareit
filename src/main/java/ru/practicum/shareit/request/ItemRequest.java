package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.shareit.AbstractBaseEntity;
import ru.practicum.shareit.user.model.User;

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
@Table(name = "requests")
public class ItemRequest extends AbstractBaseEntity {

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "requestor_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @NotNull
    private User requestor;

    @Column(name = "start_date")
    private LocalDateTime created;
}
