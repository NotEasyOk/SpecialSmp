package com.noteasyok.spcialsmp.cards;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EndermanCard implements Card {

    @Override
    public String getName() {
        return "Enderman Card";
    }

    @Override
    public void leftClick(Player p) {
        Location t = p.getTargetBlockExact(50) != null
                ? p.getTargetBlockExact(50).getLocation().add(0,1,0)
                : p.getLocation();
        p.teleport(t);
    }

    @Override
    public void rightClick(Player p) {
        Player any = Bukkit.getOnlinePlayers().stream()
                .filter(pl -> !pl.equals(p))
                .findAny().orElse(null);

        if (any == null) return;

        p.teleport(any.getLocation());
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, 200, 0));
    }

    @Override
    public void shiftRightClick(Player p) {
        for (int i = 0; i < 3; i++) {
            Enderman e = p.getWorld().spawn(p.getLocation(), Enderman.class);
            e.setTarget(null);
        }
    }
}
