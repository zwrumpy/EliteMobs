/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs;

/**
 * Created by MagmaGuy on 07/10/2016.
 */

import com.magmaguy.elitemobs.collateralminecraftchanges.ChunkUnloadMetadataPurge;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventCreeperPassiveEntityDamage;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventSpawnerMobEggInteraction;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.elitedrops.PotionEffectApplier;
import com.magmaguy.elitemobs.minorpowers.*;
import com.magmaguy.elitemobs.mobcustomizer.DamageHandler;
import com.magmaguy.elitemobs.mobs.aggressive.*;
import com.magmaguy.elitemobs.mobs.passive.*;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobMetadataAssigner;
import com.magmaguy.elitemobs.naturalmobspawner.NaturalMobSpawner;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class EliteMobs extends JavaPlugin implements Listener {

    public static List<World> worldList = new ArrayList();
    private int processID;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    @Override
    public void onEnable() {

        getLogger().info("EliteMobs - Enabled!");

        //Load loot from config
        loadConfiguration();
        initCustomConfig();

        //Parse loot
        EliteDropsHandler superDrops = new EliteDropsHandler(this);
        superDrops.superDropParser();

        //Get world list
        worldScanner();

        //Start the repeating tasks such as scanners
        repeatingTaskRunner();

        //Hook up all listeners, some depend on config
        this.getServer().getPluginManager().registerEvents(this, this);

        //Aggressive mobs
        this.getServer().getPluginManager().registerEvents(new ZombieHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new HuskHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SkeletonHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new WitherSkeletonHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new StrayHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PigZombieHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CreeperHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SpiderHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new EndermanHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CaveSpiderHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SilverfishHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new BlazeHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new WitchHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new EndermiteHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PolarBearHandler(this), this);

        //Passive mobs
        this.getServer().getPluginManager().registerEvents(new ChickenHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new CowHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new IronGolemHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new MushroomCowHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PigHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new SheepHandler(this), this);

        //Mob damage
        this.getServer().getPluginManager().registerEvents(new DamageHandler(this), this);

        //Minor mob powers
        this.getServer().getPluginManager().registerEvents(new AttackBlinding(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackConfusing(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackFire(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackFreeze(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackGravity(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackPush(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackWeb(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackPoison(this), this);
        this.getServer().getPluginManager().registerEvents(new AttackWither(this), this);
        this.getServer().getPluginManager().registerEvents(new BonusLoot(this), this);
        this.getServer().getPluginManager().registerEvents(new Invisibility(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityArrow(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFire(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityKnockback(this), this);
        this.getServer().getPluginManager().registerEvents(new InvulnerabilityFallDamage(this), this);

        //Mob scanner
        this.getServer().getPluginManager().registerEvents(new MobScanner(this), this);

        //Natural EliteMobs Spawning
        if (getConfig().getBoolean("Natural EliteMob spawning")) {

            this.getServer().getPluginManager().registerEvents(new NaturalMobSpawner(this), this);

            getLogger().info("EliteMobs - Natural EliteMob Spawning enabled!");

        }

        //Natural Mob Metadata Assigner
        this.getServer().getPluginManager().registerEvents(new NaturalMobMetadataAssigner(this), this);

        //Visual effects
        this.getServer().getPluginManager().registerEvents(new MinorPowerPowerStance(this), this);

        //Loot
        if (getConfig().getBoolean("Aggressive EliteMobs can drop config loot (level 5 EliteMobs and up)")) {

            this.getServer().getPluginManager().registerEvents(new EliteDropsHandler(this), this);

            getLogger().info("EliteMobs - EliteMob loot enabled!");

        }

        //Minecraft behavior canceller
        if (getConfig().getBoolean("Prevent players from changing mob spawners using eggs")) {

            this.getServer().getPluginManager().registerEvents(new PreventSpawnerMobEggInteraction(this), this);

            getLogger().info("EliteMobs - Mob egg interact on mob spawner canceller enabled!");

        }

        if (getConfig().getBoolean("Prevent creepers from killing passive mobs")) {

            this.getServer().getPluginManager().registerEvents(new PreventCreeperPassiveEntityDamage(this), this);

            getLogger().info("EliteMobs - Creeper passive mob collateral damage canceller enabled!");

        }

        this.getServer().getPluginManager().registerEvents(new ChunkUnloadMetadataPurge(this), this);

        //Commands
        this.getCommand("elitemobs").setExecutor(new CommandHandler(this));

    }

    @Override
    public void onDisable() {

        MetadataHandler metadataHandler = new MetadataHandler(this);

        for (World world : worldList) {

            for (Entity entity : world.getEntities()) {

                metadataHandler.flushMetadata(entity);

            }

        }

        getLogger().info("EliteMobs - Disabled!");

    }

    public void worldScanner() {

        for (World world : Bukkit.getWorlds()) {

            worldList.add(world);

        }

    }

    public void repeatingTaskRunner() {

        //eggs need to scale with stacked amount
        int passiveStackAmount = this.getConfig().getInt("Passive EliteMob stack amount");

        MobScanner mobScanner = new MobScanner(this);
        PotionEffectApplier potionEffectApplier = new PotionEffectApplier();

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                mobScanner.scanMobs(passiveStackAmount);

            }

        }, 1, 1);

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {

                potionEffectApplier.potionEffectApplier();

            }

        }, 20, 20);


    }

    public void loadConfiguration() {

        MetadataHandler metadataHandler = new MetadataHandler(this);

        //check defaults
        getConfig().addDefault("Allow aggressive EliteMobs", true);
        getConfig().addDefault("Valid aggressive EliteMobs", Arrays.asList("Blaze", "CaveSpider", "Creeper",
                "Enderman", "Endermite", "IronGolem", "PigZombie", "PolarBear", "Silverfish", "Skeleton",
                "Spider", "Witch", "Zombie"));
        getConfig().addDefault("Valid aggressive EliteMobs powers", metadataHandler.minorPowerList());
        getConfig().addDefault("Allow Passive EliteMobs", true);
        getConfig().addDefault("Valid Passive EliteMobs", Arrays.asList("Chicken", "Cow", "MushroomCow",
                "Pig", "Sheep"));
        getConfig().addDefault("Natural aggressive EliteMob spawning", true);
        getConfig().addDefault("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn", 20);
        getConfig().addDefault("Aggressive mob stacking", true);
        getConfig().addDefault("Aggressive mob stacking cap", 50);
        getConfig().addDefault("Passive EliteMob stack amount", 50);
        getConfig().addDefault("Aggressive EliteMobs can drop config loot (level 5 EliteMobs and up)", true);
        getConfig().addDefault("Aggressive EliteMobs flat loot drop rate %", 75);
        getConfig().addDefault("Aggressive EliteMobs can drop additional loot with drop % based on EliteMobs level (higher is more likely)", true);
        getConfig().addDefault("Prevent players from changing mob spawners using eggs", true);
        getConfig().addDefault("Prevent creepers from killing passive mobs", true);
        getConfig().addDefault("SuperCreeper explosion nerf multiplier", 1.0);
        getConfig().addDefault("Turn on visual effects for natural or plugin-spawned EliteMobs", true);
        getConfig().addDefault("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs", true);
        getConfig().options().copyDefaults(true);

        //save the config when changed
        saveConfig();
        saveDefaultConfig();

        getLogger().info("EliteMobs config loaded!");

    }

    public void reloadConfiguration() {

        reloadConfig();
        reloadCustomConfig();

        getLogger().info("EliteMobs config reloaded!");

    }

    public void initCustomConfig() {

        this.getCustomConfig().addDefault("Loot.Zombie Slayer.Item Type", "DIAMOND_SWORD");
        this.getCustomConfig().addDefault("Loot.Zombie Slayer.Item Name", "Zombie Slayer");
        //Loot example
        this.getCustomConfig().addDefault("Loot.Zombie Slayer.Item Lore", Arrays.asList("Slays zombies Bigly."));
        this.getCustomConfig().addDefault("Loot.Zombie Slayer.Enchantments", Arrays.asList("DAMAGE_ALL,5", "DAMAGE_UNDEAD,5"));
        this.getCustomConfig().addDefault("Loot.Zombie Slayer.Potion Effects", Arrays.asList("GLOWING,1"));
        //hunting helmet
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Helmet.Item Type", "DIAMOND_HELMET");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Helmet.Item Name", "Elite Mob Hunting Helmet");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Helmet.Item Lore", Arrays.asList("Wearing this helmet will",
                "increase the number of", "high level Elite Mobs", "that spawn around you!", "Only for the most tenacious minds."));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Helmet.Enchantments", Arrays.asList("VANISHING_CURSE,1"));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Helmet.Potion Effects", Arrays.asList("NIGHT_VISION,1"));
        //hunting chestplate
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Chestplate.Item Type", "DIAMOND_CHESTPLATE");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Chestplate.Item Name", "Elite Mob Hunting Chestplate");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Chestplate.Item Lore", Arrays.asList("Wearing this chestplate will",
                "increase the number of", "high level Elite Mobs", "that spawn around you!", "Only for the bravest souls."));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Chestplate.Enchantments", Arrays.asList("VANISHING_CURSE,1"));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Chestplate.Potion Effects", Arrays.asList("HEALTH_BOOST,1"));
        //hunting leggings
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Leggings.Item Type", "DIAMOND_LEGGINGS");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Leggings.Item Name", "Elite Mob Hunting Leggings");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Leggings.Item Lore", Arrays.asList("Wearing these leggings will",
                "increase the number of", "high level Elite Mobs", "that spawn around you!", "Only for those who aim the highest."));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Leggings.Enchantments", Arrays.asList("VANISHING_CURSE,1"));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Leggings.Potion Effects", Arrays.asList("JUMP,2"));
        //hunting boots
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Boots.Item Type", "DIAMOND_BOOTS");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Boots.Item Name", "Elite Mob Hunting Boots");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Boots.Item Lore", Arrays.asList("Wearing these boots will",
                "increase the number of", "high level Elite Mobs", "that spawn around you!", "Only for those fleetest of foot."));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Boots.Enchantments", Arrays.asList("VANISHING_CURSE,1"));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Boots.Potion Effects", Arrays.asList("SPEED,2"));
        //hunting bow
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Bow.Item Type", "BOW");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Bow.Item Name", "Elite Mob Hunting Bow");
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Bow.Item Lore", Arrays.asList("Wielding this bow will",
                "increase the number of", "high level Elite Mobs", "that spawn around you!", "Only natural-born hunters."));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Bow.Enchantments", Arrays.asList("VANISHING_CURSE,1", "ARROW_DAMAGE,3"));
        this.getCustomConfig().addDefault("Loot.Elite Mob Hunting Bow.Potion Effects", Arrays.asList("DAMAGE_RESISTANCE,1"));

        this.getCustomConfig().options().copyDefaults(true);
        this.saveDefaultCustomConfig();
        this.saveCustomConfig();

    }

    public void reloadCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(this.getDataFolder(), "loot.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        Reader defConfigStream = null;
        try {
            defConfigStream = new InputStreamReader(this.getResource("loot.yml"), "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(customConfigFile);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    public void saveDefaultCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(this.getDataFolder(), "loot.yml");
        }
        if (!customConfigFile.exists()) {
            this.saveResource("loot.yml", false);
        }
    }
    // End Custom Config

}