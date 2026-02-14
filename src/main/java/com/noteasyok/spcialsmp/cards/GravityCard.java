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

    @Override
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
        int radius = 50;

        p.sendMessage("§b§lGRAVITY » §e§lTHE WORLD IS FLOATING...");
        p.getWorld().playSound(origin, Sound.ENTITY_WITHER_SPAWN, 1f, 0.2f);

        List<FallingBlock> debris = new ArrayList<>();

        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                // STOP CONDITION: Time up ya Card hata diya
                if (timer > 240 || !isHoldingCard(p)) {
                    cleanup(debris, origin, radius);
                    p.sendMessage("§c§lGRAVITY » §fAbsolute gravity restored.");
                    this.cancel();
                    return;
                }

                // 1. RANDOM BLOCK LEVITATION (Optimization: max 80 blocks)
                if (debris.size() < 80 && timer % 2 == 0) {
                    int rx = random.nextInt(radius * 2) - radius;
                    int rz = random.nextInt(radius * 2) - radius;
                    Block b = origin.clone().add(rx, -1, rz).getBlock();
                    
                    if (b.getType().isSolid() && !b.getType().toString().contains("LEAVES")) {
                        FallingBlock fb = origin.getWorld().spawnFallingBlock(b.getLocation().add(0.5, 1.2, 0.5), b.getBlockData());
                        fb.setDropItem(false);
                        fb.setGravity(false);
                        fb.setVelocity(new Vector(0, 0.07, 0));
                        debris.add(fb);
                    }
                }

                // 2. GRAVITY PULSE (Shockwave har 2 seconds)
                if (timer % 40 == 0) {
                    origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.1f);
                    origin.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, origin, 5, 10, 1, 10, 0.1);
                    for (Entity e : origin.getWorld().getNearbyEntities(origin, radius, 15, radius)) {
                        if (e instanceof LivingEntity le && e != p) {
                            le.setVelocity(le.getVelocity().add(new Vector(0, 1.2, 0))); // Sabko hawa mein uchal dega
                        }
                    }
                }

                // 3. CONTINUOUS LEVITATION & PARTICLES
                for (Entity e : origin.getWorld().getNearbyEntities(origin, radius, 15, radius)) {
                    if (e instanceof LivingEntity le && e != p) {
                        le.setGravity(false);
                        le.setVelocity(le.getVelocity().add(new Vector(0, 0.03, 0)));
                        le.getWorld().spawnParticle(Particle.ENCHANT, le.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
                    }
                }

                // Dome Visual Effect
                if (timer % 15 == 0) {
                    origin.getWorld().spawnParticle(Particle.DRAGON_BREATH, origin, 150, radius/2, 2, radius/2, 0.02);
                }
                
                timer++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
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
