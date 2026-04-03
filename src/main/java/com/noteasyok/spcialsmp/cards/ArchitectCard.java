package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArchitectCard extends BaseCard {

    public ArchitectCard() {
        super(); }

    @Override
    public String getName() { return "Architect Card"; }
    @Override
public String getConfigKey() {
    return "architect-card";
}
    @Override
    public Material getMaterial() { return Material.ORANGE_DYE; }
    @Override
    public int getModelData() { return 102; }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains(getName());
    }

    // --- LEFT CLICK: AIM-BASED BEDROCK PRISON ---
    @Override
    public void leftClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        // Yahan Aim-Check ho raha hai (Range: 25 Blocks)
        Entity target = getTarget(p, 25);
        if (target instanceof LivingEntity victim && !victim.equals(p)) {
            Location loc = victim.getLocation().getBlock().getLocation();
            
            // Prison Blocks (Packet based - Server par kachra nahi hoga)
            int[][] shape = {{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}, {1,1,0}, {-1,1,0}, {0,1,1}, {0,1,-1}, {0,2,0}};
            for (int[] offset : shape) {
                Location bLoc = loc.clone().add(offset[0], offset[1], offset[2]);
                p.sendBlockChange(bLoc, Material.BEDROCK.createBlockData());
                // Dushman ko bhi dikhana hoga ki wo qaid hai
                if (victim instanceof Player targetPlayer) {
                    targetPlayer.sendBlockChange(bLoc, Material.BEDROCK.createBlockData());
                }
            }

            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255));
            p.sendMessage("§6§l[!] §fLocked §e" + victim.getName() + " §fin bedrock!");

            // 5s baad prison gayab
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int[] offset : shape) {
                        Location bLoc = loc.clone().add(offset[0], offset[1], offset[2]);
                        p.sendBlockChange(bLoc, bLoc.getBlock().getBlockData());
                        if (victim instanceof Player targetPlayer) {
                            targetPlayer.sendBlockChange(bLoc, bLoc.getBlock().getBlockData());
                        }
                    }
                }
            }.runTaskLater(SpcialSmp.get(), 100);

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
        }
    }

    @Override
    public void rightClick(Player p) {
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "right")) return;

        p.sendMessage("§6§l[!] §fKinetic Shield Active!");
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 200) { this.cancel(); return; } // 10 seconds active

                // Visual Animation
                Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(1.5)).add(0, 1, 0);
                p.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, loc, 5, 0.5, 0.5, 0.5, 0.05);

                // Logic: Push nearby enemies
                for (Entity e : p.getNearbyEntities(3, 3, 3)) {
                    if (e instanceof LivingEntity target && !e.equals(p)) {
                        org.bukkit.util.Vector push = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2);
                        push.setY(0.5);
                        target.setVelocity(push);
                        target.damage(1.0, p); // Light damage
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right"); // Set to 10s in config
                    }

    // --- SHIFT+RIGHT: GOLD BRIDGE ---
    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!isHoldingCard(p) || ticks++ > 200) { this.cancel(); return; }

                Location under = p.getLocation().subtract(0, 1, 0);
                Block b = under.getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.GOLD_BLOCK);
                    p.playSound(p.getLocation(), Sound.BLOCK_METAL_PLACE, 0.5f, 2f);
                    
                    // 2s baad remove
                    new BukkitRunnable() {
                        @Override
                        public void run() { 
                            if(b.getType() == Material.GOLD_BLOCK) b.setType(Material.AIR); 
                        }
                    }.runTaskLater(SpcialSmp.get(), 40);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }

    private Entity getTarget(Player p, int range) {
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, e -> !e.equals(p));
        return (ray != null) ? ray.getHitEntity() : null;
    }
          }
