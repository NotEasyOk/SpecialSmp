package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> hammerMode = new HashSet<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Ultimate Card"; }
    @Override
    public int getModelData() { return 0; }
    @Override
    public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= 1. FIXED ORBIT SYSTEM (9 CARDS) ================= */
    public void startOrbit(Player p) {
        stopOrbit(p);
        if (hammerMode.contains(p.getUniqueId())) return;

        // Aapke bataye huye 9 specific materials
        List<Material> mats = Arrays.asList(
            Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, 
            Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, 
            Material.GRAY_DYE, Material.MUSIC_DISC_5, Material.PINK_DYE
        );
        
        List<ArmorStand> cards = new ArrayList<>();
        for (Material mat : mats) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true); as.setMarker(true); as.setGravity(false); as.setSmall(true);
            as.getEquipment().setItemInMainHand(new ItemStack(mat));
            // Card jaisa dikhane ke liye angle
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);

        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                // FIX: Hath se hata ne par ya online na hone par ORBIT OFF
                if (!p.isOnline() || !isHoldingCard(p) || hammerMode.contains(p.getUniqueId())) {
                    stopOrbit(p);
                    this.cancel();
                    return;
                }

                angle += 0.12; // Smooth rotation speed
                Location center = p.getLocation().add(0, 1.2, 0);

                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    double x = 2.8 * Math.cos(angle + offset);
                    double z = 2.8 * Math.sin(angle + offset);
                    
                    // Smooth teleportation
                    cards.get(i).teleport(center.clone().add(x, 0, z));
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

    /* ================= 2. THOR MODE (FLIGHT & ATOMIC) ================= */
    @Override
    public void rightClick(Player p) {
        UUID uuid = p.getUniqueId();
        if (!hammerMode.contains(uuid)) {
            hammerMode.add(uuid);
            p.getInventory().getItemInMainHand().setType(Material.WARPED_FUNGUS_ON_A_STICK);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 4, false, false));
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            p.sendMessage("§b§l⚡ THOR MODE: §aENABLED");
            stopOrbit(p);
        }
    }

    private void disableThor(Player p) {
        hammerMode.remove(p.getUniqueId());
        if (p.getInventory().getItemInMainHand().getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
            p.getInventory().getItemInMainHand().setType(Material.GREEN_DYE);
        }
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        p.sendMessage("§b§l⚡ THOR MODE: §cDISABLED");
        startOrbit(p);
    }

    @EventHandler
    public void onThorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!hammerMode.contains(p.getUniqueId())) return;
        if (p.getInventory().getItemInMainHand().getType() != Material.WARPED_FUNGUS_ON_A_STICK) return;

        e.setCancelled(true);

        // Right Click to Disable
        if (e.getAction().name().contains("RIGHT") && !p.isSneaking()) {
            disableThor(p);
        } 
        // Shift + Right Click to Fly (Riptide Style)
        else if (e.getAction().name().contains("RIGHT") && p.isSneaking()) {
            p.setVelocity(p.getLocation().getDirection().multiply(3.0));
            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1f);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (p.isOnGround()) {
                        createAtomicBoom(p.getLocation(), p);
                        this.cancel();
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 5L, 1L);
        }
        // Left Click to throw Atomic Trident
        else if (e.getAction().name().contains("LEFT")) {
            Trident hammer = p.launchProjectile(Trident.class);
            hammer.setCustomName("AtomicHammer");
            hammer.setVelocity(p.getLocation().getDirection().multiply(3.5));
        }
    }

    /* ================= 3. EARTHQUAKE & SWORD (ADVANCED) ================= */
    private void createAtomicBoom(Location loc, Player p) {
        loc.getWorld().strikeLightning(loc);
        loc.getWorld().createExplosion(loc, 15f, false, false, p);
        
        // Earthquake Visuals (Falling Blocks)
        for (int i = 0; i < 15; i++) {
            double rx = (Math.random() - 0.5) * 10;
            double rz = (Math.random() - 0.5) * 10;
            Block b = loc.clone().add(rx, -1, rz).getBlock();
            if (b.getType() != Material.AIR) {
                FallingBlock fb = loc.getWorld().spawnFallingBlock(b.getLocation().add(0, 1, 0), b.getBlockData());
                fb.setVelocity(new Vector(0, 0.5, 0));
                fb.setDropItem(false);
            }
        }
    }

    @Override
    public void shiftRightClick(Player p) {
        if (hammerMode.contains(p.getUniqueId()) || !isCool(p, "sword", 15)) return;

        Location target = p.getTargetBlock(null, 50).getLocation();
        ArmorStand sword = p.getWorld().spawn(target.clone().add(0, 30, 0), ArmorStand.class);
        sword.setInvisible(true); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                sword.teleport(sword.getLocation().subtract(0, 1.5, 0));
                if (sword.getLocation().getY() <= target.getY()) {
                    p.getWorld().createExplosion(sword.getLocation(), 25f, false, false, p); // Huge Explosion
                    p.getWorld().strikeLightning(sword.getLocation());
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
    }

    /* ================= UTILS ================= */
    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING);
    }

    private boolean isCool(Player p, String key, int sec) {
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId() + key, 0L) > now) return false;
        cooldowns.put(p.getUniqueId() + key, now + (sec * 1000L));
        return true;
    }

    @Override
    public void leftClick(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return;
        p.getWorld().strikeLightning(p.getTargetBlock(null, 50).getLocation());
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = createItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lUltimate Card");
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
        item.setItemMeta(meta);
        return item;
    }
                }
