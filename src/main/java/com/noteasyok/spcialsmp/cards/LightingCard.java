package com.noteasyok.spcialsmp.cards;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class LightingCard implements Card {

    @Override
    public String getName() {
        return "Lighting Card";
    }

    @Override
    public void leftClick(Player p) {
        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                50
        );
        if (r == null || r.getHitPosition() == null) return;

        Location hit = r.getHitPosition().toLocation(p.getWorld());
        p.getWorld().strikeLightning(hit);
    }

    @Override
    public void rightClick(Player p) {
        p.getWorld().setStorm(!p.getWorld().hasStorm());
    }

    @Override public void shiftRightClick(Player p) {}
}
