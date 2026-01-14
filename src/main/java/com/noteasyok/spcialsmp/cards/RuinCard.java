package com.noteasyok.spcialsmp.cards;

import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;

public class RuinCard implements Card {

    @Override
    public String getName() {
        return "Ruin";
    }

    @Override
    public void leftClick(Player p) {
        p.sendMessage("Ruin area activated");
    }

    @Override
    public void rightClick(Player p) {
        for (int i = 0; i < 10; i++) {
            p.getWorld().spawn(p.getLocation(), Silverfish.class);
        }
    }

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("Enemies poisoned");
    }
              }
