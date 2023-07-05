package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public void create(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public void update(@RequestBody ItemDto itemDto, @PathVariable long itemId,
                       @RequestHeader("X-Sharer-User-Id") long userId) {
        itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public Item findById(@PathVariable long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<Item> findAll() {
        return itemService.findAll();
    }

    @GetMapping("/search")
    public List<Item> findByText(@RequestParam(required = false, name = "text") String text) {
        return itemService.searchByText(text);
    }
}
