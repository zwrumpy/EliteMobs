package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class CustomTreasureChestConfigFields extends CustomConfigFields {

    @Getter
    private Material chestMaterial = Material.CHEST;
    @Getter
    private BlockFace facing = BlockFace.NORTH;
    @Getter
    private int chestTier = 0;
    @Getter
    private TreasureChest.DropStyle dropStyle = TreasureChest.DropStyle.SINGLE;
    @Getter
    private int restockTimer = 0;
    @Getter
    private List<String> lootList = new ArrayList<>();
    @Getter
    private double mimicChance = 0;
    @Getter
    private List<String> mimicCustomBossesList = new ArrayList<>();
    @Getter
    private List<String> restockTimers = new ArrayList<>();
    @Getter
    private List<String> effects = new ArrayList<>();
    @Getter
    private String worldName;
    @Getter
    private Location location;
    @Getter
    private long restockTime = 0L;
    @Getter
    private List<String> locations = new ArrayList<>();
    @Getter
    private CustomLootTable customLootTable = null;


    public CustomTreasureChestConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomTreasureChestConfigFields(String fileName,
                                           boolean isEnabled,
                                           Material chestMaterial,
                                           BlockFace facing,
                                           int chestTier,
                                           Location location,
                                           TreasureChest.DropStyle dropStyle,
                                           int restockTimer,
                                           List<String> lootList,
                                           double mimicChance,
                                           List<String> mimicCustomBossesList,
                                           long restockTime,
                                           List<String> restockTimers,
                                           List<String> effects) {

        super(fileName, isEnabled);
        this.chestMaterial = chestMaterial;
        this.facing = facing;
        this.chestTier = chestTier;
        this.location = location;
        this.dropStyle = dropStyle;
        this.restockTimer = restockTimer;
        this.lootList = lootList;
        this.mimicChance = mimicChance;
        this.mimicCustomBossesList = mimicCustomBossesList;
        this.restockTime = restockTime;
        this.restockTimers = restockTimers;
        this.effects = effects;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, false, false);
        this.chestMaterial = processEnum("chestType", chestMaterial, Material.CHEST, Material.class, true);
        this.facing = processEnum("facing", facing, BlockFace.NORTH, BlockFace.class, true);
        this.chestTier = processInt("chestTier", chestTier, 0, true);
        this.worldName = processString("location", worldName, null, false);
        if (worldName != null)
            worldName = worldName.split(",")[0];
        this.dropStyle = processEnum("dropStyle", dropStyle, TreasureChest.DropStyle.SINGLE, TreasureChest.DropStyle.class, true);
        this.restockTimer = processInt("restockTimer", restockTimer, 0, true);
        this.lootList = processStringList("lootList", lootList, new ArrayList<>(), true);
        this.customLootTable = new CustomLootTable(this);
        this.mimicChance = processDouble("mimicChance", mimicChance, 0, true);
        this.mimicCustomBossesList = processStringList("mimicCustomBossesList", mimicCustomBossesList, new ArrayList<>(), true);
        this.restockTime = processLong("restockTime", restockTime, 0, false);
        this.restockTimers = processStringList("restockTimers", restockTimers, new ArrayList<>(), false);
        this.effects = processStringList("effects", effects, new ArrayList<>(), false);
        this.locations = processStringList("locations", locations, new ArrayList<>(), false);
        this.location = processLocation("location", location, null, false);
        if (location == null && locations.isEmpty())
            new InfoMessage("Custom Treasure Chest in file " + filename + " does not have a defined location(s)! It will not spawn.");
        else
            new TreasureChest(this, location, restockTime);
        for (String string : locations) {
            String[] strings = string.split(":");
            Location location = ConfigurationLocation.serialize(strings[0]);
            if (location == null) {
                new WarningMessage("Bad location entry in locations for " + filename + " . Entry: " + strings[0]);
                continue;
            }
            long timestamp = 0;
            if (strings.length > 1) {
                try {
                    timestamp = Long.parseLong(strings[1]);
                } catch (Exception exception) {
                    new WarningMessage("Bad unix timestamp in locations for " + filename + " . Entry: " + strings[0]);
                }
            }
            new TreasureChest(this, location, timestamp);
        }
    }

    /**
     * For the new format of Treasure chests, which have multiple locations per file
     * @param chestInstanceLocation
     * @param unixTimeStamp
     * @return
     */
    public TreasureChest updateTreasureChest(Location chestInstanceLocation, long unixTimeStamp) {
        int index = -1;
        String deserializedLocation = ConfigurationLocation.deserialize(chestInstanceLocation.getBlock().getLocation());
        for (String string : locations)
            if (string.split(":")[0].equals(deserializedLocation)) {
                index = locations.indexOf(string);
                break;
            }
        String serializedUpdatedLocation = deserializedLocation + ":" + unixTimeStamp;
        TreasureChest treasureChest = null;
        if (index != -1) {
            //case for existing treasure chest getting a cooldown
            locations.set(index, serializedUpdatedLocation);
        } else {
            //case for a new treasure chest
            locations.add(serializedUpdatedLocation);
            treasureChest = new TreasureChest(this, chestInstanceLocation, unixTimeStamp);
        }
        fileConfiguration.set("locations", locations);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        return treasureChest;
    }

    public void removeTreasureChest(Location chestInstanceLocation) {
        if (locations.isEmpty()) return;
        int index = -1;
        String deserializedLocation = ConfigurationLocation.deserialize(chestInstanceLocation.getBlock().getLocation());
        for (String string : locations)
            if (string.split(":")[0].equals(deserializedLocation)) {
                index = locations.indexOf(string);
                break;
            }
        if (index < 0)
            return;
        String location = locations.get(index);
        locations.remove(location);
        fileConfiguration.set("locations", locations);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void setRestockTime(Location location, long newRestockTime) {
        if (!locations.isEmpty()) {
            updateTreasureChest(location, newRestockTime);
            return;
        }

        this.restockTime = newRestockTime;
        this.fileConfiguration.set("restockTime", newRestockTime);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update restock time for a custom treasure chest and failed, did you delete it during runtime?");
        }
    }

}
