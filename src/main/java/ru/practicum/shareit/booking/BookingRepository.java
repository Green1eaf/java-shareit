package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(long userId);

    List<Booking> findAllByItem_OwnerId(long item_owner);

    List<Booking> findAllByItemIdAndItem_OwnerId(long itemId, long item_owner);
}
