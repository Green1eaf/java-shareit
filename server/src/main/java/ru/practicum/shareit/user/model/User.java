package ru.practicum.shareit.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.AbstractNamedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends AbstractNamedEntity {

    @Column(name = "email", unique = true)
    private String email;
}
