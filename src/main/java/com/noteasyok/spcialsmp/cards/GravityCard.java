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
        int radius = 7; // Total 15x15 Area (Max stable for Shulkers)
        int depth = 5;  // 5 layers deep: Grass, Dirt, Stone
        int maxHeight = 35; // Higher lift
        
        p.sendMessage("§5§lGRAVITY » §4§lTHE WORLD IS TEARING APART!");
        p.getWorld().playSound(origin, Sound.ENTITY_WITHER_SPAWN, 2f, 0.1f);
        p.getWorld().playSound(origin, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0.5f);

        // --- STAGE 1: EARTH CRACKING & INITIAL PULSE (3 Seconds) ---
        new BukkitRunnable() {
            int preTicks = 0;
            @Override
            public void run() {
                if (preTicks >= 60) { // 3 seconds baad actual lift shuru
                    startUltimateAscension(p, origin, radius, depth, maxHeight);
                    this.cancel();
                    return;
                }

                // Visuals: Cracks, Dust, Smoke
                if (preTicks % 5 == 0) {
                    p.getWorld().spawnParticle(Particle.BLOCK, origin, 80, radius, 0.5, radius, 0.1, Material.STONE.createBlockData());
                    p.getWorld().spawnParticle(Particle.ENCHANTED_HIT, origin, 50, radius, 1, radius, 0.1);
                    p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, origin, 1, 0, 0, 0, 0); // Mini-explosions
                    p.getWorld().playSound(origin, Sound.BLOCK_STONE_BREAK, 0.8f, 0.5f);
                }
                preTicks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
    }

    private void startUltimateAscension(Player p, Location origin, int radius, int depth, int maxHeight) {
        List<Entity> islandEntities = new ArrayList<>();
        List<Location> removedBlocks = new ArrayList<>(); // Track original block locations
        
        p.getWorld().playSound(origin, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.1f);
        p.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 2f, 0.5f);

        // --- STAGE 2: MASSIVE BLOCK REMOVAL & ENTITY SPAWN ---
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y > -depth -1; y--) { // -1 se shuru karke deep uthana
                    Block b = origin.clone().add(x, y, z).getBlock();
                    if (b.getType() != Material.AIR && b.getType().isSolid()) {
                        Location spawnLoc = b.getLocation().add(0.5, 0, 0.5);
                        
                        // Falling Block for visuals
                        FallingBlock fb = p.getWorld().spawnFallingBlock(spawnLoc, b.getBlockData());
                        fb.setDropItem(false);
                        fb.setGravity(false);
                        islandEntities.add(fb);

                        // Invisible Shulker for REAL COLLISION
                        org.bukkit.entity.Shulker shulker = p.getWorld().spawn(spawnLoc, org.bukkit.entity.Shulker.class);
                        shulker.setInvisible(true);
                        shulker.setAI(false);
                        shulker.setInvulnerable(true);
                        shulker.setGravity(false);
                        islandEntities.add(shulker);
                        
                        // Zameen mein gaddha (hole) ban jayega
                        removedBlocks.add(b.getLocation()); // Original location store karo
                        b.setType(Material.AIR);
                    }
                }
            }
        }

        // --- STAGE 3: ASCENSION LOOP & VISUALS ---
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // Total duration 15 seconds (300 ticks) or Card removed
                if (ticks > 300 || !p.getInventory().getItemInMainHand().getType().equals(getMaterial())) {
                    // Cleanup: FallingBlocks niche girenge, Shulkers gayab
                    for (Entity ent : islandEntities) {
                        if (ent instanceof FallingBlock fb) fb.setGravity(true);
                        else ent.remove();
                    }
                    // Particles when blocks fall back
                    for(Location loc : removedBlocks) {
                         loc.getWorld().spawnParticle(Particle.POOF, loc, 10, 0.5, 0.5, 0.5, 0.1);
                    }

                    p.sendMessage("§c§lGRAVITY » §fThe world reclaims its pieces.");
                    this.cancel();
                    return;
                }

                // Smooth upward velocity
                Vector v = new Vector(0, 0.18, 0); 
                if (ticks > 250) v.setY(0); // Pause at peak for last 2.5 seconds

                for (Entity ent : islandEntities) {
                    if (ent.isValid()) {
                        if (ent.getLocation().getY() < origin.getY() + maxHeight) {
                            ent.setVelocity(v);
                        } else {
                            ent.setVelocity(new Vector(0, 0.01, 0)); // Hover vibration
                        }
                    }
                }

                // Main Gravity Core particle effect at the bottom of the floating island
                if (ticks % 5 == 0) {
                    origin.getWorld().spawnParticle(Particle.REVERSE_PORTAL, origin.clone().add(0, -depth -5, 0), 50, radius/2, 2, radius/2, 0.05);
                    origin.getWorld().spawnParticle(Particle.DRAGON_BREATH, origin.clone().add(0, -depth -3, 0), 30, radius/2, 1, radius/2, 0.02);
                }

                // Falling debris from the floating island
                if (ticks % 10 == 0) {
                     int rX = random.nextInt(radius * 2) - radius;
                     int rZ = random.nextInt(radius * 2) - radius;
                     origin.getWorld().spawnParticle(Particle.FALLING_DUST, origin.clone().add(rX, origin.getY() + maxHeight - 5, rZ), 5, 0.1, 0.1, 0.1, 0, Material.DIRT.createBlockData());
                }
                
                ticks++;
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
