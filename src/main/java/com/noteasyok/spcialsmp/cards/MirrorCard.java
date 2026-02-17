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
    public Material getMaterial() { return Material.ECHO_SHARD; }
    @Override
    public int getModelData() { return 103; }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains(getName());
    }
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.profile.PlayerProfile;

import java.util.*;

public class MirrorCard extends BaseCard implements Listener {

    private final Set<UUID> reflectingPlayers = new HashSet<>();

    public MirrorCard() { 
        super(); 
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get()); 
    }

    @Override
    public String getName() { return "Mirror Card"; }
    @Override
    public Material getMaterial() { return Material.ECHO_SHARD; }
    @Override
    public int getModelData() { return 103; }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.getType() == getMaterial() && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains(getName());
    }

    // --- LEFT CLICK: SECRET IDENTITY THIEF (SKIN + NAME + NAMETAG) ---
    @Override
    public void leftClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        Entity target = getTarget(p, 20);
        if (target instanceof Player victim) {
            // 1. Store Originals
            PlayerProfile originalProfile = p.getPlayerProfile();
            String originalName = p.getName();
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

            // 2. Secret Swap (Skin + Tab + Chat)
            p.setPlayerProfile(victim.getPlayerProfile()); // Skin copy
            p.setDisplayName(victim.getName());
            p.setPlayerListName(victim.getName());

            // 3. Nametag Fix (Sir ke upar wala naam)
            String teamName = "m_" + p.getName().substring(0, Math.min(p.getName().length(), 14));
            Team team = board.getTeam(teamName);
            if (team == null) team = board.registerNewTeam(teamName);
            team.setPrefix(victim.getName()); 
            team.addEntry(p.getName());

            p.sendMessage("§b§l[!] §fIdentity Synchronized: §e" + victim.getName());
            p.getWorld().spawnParticle(Particle.ENCHANTED_HIT, p.getLocation().add(0,1,0), 30, 0.2, 0.5, 0.2, 0.05);

            // 20 Seconds duration (Time fixed)
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.setPlayerProfile(originalProfile); // Reset Skin
                    p.setDisplayName(originalName);
                    p.setPlayerListName(originalName);
                    Team t = board.getTeam(teamName);
                    if (t != null) t.unregister(); // Reset Nametag
                    p.sendMessage("§7§o[!] Your true form has returned.");
                }
            }.runTaskLater(SpcialSmp.get(), 400); // 20 Seconds

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
        }
    }

    @Override
    public void rightClick(Player p) {
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "right")) return;

        Entity target = getTarget(p, 15);
        if (target instanceof Player victim) {
            p.sendMessage("§b§l[!] §fDistorting §e" + victim.getName());
            p.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5f, 0.8f);

            victim.sendTitle("§c§lTIME DISTORTION", "§7Reality is bending...", 10, 60, 10);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, false, false));

            new BukkitRunnable() {
                int ticks = 0;
                Location lastLoc = victim.getLocation();
                @Override
                public void run() {
                    if (ticks++ > 100 || !victim.isOnline()) { this.cancel(); return; }
                    victim.getWorld().spawnParticle(Particle.REVERSE_PORTAL, victim.getLocation(), 10, 0.2, 0.2, 0.2, -0.1);
                    if (!victim.getLocation().equals(lastLoc)) {
                        Vector direction = victim.getLocation().toVector().subtract(lastLoc.toVector());
                        victim.getWorld().spawnParticle(Particle.SONIC_BOOM, victim.getLocation(), 3, direction.getX(), direction.getY(), direction.getZ(), 0.01);
                    }
                    lastLoc = victim.getLocation();
                }
            }.runTaskTimer(SpcialSmp.get(), 0L, 1L);

            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "right");
        }
    }

    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return;
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        reflectingPlayers.add(p.getUniqueId());
        p.sendMessage("§b§l[!] §fMirror Shield Active!");

        new BukkitRunnable() {
            @Override
            public void run() { reflectingPlayers.remove(p.getUniqueId()); }
        }.runTaskLater(SpcialSmp.get(), 100);

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
        // Range buffer 1.5 rakha hai taaki asani se target lock ho
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, 1.5, e -> !e.equals(p));
        return (ray != null) ? ray.getHitEntity() : null;
    }
            }tHitEntity() : null;
    }
          }
