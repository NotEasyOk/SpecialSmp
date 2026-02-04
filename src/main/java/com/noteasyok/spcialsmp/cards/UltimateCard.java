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
import org.bukkit.util.Vector;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeStorm = new HashSet<>();
    private final Map<UUID, Boolean> timeStopped = new HashMap<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= LEFT CLICK: WITHER STORM & TIME LORD (60s) ================= */
    @Override
    public void leftClick(Player p) {
        if (activeStorm.contains(p.getUniqueId()) || !isCool(p, "ultimate_storm", 120)) return;

        activeStorm.add(p.getUniqueId());
        Location loc = p.getLocation();

        // 1. SUMMON WITHER STORM BOSS (Entity)
        Wither storm = (Wither) p.getWorld().spawnEntity(p.getLocation().add(0, 10, 0), EntityType.WITHER);
        storm.setCustomName("§0§lWITHER STORM");
        storm.setInvulnerable(true);

        // 2. TIME CONTROL CLOCK GIVE
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta cm = clock.getItemMeta();
        cm.setDisplayName("§b§lTIME CONTROL CLOCK");
        clock.setItemMeta(cm);
        p.getInventory().addItem(clock);
        
        // 3. OWNER ABILITIES (Fly + Music)
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getWorld().playSound(p.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 1f, 1f);

        new BukkitRunnable() {
            int timer = 0;
            List<Zombie> minions = new ArrayList<>();

            @Override
            public void run() {
                if (timer > 1200 || !p.isOnline()) { // 60 Seconds
                    activeStorm.remove(p.getUniqueId());
                    timeStopped.remove(p.getUniqueId());
                    p.getInventory().removeItem(clock);
                    p.setAllowFlight(false);
                    p.setFlying(false);
                    minions.forEach(Entity::remove);
                    if (storm != null) storm.remove();
                    p.sendMessage("§6§lThe Wither Power fades away...");
                    this.cancel();
                    return;
                }

                // 4. WITHER STORM PHYSICS (Sucking)
                for (Entity e : storm.getNearbyEntities(20, 20, 20)) {
                    if (e.getUniqueId().equals(p.getUniqueId()) || e instanceof Wither) continue;
                    Vector pull = storm.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.3);
                    e.setVelocity(pull);
                }

                // 5. DARK AURA & LIGHTNING (Owner Visible)
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 40, 1.5, 2.5, 1.5, new Particle.DustOptions(Color.BLACK, 2.0f));
                if (timer % 5 == 0) p.getWorld().strikeLightningEffect(storm.getLocation().add(Math.random()*10-5, 0, Math.random()*10-5));

                // 6. BABY ZOMBIE ARMY (Full Netherite)
                if (timer == 1) {
                    for (int i = -3; i <= 3; i++) {
                        Zombie z = (Zombie) p.getWorld().spawnEntity(p.getLocation().add(i, 0, 3), EntityType.ZOMBIE);
                        z.setBaby(true);
                        z.getEquipment().setArmorContents(new ItemStack[]{
                            new ItemStack(Material.NETHERITE_BOOTS), new ItemStack(Material.NETHERITE_LEGGINGS),
                            new ItemStack(Material.NETHERITE_CHESTPLATE), new ItemStack(Material.NETHERITE_HELMET)
                        });
                        z.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                        minions.add(z);
                    }
                }
                
                // 7. POISON AURA
                for (Entity e : p.getNearbyEntities(10, 10, 10)) {
                    if (e instanceof LivingEntity && !e.equals(p) && !(e instanceof Zombie)) {
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    }
                }
                timer++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= TIME STOP & PROTECTION (FIXED) ================= */
    @EventHandler
    public void onTimeStop(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        // Clock check aur active storm check
        if (p.getInventory().getItemInMainHand().getType() == Material.CLOCK && activeStorm.contains(p.getUniqueId())) {
            
            // Toggle logic
            boolean isStopped = !timeStopped.getOrDefault(p.getUniqueId(), false);
            timeStopped.put(p.getUniqueId(), isStopped);
            
            if (isStopped) {
                p.sendMessage("§c§lTIME STOPPED");
                // Sabhi paas waale mobs ko jamado (Slowness 255 se wo hil nahi payenge)
                for (Entity ent : p.getNearbyEntities(30, 30, 30)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        LivingEntity le = (LivingEntity) ent;
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 255, false, false));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 600, 250, false, false));
                    }
                }
            } else {
                p.sendMessage("§a§lTIME RESUMED");
                // Effects hata do
                for (Entity ent : p.getNearbyEntities(30, 30, 30)) {
                    if (ent instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) ent;
                        le.removePotionEffect(PotionEffectType.SLOWNESS);
                        le.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        // Owner ko damage nahi hoga jab tak storm active hai
        if (e.getEntity() instanceof Player && activeStorm.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    /* ================= RIGHT CLICK: STABLE ORBIT (No Changes) ================= */
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

    /* ================= SHIFT + RIGHT: GIANT SWORD (No Changes) ================= */
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
                sword.getWorld().spawnParticle(Particle.DUST, sword.getLocation(), 5, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.RED, 1.2f));
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
            }
