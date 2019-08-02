package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class DamageDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageEvent event) {

        if (event.isCancelled()) return;

        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) return;

        if (MobCombatSettingsConfig.onlyShowDamageForEliteMobs) {

            if (EntityTracker.isEliteMob(event.getEntity()) && event.getEntity() instanceof LivingEntity) {
                if (event.getDamage() > 0)
                    displayDamage(event.getEntity(),
                            event.getFinalDamage() /
                                    ((LivingEntity) event.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() *
                                    EntityTracker.getEliteMobEntity(event.getEntity()).getMaxHealth());
            } else if (EntityTracker.isSuperMob(event.getEntity()))
                displayDamage(event.getEntity(), event.getFinalDamage());

        } else if (event.getDamage() > 0) displayDamage(event.getEntity(), event.getFinalDamage());


    }

    public static void displayDamage(Entity entity, double damage) {

        if (!MobCombatSettingsConfig.displayDamageOnHit) return;

        Location entityLocation = entity.getLocation();

        Random random = new Random();
        double randomCoordX = (random.nextDouble() * 2) - 1 + entityLocation.getX();
        double randomCoordZ = (random.nextDouble() * 2) - 1 + entityLocation.getZ();

        Location newLocation = new Location(entityLocation.getWorld(), randomCoordX, entityLocation.getY() + 1.7, randomCoordZ);

         /*
        Dirty fix: armorstands don't render invisibly on their first tick, so it gets moved elsewhere temporarily
         */
        ArmorStand armorStand = (ArmorStand) newLocation.getWorld().spawnEntity(newLocation.add(new Vector(0, -50, 0)), EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setMarker(true);
        int newDisplayDamage = (int) damage;
        armorStand.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + "" + newDisplayDamage + "");
        armorStand.setGravity(false);
        EntityTracker.registerArmorStands(armorStand);
        armorStand.setCustomNameVisible(false);

        new BukkitRunnable() {

            int taskTimer = 0;

            @Override
            public void run() {

                if (taskTimer == 0) {
                    armorStand.teleport(new Location(armorStand.getWorld(), armorStand.getLocation().getX(),
                            armorStand.getLocation().getY() + 50, armorStand.getLocation().getZ()));
                } else
                    armorStand.teleport(new Location(armorStand.getWorld(), armorStand.getLocation().getX(),
                            armorStand.getLocation().getY() + 0.1, armorStand.getLocation().getZ()));

                if (taskTimer == 1)
                    armorStand.setCustomNameVisible(true);

                taskTimer++;

                if (taskTimer > 15) {

                    EntityTracker.unregisterArmorStand(armorStand);
                    cancel();

                }

            }

        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 0, 1);

    }

}
