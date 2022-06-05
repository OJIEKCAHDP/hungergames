package ru.stonlex.bukkit.inventory.handler;

import lombok.NonNull;
import ru.stonlex.bukkit.inventory.BaseInventory;
import ru.stonlex.bukkit.utility.WeakObjectCache;

public interface BaseInventoryHandler {

    void handle(@NonNull BaseInventory baseInventory, WeakObjectCache objectCache);
}
