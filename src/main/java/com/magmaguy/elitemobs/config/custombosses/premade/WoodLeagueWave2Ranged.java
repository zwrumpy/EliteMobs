package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave2Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave2Ranged(){
        super("wood_league_wave_2_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "2");
        setMainHand(new ItemStack(Material.CROSSBOW));
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
