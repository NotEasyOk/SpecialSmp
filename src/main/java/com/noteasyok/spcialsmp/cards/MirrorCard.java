package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MirrorCard extends BaseCard implements Listener {

    private final Set<UUID> reflectingPlayers = new HashSet<>();

    public MirrorCard() { super(); Bukkit.getPluginManager().registerEvents(this, com.noteasyok.spcialsmp.SpcialSmp.get()); }

    @Override
    public String getName() { return "Mirror Card"; }
    @Override
    public Material getMaterial() { return Material.GLASS_PANE; }
    @Override
    public int getModelData() { return 103; }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains(getName());
    }

    // --- LEFT CLICK: AIM-BASED IDENTITY THIEF ---
    @Override
    public void leftClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        // Range 20 blocks Aim Check
        Entity target = getTarget(p, 20);
        if (target instanceof Player victim) {
            String originalName = p.getDisplayName();
            p.setDisplayName(victim.getDisplayName());
            p.setPlayerListName(victim.getPlayerListName());
            p.sendMessage("§b§l[!] §fIdentity stolen from §e" + victim.getName());
            
            p.getWorld().spawnParticle(Particle.ENCHANTED_HIT, p.getLocation(), 30, 0.5, 1, 0.5, 0.1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    p.setDisplayName(originalName);
                    p.setPlayerListName(p.getName());
                }
            }.runTaskLater(SpcialSmp.get(), 200); // 10s back to normal

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
        }
    }

    @Override
    public void rightClick(Player p) {
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "right")) return;

        Entity target = getTarget(p, 15); // 15 block ki range
        if (target instanceof Player victim) {
            p.sendMessage("§b§l[!] §fDistorting §e" + victim.getName() + "'s §ftime perception!");
            p.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5f, 0.8f);

            // Effect for the Victim
            victim.sendTitle("§c§lTIME DISTORTION", "§7Reality is bending...", 10, 60, 10);
            
            // Apply Chromatic Aberration (Packet-based)
            // Note: Actual chromatic aberration is hard in Bukkit, simulating with client-side darkness/blur
            victim.addPotionEffect(new org.bukkit.potion.PotionEffect( org.bukkit.potion.PotionEffectType.DARKNESS, 100, 0, false, false));
            victim.addPotionEffect(new org.bukkit.potion.PotionEffect( org.bukkit.potion.PotionEffectType.NAUSEA, 100, 0, false, false));

            // Temporal Distortion Animation (Client-side illusion)
            new BukkitRunnable() {
                int ticks = 0;
                Location lastLoc = victim.getLocation();

                @Override
                public void run() {
                    if (ticks++ > 100 || !victim.isOnline()) { // 5 seconds
                        this.cancel();
                        return;
                    }

                    // Reverse particle trail
                    victim.getWorld().spawnParticle(Particle.REVERSE_PORTAL, victim.getLocation(), 10, 0.2, 0.2, 0.2, -0.1); // Negative speed for reverse

                    // Simulate movement distortion
                    if (!victim.getLocation().equals(lastLoc)) {
                    org.bukkit.util.Vector direction = victim.getLocation().toVector().subtract(lastLoc.toVector());
                    victim.getWorld().spawnParticle(Particle.SONIC_BOOM, victim.getLocation(), 3, direction.getX(), direction.getY(), direction.getZ(), 0.01);
                    }
                    lastLoc = victim.getLocation();
                }
            }.runTaskTimer(SpcialSmp.get(), 0L, 1L); // Har tick par update

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right"); // 10s cooldown
        } else {
            p.sendMessage("§c§l[!] §7Aim at a player to distort their reality!");
        }
    }

    // --- SHIFT+RIGHT: REFLECTION SHIELD ---
    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        reflectingPlayers.add(p.getUniqueId());
        p.sendMessage("§b§l[!] §fMirror Shield Active!");

        new BukkitRunnable() {
            @Override
            public void run() { reflectingPlayers.remove(p.getUniqueId()); }
        }.runTaskLater(SpcialSmp.get(), 100); // 5s

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player victim && reflectingPlayers.contains(victim.getUniqueId())) {
            if (e.getDamager() instanceof LivingEntity attacker) {
                e.setCancelled(true);
                attacker.damage(e.getDamage(), victim);
                victim.getWorld().spawnParticle(Particle.FLASH, victim.getLocation(), 5);
            }
        }
    }

    private Entity getTarget(Player p, int range) {
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, e -> !e.equals(p));
        return (ray != null) ? ray.getHitEntity() : null;
    }
          }
