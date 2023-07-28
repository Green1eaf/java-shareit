package ru.practicum.shareit.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.AbstractNamedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
    @Email
    @NotBlank
    @Size(max = 128)
    private String email;
}
