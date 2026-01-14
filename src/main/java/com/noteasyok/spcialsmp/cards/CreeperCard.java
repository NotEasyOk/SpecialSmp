package com.noteasyok.spcialsmp.cards;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CreeperCard implements Card {

    @Override
    public String getName() {
        return "Creeper";
    }

    @Override
    public void leftClick(Player p) {
        Location l = p.getLocation();
        l.getWorld().createExplosion(l, 4f, false, false);
    }

    @Override
    public void rightClick(Player p) {
        Location l = p.getTargetBlockExact(50).getLocation();
        l.getWorld().createExplosion(l, 6f, false, false);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.getWorld().createExplosion(p.getLocation(), 5f, false, false);
    }
}
