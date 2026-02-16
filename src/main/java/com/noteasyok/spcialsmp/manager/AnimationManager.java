package com.noteasyok.spcialsmp.manager;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationManager {

    public static void playSoulLeavingEffect(Location loc, Player victim) {
        if (!SpcialSmp.get().getConfig().getBoolean("life-system.enabled")) return;

        World world = loc.getWorld();
        if (world == null) return;

        // 1. Sound Effect (Darrawani Soul Awaaz)
        world.playSound(loc, Sound.ENTITY_WARDEN_DEATH, 1.0f, 0.5f);
        world.playSound(loc, Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 0.8f);

        // 2. Create a floating "Soul" (Invisible Armor Stand with Player Head)
        ArmorStand soul = (ArmorStand) world.spawnEntity(loc.clone().add(0, -0.5, 0), EntityType.ARMOR_STAND);
        soul.setVisible(false);
        soul.setGravity(false);
        soul.setSmall(true);
        soul.setMarker(true); // Hitbox remove karne ke liye

        // Player ka sar (Head) soul banayenge
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(victim);
            head.setItemMeta(meta);
        }
        soul.getEquipment().setHelmet(head);

        // 3. Animation Loop (Soul upar jayegi aur particles niklenge)
        new BukkitRunnable() {
            int ticks = 0;
            double yOffset = 0;

            @Override
            public void run() {
                if (ticks >= 60) { // 3 Seconds ke baad gayab
                    soul.remove();
                    // Explosion effect at end
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, soul.getLocation(), 20, 0.2, 0.2, 0.2, 0.05);
                    this.cancel();
                    return;
                }

                // Soul ko dheere dheere upar le jao
                yOffset += 0.05;
                Location currentLoc = loc.clone().add(0, yOffset, 0);
                
                // Spin animation
                float yaw = (ticks * 20) % 360;
                currentLoc.setYaw(yaw);
                soul.teleport(currentLoc);

                // Particles (Soul Trails)
                world.spawnParticle(Particle.SOUL, currentLoc.clone().add(0, 0.5, 0), 2, 0.1, 0.1, 0.1, 0.02);
                world.spawnParticle(Particle.SCULK_SOUL, currentLoc, 1, 0.1, 0.1, 0.1, 0.01);

                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }
          }
