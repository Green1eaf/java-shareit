package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public Item create(@RequestBody @Valid Item item, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody Item item, @PathVariable long itemId,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.update(item, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public Item findById(@PathVariable long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<Item> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findAllByUserId(userId);
    }

    @GetMapping("/search")
    public List<Item> findByText(@RequestParam(required = false, name = "text") String text) {
        return itemService.searchByText(text);
    }
}
