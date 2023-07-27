package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(long userId);

    List<Booking> findAllByItem_OwnerId(long itemOwner);

    List<Booking> findAllByItemIdAndBookerId(long itemId, long bookerId);

    List<Booking> findAllByItemId(Long itemId);
}
