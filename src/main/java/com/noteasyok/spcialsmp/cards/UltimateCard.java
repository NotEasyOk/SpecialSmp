package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeDomain = new HashSet<>();
    private final Map<UUID, UUID> throneSeats = new HashMap<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= LEFT CLICK: RISING STRUCTURE & THRONE ================= */
    @Override
    public void leftClick(Player p) {
        if (activeDomain.contains(p.getUniqueId()) || !isCool(p, "blood_domain", 60)) return;

        // FIXED AIM: Exactly where the player is looking
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 15);
        Location baseLoc = (ray != null && ray.getHitBlock() != null) ? ray.getHitBlock().getLocation().add(0.5, 0, 0.5) : p.getLocation();
        
        activeDomain.add(p.getUniqueId());
        List<ArmorStand> structureParts = new ArrayList<>();
        
        p.getWorld().strikeLightningEffect(baseLoc);
        p.getWorld().playSound(baseLoc, Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);

        // --- STRUCTURE BUILDING (Spawns underground) ---
        // Netherite Pillars (As per PNG 1)
        structureParts.add(createThronePart(baseLoc.clone().add(2, -5, 2), Material.NETHERITE_BLOCK));
        structureParts.add(createThronePart(baseLoc.clone().add(-2, -5, 2), Material.NETHERITE_BLOCK));
        structureParts.add(createThronePart(baseLoc.clone().add(2, -5, -2), Material.NETHERITE_BLOCK));
        structureParts.add(createThronePart(baseLoc.clone().add(-2, -5, -2), Material.NETHERITE_BLOCK));
        
        // Crystal/Ice Top (As per PNG 1)
        structureParts.add(createThronePart(baseLoc.clone().add(0, -3, 0), Material.BLUE_ICE));
        
        // THE SWORD THRONE (As per PNG 2) - Hidden in center
        ArmorStand throneSeat = createThronePart(baseLoc.clone().add(0, -5, 0), Material.NETHERITE_SWORD);
        structureParts.add(throneSeat);

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                // RISING PHASE: Coming up to Ground Level (No Hover)
                if (step < 100) { 
                    for (ArmorStand as : structureParts) {
                        as.teleport(as.getLocation().add(0, 0.05, 0));
                    }
                    // Visual Effects: Red Smoke + Ground Dirt
                    Location currentPos = throneSeat.getLocation();
                    currentPos.getWorld().spawnParticle(Particle.DUST, currentPos.clone().add(0, 1, 0), 10, 1.5, 0.5, 1.5, new Particle.DustOptions(Color.RED, 1.8f));
                    
                    if (step % 5 == 0 && Math.abs(currentPos.getY() - baseLoc.getY()) < 1.5) {
                        baseLoc.getWorld().spawnParticle(Particle.BLOCK, baseLoc.clone().add(0, 0.1, 0), 30, 1.5, 0.2, 1.5, Material.NETHERRACK.createBlockData());
                        baseLoc.getWorld().playSound(baseLoc, Sound.BLOCK_STONE_BREAK, 1f, 0.5f);
                    }
                } 
                // MOUNT PHASE: Step 100 is Ground Level
                else if (step == 100) {
                    baseLoc.getWorld().strikeLightningEffect(throneSeat.getLocation());
                    throneSeat.addPassenger(p);
                    throneSeats.put(throneSeat.getUniqueId(), p.getUniqueId());
                } 
                // TERMINATION: Disappear after 30 seconds
                else if (step > 600 || !p.isOnline()) {
                    structureParts.forEach(Entity::remove);
                    activeDomain.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }
                step++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
                                            }
    /* ================= RIGHT CLICK: STABLE ORBIT ================= */
    @Override public void rightClick(Player p) { startOrbit(p); }

    public void startOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) return;
        List<Material> mats = Arrays.asList(Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, Material.GRAY_DYE, Material.MUSIC_DISC_5, Material.PINK_DYE);
        List<ArmorStand> cards = new ArrayList<>();
        for (Material m : mats) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setSmall(true); as.setInvisible(true); as.setMarker(true); as.setGravity(false);
            as.getEquipment().setItemInMainHand(new ItemStack(m));
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!p.isOnline() || !isHoldingCard(p)) {
                    stopOrbit(p); this.cancel(); return;
                }
                angle += 0.06;
                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    Location loc = p.getLocation().clone().add(2.3 * Math.cos(angle + offset), 1.2, 2.3 * Math.sin(angle + offset));
                    cards.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    public void stopOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) {
            orbiting.get(p.getUniqueId()).forEach(Entity::remove);
            orbiting.remove(p.getUniqueId());
        }
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD (10 TNT POWER) ================= */
    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "ultimate_sword", 30)) return;
        
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 50);
        Location target = (ray != null && ray.getHitBlock() != null) ? ray.getHitBlock().getLocation() : p.getLocation().add(p.getLocation().getDirection().multiply(15));
        
        ArmorStand sword = p.getWorld().spawn(target.clone().add(0, 35, 0), ArmorStand.class);
        sword.setInvisible(true); sword.setGravity(false); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        sword.setRightArmPose(new EulerAngle(Math.toRadians(180), 0, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                sword.teleport(sword.getLocation().subtract(0, 1.8, 0));
                // Red Dust + Flame on Sword
                sword.getWorld().spawnParticle(Particle.DUST, sword.getLocation(), 5, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.RED, 1.2f));
                sword.getWorld().spawnParticle(Particle.FLAME, sword.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);
                
                if (sword.getLocation().getY() <= target.getY() || sword.getLocation().getBlock().getType().isSolid()) {
                    sword.getWorld().createExplosion(sword.getLocation(), 10f, true, true);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    private boolean isCool(Player p, String key, int sec) {
        long now = System.currentTimeMillis();
        String k = p.getUniqueId() + "_" + key;
        if (cooldowns.containsKey(k) && cooldowns.get(k) > now) return false;
        cooldowns.put(k, now + (sec * 1000L));
        return true;
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
            // ================= FIX: ADDING THE MISSING METHOD =================
    private ArmorStand createThronePart(Location l, Material m) {
        ArmorStand as = l.getWorld().spawn(l, ArmorStand.class);
        as.setInvisible(true); 
        as.setGravity(false); 
        as.setMarker(true);
        as.getEquipment().setHelmet(new ItemStack(m));
        return as;
    }
}
