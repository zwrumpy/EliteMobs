package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteRavager extends EliteMobProperties {
    public EliteRavager() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.RAVAGER).getName();
        this.entityType = EntityType.RAVAGER;
        this.defaultMaxHealth = 100;
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.RAVAGER).isEnabled();
        if (this.isEnabled)
            eliteMobData.add(this);
    }
}