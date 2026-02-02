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
import org.bukkit.util.Vector;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> hammerMode = new HashSet<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
        
        // AUTO-RESET CHECKER: Har 5 ticks mein check karega ki player ne hammer pakda hai ya nahi
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new HashSet<>(hammerMode)) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline() || !isHoldingHammer(p)) {
                        forceDisableThor(p != null ? p : null, uuid);
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }
    @Override
public int getModelData() { 
    return 0; 
}

    /* ================= 1. ORBIT SYSTEM (Paas Wala) ================= */
    public void startOrbit(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return;
        stopOrbit(p);
        List<Material> mats = Arrays.asList(Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, Material.GRAY_DYE, Material.MUSIC_DISC_5, Material.PINK_DYE);
        List<ArmorStand> cards = new ArrayList<>();
        for (Material mat : mats) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true); as.setMarker(true); as.setGravity(false); as.setSmall(true);
            as.getEquipment().setItemInMainHand(new ItemStack(mat));
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);
        new BukkitRunnable() {
            double angle = 0;
            @Override public void run() {
                if (!p.isOnline() || hammerMode.contains(p.getUniqueId()) || !isHoldingCard(p)) {
                    stopOrbit(p); this.cancel(); return;
                }
                angle += 0.15;
                Location center = p.getLocation().add(0, 1.2, 0);
                for (int i = 0; i < cards.size(); i++) {
                    double x = 1.2 * Math.cos(angle + (2 * Math.PI / cards.size()) * i);
                    double z = 1.2 * Math.sin(angle + (2 * Math.PI / cards.size()) * i);
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

    /* ================= 2. THOR ABILITIES ================= */
    @EventHandler
    public void onThorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingCard(p)) return;

        if (e.getAction().name().contains("RIGHT")) {
            if (!p.isSneaking()) {
                e.setCancelled(true);
                toggleThorMode(p);
            } else if (hammerMode.contains(p.getUniqueId())) {
                e.setCancelled(true);
                p.setVelocity(p.getLocation().getDirection().multiply(2.5));
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1f);
                new BukkitRunnable() {
                    @Override public void run() {
                        if (p.isOnGround()) { createAtomicBoom(p.getLocation(), p); this.cancel(); }
                    }
                }.runTaskTimer(SpcialSmp.get(), 5L, 1L);
            }
        } else if (e.getAction().name().contains("LEFT")) {
            if (hammerMode.contains(p.getUniqueId())) {
                e.setCancelled(true);
                Trident hammer = p.launchProjectile(Trident.class);
                hammer.setCustomName("ThorHammer");
                hammer.setVelocity(p.getLocation().getDirection().multiply(3.0));
            } else {
                Block target = p.getTargetBlock(null, 100);
                if (target.getType() != Material.AIR) p.getWorld().strikeLightning(target.getLocation());
            }
        }
    }

    @EventHandler
    public void onHammerHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Trident t && "ThorHammer".equals(t.getCustomName())) {
            Location loc = t.getLocation();
            loc.getWorld().strikeLightning(loc);
            loc.getWorld().createExplosion(loc, 4.0f, false, false, (Player) t.getShooter());
            t.remove(); // Hammer hit hote hi gayab
        }
    }

    private void toggleThorMode(Player p) {
        if (hammerMode.contains(p.getUniqueId())) {
            forceDisableThor(p, p.getUniqueId());
        } else {
            hammerMode.add(p.getUniqueId());
            stopOrbit(p);
            ItemStack hammer = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK);
            ItemMeta meta = hammer.getItemMeta();
            meta.setDisplayName("§b§lTHOR'S HAMMER");
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            hammer.setItemMeta(meta);
            p.getInventory().setItemInMainHand(hammer);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 4, false, false));
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            p.sendMessage("§b§l⚡ THOR MODE: ENABLED");
        }
    }

    private void forceDisableThor(Player p, UUID uuid) {
        hammerMode.remove(uuid);
        if (p != null && p.isOnline()) {
            p.getInventory().setItemInMainHand(getItemStackWithLore(getName()));
            p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
            p.sendMessage("§c§l⚡ THOR MODE: DISABLED");
            startOrbit(p);
        }
    }

    private void createAtomicBoom(Location loc, Player p) {
        loc.getWorld().strikeLightning(loc);
        loc.getWorld().createExplosion(loc, 10f, false, false, p);
        for (int i = 0; i < 10; i++) {
            Block b = loc.clone().add((Math.random()-0.5)*6, -1, (Math.random()-0.5)*6).getBlock();
            if (b.getType() != Material.AIR) {
                FallingBlock fb = loc.getWorld().spawnFallingBlock(b.getLocation().add(0, 1, 0), b.getBlockData());
                fb.setVelocity(new Vector(0, 0.4, 0)); fb.setDropItem(false);
            }
        }
    }

    @Override
    public void shiftRightClick(Player p) {
        if (hammerMode.contains(p.getUniqueId()) || !isCool(p, "giant", 15)) return;
        Location target = p.getTargetBlock(null, 50).getLocation();
        ArmorStand sword = p.getWorld().spawn(target.clone().add(0, 40, 0), ArmorStand.class);
        sword.setInvisible(true); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(12.0);
        new BukkitRunnable() {
            @Override public void run() {
                sword.teleport(sword.getLocation().subtract(0, 1.8, 0));
                if (sword.getLocation().getY() <= target.getY() + 1) {
                    p.getWorld().createExplosion(sword.getLocation(), 25f, true, false, p);
                    p.getWorld().strikeLightning(sword.getLocation());
                    sword.remove(); this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        return "Ultimate Card".equals(item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING));
    }

    private boolean isHoldingHammer(Player p) {
        return isHoldingCard(p) && p.getInventory().getItemInMainHand().getType() == Material.WARPED_FUNGUS_ON_A_STICK;
    }

    private boolean isCool(Player p, String key, int sec) {
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId() + key, 0L) > now) return false;
        cooldowns.put(p.getUniqueId() + key, now + (sec * 1000L));
        return true;
    }

    @Override public void leftClick(Player p) {}
    @Override public ItemStack getItemStackWithLore(String name) {
        ItemStack item = createItem(); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lUltimate Card");
        meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
        item.setItemMeta(meta); return item;
    }
                }
