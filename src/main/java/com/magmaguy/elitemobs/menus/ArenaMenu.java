package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.menus.premade.ArenaMenuConfig;
import com.magmaguy.elitemobs.instanced.ArenaInstance;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class ArenaMenu {
    public void constructArenaMenu(Player player, String arenaFilename) {
        ArenaInstance arenaInstance = ArenaInstance.getArenaInstances().get(arenaFilename);
        if (arenaFilename == null) {
            player.sendMessage(ChatColorConverter.convert("&4[EliteMobs] &cInvalid arena name!"));
            return;
        }
        Inventory shopInventory = Bukkit.createInventory(player, 9, arenaInstance.getCustomArenasConfigFields().getArenaName());
        shopInventory.setItem(ArenaMenuConfig.getPlayerItemSlot(), ArenaMenuConfig.getPlayerItem());
        shopInventory.setItem(ArenaMenuConfig.getSpectatorItemSlot(), ArenaMenuConfig.getSpectatorItem());
        player.openInventory(shopInventory);
        ArenaMenuEvents.menus.put(player, new MenuContainer(shopInventory, arenaInstance));
    }

    public static class ArenaMenuEvents implements Listener {
        private static final Map<Player, MenuContainer> menus = new HashMap<>();

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!menus.containsKey(player)) return;
            event.setCancelled(true);
            if (event.getSlot() == ArenaMenuConfig.getPlayerItemSlot()) {
                menus.get(player).getArenaInstance().addPlayer(player);
                player.closeInventory();
                return;
            }
//            if (event.getSlot() == ArenaMenuConfig.getSpectatorItemSlot()) {
//                menus.get(player).getArenaInstance().addSpectator(player);
//                player.closeInventory();
//                return;
//            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            menus.remove(event.getPlayer());
        }
    }

    private class MenuContainer {
        @Getter
        private Inventory inventory;
        @Getter
        private ArenaInstance arenaInstance;

        private MenuContainer(Inventory inventory, ArenaInstance arenaInstance) {
            this.inventory = inventory;
            this.arenaInstance = arenaInstance;
        }
    }
}
