package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
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

    /* ================= LEFT CLICK: WITHER STORM ================*/
@Override
public void leftClick(Player p) {
        if (activeStorm.contains(p.getUniqueId()) || !isCool(p, "ultimate_storm", 120)) return;

        activeStorm.add(p.getUniqueId());
        Location center = p.getLocation().add(0, 30, 0); 
        List<ArmorStand> bodyParts = new ArrayList<>();
        List<ArmorStand> tentacles = new ArrayList<>();

        // 1. BOSS BAR & WORLD APOCALYPSE
        BossBar bossBar = Bukkit.createBossBar("§0§lWITHER STORM", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1.0);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        p.getWorld().setStorm(true);
        p.getWorld().setThundering(true);
        p.getWorld().setFullTime(18000);

        // 2. SUMMON GIANT CORE (150+ Blocks for massive volume)
        for (int i = 0; i < 150; i++) {
            ArmorStand part = (ArmorStand) center.getWorld().spawnEntity(
                center.clone().add(Math.random()*20-10, Math.random()*15-7.5, Math.random()*20-10), 
                EntityType.ARMOR_STAND
            );
            part.setInvisible(true);
            part.setGravity(false);
            part.setMarker(true);
            part.getEquipment().setHelmet(new ItemStack(i % 5 == 0 ? Material.CRYING_OBSIDIAN : Material.BLACK_CONCRETE));
            bodyParts.add(part);
        }

        // 3. SUMMON 8 GIANT TENTACLES (Each 10-15 blocks long)
        for (int t = 0; t < 8; t++) {
            for (int segment = 0; segment < 12; segment++) {
                ArmorStand s = (ArmorStand) center.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
                s.setInvisible(true); s.setGravity(false); s.setMarker(true);
                s.getEquipment().setHelmet(new ItemStack(Material.BLACK_CONCRETE));
                tentacles.add(s);
            }
        }

        // 4. THE 3 ELDER HEADS
        Wither[] heads = new Wither[3];
        heads[0] = (Wither) center.getWorld().spawnEntity(center.clone().add(6, 0, 0), EntityType.WITHER);
        heads[1] = (Wither) center.getWorld().spawnEntity(center.clone().add(-6, 0, 0), EntityType.WITHER);
        heads[2] = (Wither) center.getWorld().spawnEntity(center.clone().add(0, 5, 6), EntityType.WITHER);
        for(Wither h : heads) { h.setInvulnerable(true); h.setCustomName("§5§lSTORM HEAD"); }

        // 5. BABY ZOMBIE ELITE GUARDS
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        for (int i = -4; i <= 4; i++) {
            Location zLoc = p.getLocation().add(dir.multiply(7)).add(side.multiply(i));
            Zombie z = (Zombie) p.getWorld().spawnEntity(zLoc, EntityType.ZOMBIE);
            z.setBaby(true);
            z.getEquipment().setArmorContents(new ItemStack[]{new ItemStack(Material.NETHERITE_BOOTS), new ItemStack(Material.NETHERITE_LEGGINGS), new ItemStack(Material.NETHERITE_CHESTPLATE), new ItemStack(Material.NETHERITE_HELMET)});
            z.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        }

        new BukkitRunnable() {
            int timer = 0;
            double wave = 0;

            @Override
            public void run() {
                if (timer > 1200 || !p.isOnline()) {
                    bodyParts.forEach(Entity::remove);
                    tentacles.forEach(Entity::remove);
                    for(Wither h : heads) h.remove();
                    bossBar.removeAll();
                    activeStorm.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                bossBar.setProgress(1.0 - (double) timer / 1200.0);
                wave += 0.2;

                // TENTACLE PHYSICS (Sinuous Movement)
                for (int t = 0; t < 8; t++) {
                    double angle = (2 * Math.PI / 8) * t;
                    for (int s = 0; s < 12; s++) {
                        double dist = s * 1.8;
                        double x = Math.cos(angle) * dist + (Math.sin(wave + s) * 2);
                        double z = Math.sin(angle) * dist + (Math.cos(wave + s) * 2);
                        double y = Math.sin(wave * 0.5 + s) * 3;
                        tentacles.get(t * 12 + s).teleport(center.clone().add(x, y, z));
                    }
                }

                // WORLD DESTRUCTION & TRACTOR BEAM
                for (Entity e : center.getWorld().getNearbyEntities(center, 50, 50, 50)) {
                    if (e.equals(p) || e instanceof Wither || e instanceof ArmorStand || e instanceof Zombie) continue;
                    
                    Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize();
                    
                    // Dense Purple Beam
                    Location bPoint = e.getLocation();
                    for(double d = 0; d < 35; d += 3) {
                        center.getWorld().spawnParticle(Particle.WITCH, bPoint.clone().add(pull.clone().multiply(d)), 30, 0.6, 0.6, 0.6, 0);
                    }
                    
                    if (!timeStopped.getOrDefault(p.getUniqueId(), false)) {
                        e.setVelocity(pull.multiply(0.95)); // Super Strong Sucking
                        if (timer % 10 == 0) p.getWorld().strikeLightning(e.getLocation());
                    }
                }
                
                // Massive Core Particles
                center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center, 600, 15, 12, 15, 0.1);
                center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 200, 10, 10, 10, 0.2);
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
            
            // --- Naya Lore Section ---
            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            lore.add("§e§lSPECIAL ABILITIES:");
            lore.add("§7▶ §bLeft Click: §fSummon Wither Storm & Guards");
            lore.add("§7▶ §bClock Item: §fControl/Stop Time");
            lore.add("§7▶ §bRight Click: §fOrbiting Card Shield");
            lore.add("§7▶ §bShift + Right: §fGiant Sword Nuke");
            lore.add("");
            lore.add("§e§lPASSIVE PERKS:");
            lore.add("§7▶ §6Invincible while Storm is active!");
            lore.add("§7▶ §6Zombies protect the owner!");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);
            // -------------------------

            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
}
