package com.noteasyok.spcialsmp.cards;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class EndermanCard implements Card {

    @Override
    public String getName() { return "Enderman Card"; }

    @Override
    public void leftClick(Player p) {
        Location target = p.getTargetBlockExact(60) != null ? p.getTargetBlockExact(60).getLocation().add(0,1,0) : p.getLocation();
        p.teleport(target);
    }

    @Override
    public void rightClick(Player p) {
        List<Player> online = Bukkit.getOnlinePlayers().stream().toList();
        if (online.size() <= 1) {
            p.sendMessage("No other players");
            return;
        }
        Player rand = online.stream().filter(pl -> !pl.equals(p)).findAny().orElse(null);
        if (rand != null) {
            p.teleport(rand.getLocation().add(1,0,0));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 10*20, 0, false, false));
        }
    }

    @Override
    public void shiftRightClick(Player p) {
        for (int i=0;i<3;i++) {
            Enderman e = p.getWorld().spawn(p.getLocation().add((i-1),0,0), Enderman.class);
            e.setTarget(null);
            Bukkit.getScheduler().runTaskLater(org.bukkit.Bukkit.getPluginManager().getPlugin("spcialSmp"), e::remove, 20L*10);
        }
    }
}
