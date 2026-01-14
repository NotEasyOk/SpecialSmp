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
        strikeWhereLooking(p);
    }

    @Override
    public void rightClick(Player p) {
        boolean storm = p.getWorld().hasStorm();
        p.getWorld().setStorm(!storm);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("Lightning trail enabled (no damage)");
    }

    private void strikeWhereLooking(Player p) {
        Location eye = p.getEyeLocation();

        RayTraceResult result = p.getWorld().rayTraceBlocks(
                eye,
                eye.getDirection(),
                50
        );

        if (result == null || result.getHitPosition() == null) return;

        Location hit = result.getHitPosition().toLocation(p.getWorld());
        p.getWorld().strikeLightning(hit);
    }
}
