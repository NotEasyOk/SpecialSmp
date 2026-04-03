package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.block.Block;
import com.noteasyok.spcialsmp.cards.BaseCard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class GravityCard extends BaseCard implements Listener {

    private final Random random = new Random();

    public GravityCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Gravity Card"; }

    @Override
public String getConfigKey() {
    return "gravity-card";
}

    @Override
    public int getModelData() { return 105; }

    @Override
    public Material getMaterial() { return Material.ECHO_SHARD; }

    public List<String> getLore() {
        List<String> lore = new ArrayList<>();
        lore.add("§8§m--------------------------");
        lore.add("§d§lPASSIVE: §fZero Friction");
        lore.add("§7Immune to fall damage while holding.");
        lore.add(" ");
        lore.add("§6§l⚡ §eLEFT: §bGravity Snatcher");
        lore.add("§7Snipe and launch targets into the sky.");
        lore.add(" ");
        lore.add("§5§l🌀 §dRIGHT: §5Singularity");
        lore.add("§7Vortex that pulls all life forms.");
        lore.add(" ");
        lore.add("§b§l✨ §bSHIFT+R: §3§lEVENT HORIZON");
        lore.add("§fMassive 50-block Zero-G Field.");
        lore.add("§8§m--------------------------");
        return lore;
    }

    /* ---------------- LEFT CLICK: GRAVITY SNATCHER (Precision Aim) ---------------- */
    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "left")) return;

        // Long range nishana (30 blocks)
        RayTraceResult result = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), 30, 1.5, e -> e != p && e instanceof LivingEntity);

        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            // Visual Blast
            p.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation().add(0, 1, 0), 2);
            p.getWorld().spawnParticle(Particle.FLASH, target.getLocation(), 1);
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 0.5f);
            
            // Ultra Yeet: High velocity upward and forward
            target.setVelocity(p.getLocation().getDirection().multiply(3.5).setY(1.8));
            p.sendMessage("§5§lGRAVITY » §d" + target.getName() + " §fhas been snatched!");
        } else {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
        }
    }

    /* ---------------- RIGHT CLICK: SINGULARITY (Heavy Vortex) ---------------- */
@Override
public void rightClick(Player p) {
    if (!isCool(p, "right")) return;

    Block targetBlock = p.getTargetBlockExact(25);
    if (targetBlock == null) return;
    Location center = targetBlock.getLocation().add(0.5, 2.5, 0.5);

    new BukkitRunnable() {
        int t = 0;
        @Override
        public void run() {
            // Card hata diya ya time khatam toh stop
            if (t > 140 || !isHoldingCard(p)) {
                this.cancel();
                return;
            }

            // Visuals
            center.getWorld().spawnParticle(Particle.SQUID_INK, center, 30, 0.3, 0.3, 0.3, 0.05);
            center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 15, 3, 3, 3, 0.1);
            
            if (t % 10 == 0) center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.1f);

            // --- PULL LOGIC FIX ---
            for (Entity e : center.getWorld().getNearbyEntities(center, 12, 12, 12)) {
                // p != e ensures the caster isn't pulled
                if (e != p && (e instanceof LivingEntity || e instanceof Player)) {
                    
                    Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.4);
                    
                    // Players ke liye velocity thodi strong aur upward y-axis zaruri hai
                    // varna wo zameen se "friction" ki wajah se hilenge nahi
                    e.setVelocity(pull.add(new Vector(0, 0.15, 0))); 
                    
                    // Animation for pulling
                    if (t % 5 == 0) {
                        e.getWorld().spawnParticle(Particle.SMOKE, e.getLocation(), 5, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
            t++;
        }
    }.runTaskTimer(SpcialSmp.get(), 0, 1);
}

    /* ---------------- SHIFT+R: EVENT HORIZON (50-Block Pulse Field) ---------------- */
    @Override
public void shiftRightClick(Player p) {
    if (!isCool(p, "shift_right")) return;

    Block target = p.getTargetBlockExact(7);
    if (target == null || target.getType() == Material.AIR) return;

    Location startLoc = target.getLocation();
    Material wallMaterial = target.getType(); // Ground block ka material lega
    
    Vector direction = p.getLocation().getDirection().setY(0).normalize();
    Vector side = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

    List<Block> wallBlocks = new ArrayList<>();
    p.getWorld().playSound(startLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0.5f);

    // --- PHASE 1: RISING (7 High, 5 Wide) ---
    new BukkitRunnable() {
        int height = 1;
        @Override
        public void run() {
            if (height > 7) {
                startRemovalTask(wallBlocks, wallMaterial); // Phase 2 trigger
                this.cancel();
                return;
            }

            for (int w = -2; w <= 2; w++) { // 5 Wide (-2 to 2)
                Location loc = startLoc.clone().add(side.clone().multiply(w)).add(0, height, 0);
                Block b = loc.getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(wallMaterial);
                    wallBlocks.add(b);
                    p.getWorld().playEffect(loc, Effect.STEP_SOUND, wallMaterial);
                }
            }
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 0.8f);
            height++;
        }
    }.runTaskTimer(SpcialSmp.get(), 0, 2);
}

private void startRemovalTask(List<Block> blocks, Material mat) {
    new BukkitRunnable() {
        @Override
        public void run() {
            for (Block b : blocks) {
                if (b.getType() == mat) {
                    // Glass breaking style animation & particles
                    b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, mat.createBlockData());
                    b.getWorld().playSound(b.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.2f);
                    b.setType(Material.AIR);
                }
            }
        }
    }.runTaskLater(SpcialSmp.get(), 200L); // 10 Seconds (200 ticks)
        }

    // --- HELPER METHODS FOR BUG-FREE LOGIC ---

    private boolean isHoldingCard(Player p) {
        return p.getInventory().getItemInMainHand().getType() == getMaterial();
    }

    private void cleanup(List<FallingBlock> debris, Location loc, int radius) {
        for (FallingBlock fb : debris) {
            if (fb.isValid()) fb.setGravity(true);
        }
        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, 30, radius)) {
            if (e instanceof LivingEntity le) {
                le.setGravity(true);
                // Give slow falling so they don't die instantly (Optional)
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING, 100, 1));
            }
        }
    }

    private boolean isCool(Player p, String action) {
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), action)) {
            long remaining = SpcialSmp.get().getCooldownManager().getRemainingSeconds(p, getName(), action);
            p.sendMessage("§cWait " + remaining + "s");
            return false;
        }
        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), action);
        return true;
    }
  }
