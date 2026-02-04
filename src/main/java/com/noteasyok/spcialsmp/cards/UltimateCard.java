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
        
        // 1. SUMMON WITHER STORM BOSS
        Wither storm = (Wither) p.getWorld().spawnEntity(p.getLocation().add(0, 15, 0), EntityType.WITHER);
        storm.setCustomName("§0§lWITHER STORM");
        storm.setInvulnerable(true);

        // 2. TIME CONTROL CLOCK GIVE
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta cm = clock.getItemMeta();
        cm.setDisplayName("§b§lTIME CONTROL CLOCK");
        clock.setItemMeta(cm);
        p.getInventory().addItem(clock);
        
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2f, 0.5f);

        new BukkitRunnable() {
            int timer = 0;
            List<Zombie> minions = new ArrayList<>();

            @Override
            public void run() {
                if (timer > 1200 || !p.isOnline()) {
                    activeStorm.remove(p.getUniqueId());
                    timeStopped.remove(p.getUniqueId());
                    p.getInventory().removeItem(clock);
                    p.setAllowFlight(false);
                    minions.forEach(Entity::remove);
                    if (storm != null) storm.remove();
                    this.cancel();
                    return;
                }

                // 3. ZOMBIE ARMY IN A LINE (Max Netherite + Anti-Owner Logic)
                if (timer == 1) {
                    Location center = p.getLocation().add(p.getLocation().getDirection().multiply(3));
                    Vector side = new Vector(-p.getLocation().getDirection().getZ(), 0, p.getLocation().getDirection().getX()).normalize();
                    
                    for (int i = -3; i <= 3; i++) {
                        Location spawnLoc = center.clone().add(side.clone().multiply(i));
                        Zombie z = (Zombie) p.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                        z.setBaby(true);
                        z.setCustomName("§6" + p.getName() + "'s Guard");
                        
                        // Full Max Netherite Armor
                        z.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                        z.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                        z.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                        z.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                        z.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                        
                        // Anti-Owner Target Logic: Owner ko target nahi karenge
                        z.setTarget(null);
                        minions.add(z);
                    }
                }

                // Owner Protection: Agar koi minion owner ko galti se target kare toh hata do
                minions.forEach(z -> {
                    if (z.getTarget() != null && z.getTarget().equals(p)) {
                        z.setTarget(null);
                    }
                });

                // 4. TRACTOR BEAM & SUCK PHYSICS (Image 4 Style)
                for (Entity e : storm.getNearbyEntities(25, 25, 25)) {
                    if (e.equals(p) || e.equals(storm) || minions.contains(e)) continue;
                    
                    Vector dir = storm.getLocation().toVector().subtract(e.getLocation().toVector()).normalize();
                    
                    // Purple Beam Visual (Image 1 reference)
                    Location beamPoint = e.getLocation();
                    for(double d = 0; d < beamPoint.distance(storm.getLocation()); d += 1.5) {
                        beamPoint.getWorld().spawnParticle(Particle.WITCH, beamPoint.clone().add(dir.clone().multiply(d)), 1, 0, 0, 0, 0);
                    }
                    
                    if (!timeStopped.getOrDefault(p.getUniqueId(), false)) {
                        e.setVelocity(dir.multiply(0.45));
                    }
                }

                storm.getWorld().spawnParticle(Particle.LARGE_SMOKE, storm.getLocation(), 80, 4, 4, 4, 0.05);
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
