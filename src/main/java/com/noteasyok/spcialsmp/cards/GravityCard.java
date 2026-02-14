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

public class GravityCard extends BaseCard implements Listener {

    private final Random random = new Random();

    public GravityCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Gravity Card"; }

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
                // BUG FIX: Card hata diya to stop
                if (t > 140 || !isHoldingCard(p)) {
                    this.cancel();
                    return;
                }

                // Black Hole Animation
                center.getWorld().spawnParticle(Particle.SQUID_INK, center, 30, 0.3, 0.3, 0.3, 0.05);
                center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 10, 3, 3, 3, 0.1);
                
                if (t % 10 == 0) center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.1f);

                for (Entity e : center.getWorld().getNearbyEntities(center, 12, 12, 12)) {
                    if (e instanceof LivingEntity target && e != p) {
                        Vector pull = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.5);
                        target.setVelocity(pull.setY(0.2)); // Strong pull to center
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

        Location origin = p.getLocation();
        int radius = 16; // Ek poora chunk (16x16) cover karega
        int maxHeight = 30; // 30 blocks height
        List<FallingBlock> chunkDebris = new ArrayList<>();

        p.sendMessage("§5§lGRAVITY » §d§lCHUNK ASCENSION ACTIVATED!");
        p.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 2f, 0.5f);

        // Scan and Lift Blocks
        for (int x = -radius/2; x <= radius/2; x++) {
            for (int z = -radius/2; z <= radius/2; z++) {
                // Har column mein sirf sabse upar wala solid block uthayenge (Optimization)
                Block b = p.getWorld().getHighestBlockAt(origin.clone().add(x, 0, z)).getRelative(0, -1, 0);
                
                if (b.getType() != Material.AIR && b.getType().isSolid()) {
                    FallingBlock fb = p.getWorld().spawnFallingBlock(b.getLocation().add(0.5, 1.1, 0.5), b.getBlockData());
                    fb.setDropItem(false);
                    fb.setGravity(false); // Hawa mein rokne ke liye
                    
                    // Initial push up
                    fb.setVelocity(new Vector(0, 0.5, 0));
                    chunkDebris.add(fb);
                }
            }
        }

        new BukkitRunnable() {
            int timer = 0; // 10 seconds = 200 ticks
            @Override
            public void run() {
                // Card check (Agar card hata diya toh turant niche girega)
                if (timer > 200 || p.getInventory().getItemInMainHand().getType() != getMaterial()) {
                    for (FallingBlock fb : chunkDebris) {
                        if (fb.isValid()) fb.setGravity(true); // Gravity wapas
                    }
                    this.cancel();
                    return;
                }

                // Blocks ko 30 block ki height tak le jana aur wahan rokna
                for (FallingBlock fb : chunkDebris) {
                    if (fb.isValid()) {
                        double currentY = fb.getLocation().getY();
                        double targetY = origin.getY() + maxHeight;

                        if (currentY < targetY) {
                            fb.setVelocity(new Vector(0, 0.2, 0)); // Upar uthao
                        } else {
                            fb.setVelocity(new Vector(0, 0.01, 0)); // Hawa mein float karao
                        }
                    }
                }

                // Entities (Players/Mobs) ko bhi upar khicho
                for (Entity e : p.getNearbyEntities(radius, 20, radius)) {
                    if (e instanceof LivingEntity le) {
                        le.setGravity(false);
                        le.setVelocity(new Vector(0, 0.15, 0));
                    }
                }

                if (timer % 20 == 0) p.getWorld().spawnParticle(Particle.CLOUD, origin, 100, 8, 1, 8, 0.1);
                
                timer += 5;
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 5);
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
