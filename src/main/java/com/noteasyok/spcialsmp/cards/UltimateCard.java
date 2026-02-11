  return item;
    }
                }
package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.FuelManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.EulerAngle;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeStorm = new HashSet<>();
    private final Map<UUID, Boolean> timeStopped = new HashMap<>();
    private final Map<UUID, Boolean> isSwordRainMode = new HashMap<>();
    private final Map<UUID, Entity> currentDragon = new HashMap<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= LEFT CLICK: WITHER STORM ================ */
    @Override
    public void leftClick(Player p) {
        if (activeStorm.contains(p.getUniqueId()) || !isCool(p, "ultimate_storm", 120)) return;
        if (FuelManager.getFuel(p) < 18000) {
            p.sendMessage("§c§l[!] §6Need 5 Hours of Soul Fuel!");
            return;
        }
        FuelManager.setFuel(p, FuelManager.getFuel(p) - 18000);

        activeStorm.add(p.getUniqueId());
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getInventory().addItem(new ItemStack(Material.CLOCK));

        Location center = p.getLocation().add(0, 15, 0); 
        List<ArmorStand> bodyParts = new ArrayList<>();
        BossBar bossBar = Bukkit.createBossBar("§0§lWITHER STORM", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        bossBar.addPlayer(p);
        
        p.getWorld().setStorm(true);
        p.getWorld().setThundering(true);

        for (int i = 0; i < 150; i++) {
            Vector v = new Vector(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5).normalize().multiply(Math.random() * 8);
            ArmorStand part = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(v), EntityType.ARMOR_STAND);
            part.setInvisible(true); part.setGravity(false); part.setMarker(true);
            part.getEquipment().setHelmet(new ItemStack(i % 5 == 0 ? Material.CRYING_OBSIDIAN : Material.BLACK_CONCRETE));
            bodyParts.add(part);
        }

        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                if (timer > 1200 || !p.isOnline()) {
                    bodyParts.forEach(Entity::remove);
                    bossBar.removeAll();
                    activeStorm.remove(p.getUniqueId());
                    p.setAllowFlight(false);
                    this.cancel();
                    return;
                }
                bossBar.setProgress(1.0 - (double) timer / 1200.0);
                timer++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    } 

    /* ================= RIGHT CLICK: ORBIT + ABILITY ================ */
    @Override
    public void rightClick(Player p) {
        startOrbit(p); // Humesha orbit chalu rakho

        if (isSwordRainMode.getOrDefault(p.getUniqueId(), false)) {
            triggerUltimateSwordRain(p);
        } else {
            RayTraceResult res = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), 25, 1.0, e -> e instanceof LivingEntity && !e.equals(p));
            if (res != null && res.getHitEntity() != null) {
                triggerSoulSteal(p, (LivingEntity) res.getHitEntity());
            } else {
                triggerDragonRide(p);
            }
        }
    }

    /* ================= SHIFT+RIGHT: MODE SWITCH ================ */
    @Override
    public void shiftRightClick(Player p) {
        boolean mode = !isSwordRainMode.getOrDefault(p.getUniqueId(), false);
        isSwordRainMode.put(p.getUniqueId(), mode);
        p.sendTitle("", mode ? "§c§l⚔ SWORD RAIN MODE" : "§5§l🐉 DRAGON GOD MODE", 5, 20, 5);
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    /* ================= NATURAL SWORD RAIN (60s) ================ */
    private void triggerUltimateSwordRain(Player p) {
        if (!isCool(p, "sword_rain", 70)) return;
        p.sendMessage("§c§l⚠ SWORD STORM ACTIVATED (60s)");
        
        new BukkitRunnable() {
            int ticks = 0;
            Random r = new Random();
            @Override
            public void run() {
                if (ticks > 1200 || !p.isOnline() || !isHoldingCard(p)) { this.cancel(); return; }

                for (int i = 0; i < 2; i++) {
                    Location spawn = p.getLocation().add((r.nextDouble()-0.5)*50, 40, (r.nextDouble()-0.5)*50);
                    ItemDisplay sword = (ItemDisplay) spawn.getWorld().spawnEntity(spawn, EntityType.ITEM_DISPLAY);
                    sword.setItemStack(new ItemStack(Material.NETHERITE_SWORD));
                    Transformation tr = sword.getTransformation();
                    tr.getScale().set(6, 6, 6);
                    tr.getLeftRotation().set(new AxisAngle4f((float)Math.toRadians(180), 1, 0, 0));
                    sword.setTransformation(tr);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!sword.isValid()) { this.cancel(); return; }
                            sword.teleport(sword.getLocation().subtract(0, 2.2, 0));
                            if (sword.getLocation().getBlock().getType().isSolid()) {
                                sword.getWorld().createExplosion(sword.getLocation(), 7f, false, false);
                                sword.remove(); this.cancel();
                            }
                        }
                    }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
                }
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= DRAGON RIDE & SOUL STEAL ================ */
    private void triggerDragonRide(Player p) {
        if (currentDragon.containsKey(p.getUniqueId())) {
            currentDragon.get(p.getUniqueId()).remove();
            currentDragon.remove(p.getUniqueId());
            return;
        }
        EnderDragon dragon = (EnderDragon) p.getWorld().spawnEntity(p.getLocation(), EntityType.ENDER_DRAGON);
        dragon.setInvulnerable(true);
        dragon.addPassenger(p);
        currentDragon.put(p.getUniqueId(), dragon);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dragon.isValid() || dragon.getPassengers().isEmpty()) { dragon.remove(); this.cancel(); return; }
                dragon.setVelocity(p.getLocation().getDirection().multiply(1.8));
                dragon.setRotation(p.getLocation().getYaw() + 180, p.getLocation().getPitch());
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private void triggerSoulSteal(Player p, LivingEntity target) {
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 60 || !target.isValid() || !p.isOnline()) { this.cancel(); return; }
                Location s = p.getEyeLocation().subtract(0, 0.4, 0);
                Location e = target.getEyeLocation();
                Vector v = e.toVector().subtract(s.toVector()).normalize();
                for (double d = 0; d < s.distance(e); d += 0.5) {
                    p.getWorld().spawnParticle(Particle.REDSTONE, s.clone().add(v.clone().multiply(d)), 1, new Particle.DustOptions(Color.BLACK, 1.2f));
                }
                if (t % 10 == 0) {
                    target.damage(3);
                    p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), p.getHealth() + 3));
                }
                t += 2;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L);
    }

    /* ================= ORBIT LOGIC (UNCHANGED) ================ */
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
                if (!p.isOnline() || !isHoldingCard(p)) { stopOrbit(p); this.cancel(); return; }
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

    @EventHandler
    public void onTimeInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() != null && e.getItem().getType() == Material.CLOCK && activeStorm.contains(p.getUniqueId())) {
            boolean stop = !timeStopped.getOrDefault(p.getUniqueId(), false);
            timeStopped.put(p.getUniqueId(), stop);
            p.sendMessage(stop ? "§c§lTIME STOPPED" : "§a§lTIME RESUMED");
        }
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING);
    }

    private boolean isCool(Player p, String k, int s) {
        long n = System.currentTimeMillis();
        if (cooldowns.getOrDefault(p.getUniqueId() + k, 0L) > n) return false;
        cooldowns.put(p.getUniqueId() + k, n + (s * 1000L));
        return true;
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§l✯ ULTIMATE GOD CARD ✯");
        meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
        item.setItemMeta(meta);
        return item;
    }
            }
