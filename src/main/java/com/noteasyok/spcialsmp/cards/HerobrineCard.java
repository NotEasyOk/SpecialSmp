package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HerobrineCard implements Card {

    public String getName() { return "Herobrine Card"; }

    public void leftClick(Player p) {
        World w = p.getWorld();
        for (int i = 0; i < 5; i++)
            w.strikeLightning(p.getLocation());
    }

    public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
        p.setAllowFlight(true);
    }

    public void shiftRightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 3));
    }
}
