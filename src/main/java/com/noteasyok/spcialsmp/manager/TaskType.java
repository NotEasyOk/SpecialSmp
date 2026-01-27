package com.noteasyok.spcialsmp.manager;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum TaskType {
    MINE_DIAMOND("5 Diamonds dhoond kar mine karo", Material.DIAMOND_ORE, 5),
    CUT_WOOD("64 Oak Wood (Log) kato", Material.OAK_LOG, 64),
    KILL_ZOMBIES("20 Zombies ko khatam karo", EntityType.ZOMBIE, 20),
    FISH_ITEMS("10 baar machli pakdo (Fishing)", Material.COD, 10),
    CRAFT_BREAD("32 Bread craft karo", Material.BREAD, 32),
    KILL_SKELETONS("15 Skeletons ko maaro", EntityType.SKELETON, 15);

    private final String description;
    private final Object target;
    private final int amount;

    TaskType(String description, Object target, int amount) {
        this.description = description;
        this.target = target;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public Object getTarget() { return target; }
    public int getAmount() { return amount; }
  }
