package com.noteasyok.spcialsmp.cards;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public class EndermanCard implements Card {

    public String getName() { return "Enderman"; }

    public void leftClick(Player p) {
        if (p.getTargetBlockExact(40) == null) return;
        Location l = p.getTargetBlockExact(40).getLocation().add(0,1,0);
        p.teleport(l);
    }

    public void rightClick(Player p) {
        Player t = Bukkit.getOnlinePlayers().stream()
                .filter(pl -> !pl.equals(p)).findAny().orElse(null);
        if (t != null) p.teleport(t.getLocation());
    }

    public void shiftRightClick(Player p) {
        for (int i = 0; i < 3; i++) {
            Enderman e = p.getWorld().spawn(p.getLocation(), Enderman.class);
            e.setTarget(p);
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("spcialSmp"),
                    e::remove, 200
            );
        }
    }
              }
              
