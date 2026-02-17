package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MirrorCard extends BaseCard implements Listener {

    private final Set<UUID> reflectingPlayers = new HashSet<>();

    public MirrorCard() {
        super(); Material.GLASS_PANE, "§b§lMirror Card", "§7Aim to Mimic.");
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

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
    public void LeftClick(Player p) {
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

    // --- SHIFT+RIGHT: REFLECTION SHIELD ---
    @Override
    public void ShiftRightClick(Player p, PlayerInteractEvent e) {
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
