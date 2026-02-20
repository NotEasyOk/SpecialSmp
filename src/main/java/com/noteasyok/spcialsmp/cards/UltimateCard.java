package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import java.util.stream.Collectors;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.bukkit.metadata.FixedMetadataValue;
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
    private final Set<UUID> activeStorm = new HashSet<>();
    private final Map<UUID, Boolean> timeStopped = new HashMap<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= LEFT CLICK: WITHER STORM (FIXED) ================*/
    @Override
    public void leftClick(Player p) {
        if (activeStorm.contains(p.getUniqueId()) || !isCool(p, "left")) return;

        activeStorm.add(p.getUniqueId());
        
        // --- ADDED: Fly and Clock ---
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getInventory().addItem(new ItemStack(Material.CLOCK));
        p.sendMessage("§e§l[!] §6Storm Active! Fly enabled and Time Clock granted.");

        Location center = p.getLocation().add(0, 15, 0); 
        List<ArmorStand> bodyParts = new ArrayList<>();
        List<ArmorStand> tentacles = new ArrayList<>();

        BossBar bossBar = Bukkit.createBossBar("§0§lWITHER STORM", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1.0);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        
        p.getWorld().setStorm(true);
        p.getWorld().setThundering(true);
        p.getWorld().setFullTime(18000);

        org.bukkit.WorldBorder border = p.getWorld().getWorldBorder();
        border.setCenter(p.getLocation());
        border.setSize(5000000);
        border.setWarningDistance(Integer.MAX_VALUE);

        // --- FIXED: Dense Core Formation (Stage 4 style) ---
        for (int i = 0; i < 300; i++) {
            // Random point inside a 10-block sphere for massive volume
            Vector v = new Vector(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5).normalize().multiply(Math.random() * 8);
            ArmorStand part = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(v), EntityType.ARMOR_STAND);
            part.setInvisible(true);
            part.setGravity(false);
            part.setMarker(true);
            part.getEquipment().setHelmet(new ItemStack(i % 5 == 0 ? Material.CRYING_OBSIDIAN : Material.BLACK_CONCRETE));
            bodyParts.add(part);
        }

        for (int t = 0; t < 8; t++) {
            for (int segment = 0; segment < 12; segment++) {
                ArmorStand s = (ArmorStand) center.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
                s.setInvisible(true); s.setGravity(false); s.setMarker(true);
                s.getEquipment().setHelmet(new ItemStack(Material.BLACK_CONCRETE));
                tentacles.add(s);
            }
        }

        Wither[] heads = new Wither[3];
        heads[0] = (Wither) center.getWorld().spawnEntity(center.clone().add(8, 5, 0), EntityType.WITHER);
        heads[1] = (Wither) center.getWorld().spawnEntity(center.clone().add(-8, 5, 0), EntityType.WITHER);
        heads[2] = (Wither) center.getWorld().spawnEntity(center.clone().add(0, 10, 8), EntityType.WITHER);
        for(Wither h : heads) { 
            h.setInvulnerable(true); 
            h.setCustomName("§5§lSTORM HEAD"); 
        }

        // --- FIXED: Baby Zombie Guards Spawn ---
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        for (int i = -2; i <= 2; i++) {
            Location zLoc = p.getLocation().add(dir.clone().multiply(6)).add(side.clone().multiply(i * 2)).add(0, 1, 0);
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
                    p.getWorld().getWorldBorder().setWarningDistance(0);
                    p.setAllowFlight(false);
                    this.cancel();
                    return;
                }

                bossBar.setProgress(1.0 - (double) timer / 1200.0);
                wave += 0.2;

                // --- ADDED: Aimbot Targets OR Random 20 TNT Ground Bombing ---
if (timer % 15 == 0) {
    for (Wither h : heads) {
        // Dushman dhundna (50 block radius)
        List<Entity> targets = h.getNearbyEntities(50, 50, 50).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(p) && !(e instanceof Zombie) && !(e instanceof ArmorStand))
            .collect(java.util.stream.Collectors.toList());

        Vector targetVector;

        if (!targets.isEmpty()) {
            // 1. Agar dushman mila -> Aimbot
            Entity target = targets.get(0);
            targetVector = target.getLocation().toVector().subtract(h.getEyeLocation().toVector()).normalize();
        } else {
            // 2. Agar koi nahi hai -> Random Ground Bombing (Radius 60)
            double xOff = (Math.random() - 0.5) * 60;
            double zOff = (Math.random() - 0.5) * 60;
            // -25 coordinate tak niche fire karega
            Location groundTarget = h.getLocation().clone().add(xOff, -25, zOff);
            targetVector = groundTarget.toVector().subtract(h.getEyeLocation().toVector()).normalize();
        }

        // Skull shoot karna
        WitherSkull skull = h.launchProjectile(WitherSkull.class, targetVector.multiply(1.5));
        skull.setCharged(true); // Blue Skull
        
        // --- 20 TNT POWER SETTING ---
        // 20.0F = Khatarnak Dhamaka!
        skull.setYield(20.0F); 
      }
   }

                // TENTACLE PHYSICS
                for (int t = 0; t < 8; t++) {
                    double angle = (2 * Math.PI / 8) * t;
                    for (int s = 0; s < 12; s++) {
                        double dist = s * 1.6;
                        double x = Math.cos(angle) * dist + (Math.sin(wave + s) * 2);
                        double z = Math.sin(angle) * dist + (Math.cos(wave + s) * 2);
                        double y = Math.sin(wave * 0.5 + s) * 2.5;
                        tentacles.get(t * 12 + s).teleport(center.clone().add(x, y, z));
                    }
                }

                // TRACTOR BEAM
                for (Entity e : center.getWorld().getNearbyEntities(center, 50, 50, 50)) {
                    if (e.equals(p) || e instanceof Wither || e instanceof ArmorStand || e instanceof Zombie) continue;
                    Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize();
                    
                    Location bPoint = e.getLocation();
                    for(double d = 0; d < 35; d += 4) {
                        center.getWorld().spawnParticle(Particle.WITCH, bPoint.clone().add(pull.clone().multiply(d)), 20, 0.5, 0.5, 0.5, 0);
                    }
                    
                    if (!timeStopped.getOrDefault(p.getUniqueId(), false)) {
                        e.setVelocity(pull.multiply(0.8));
                        if (timer % 10 == 0) p.getWorld().strikeLightning(e.getLocation());
                    }
                }
                
                center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center, 400, 10, 8, 10, 0.05);
                timer++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    } 

    /* Baaki Right Click, Shift Right, aur helper methods same rakhein... */
    @EventHandler
    public void onTimeStop(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() != null && e.getItem().getType() == Material.CLOCK && activeStorm.contains(p.getUniqueId())) {
            boolean isStopped = !timeStopped.getOrDefault(p.getUniqueId(), false);
            timeStopped.put(p.getUniqueId(), isStopped);
            if (isStopped) {
                p.sendMessage("§c§lTIME STOPPED");
                p.setMetadata("time_frozen", new FixedMetadataValue(SpcialSmp.get(), true));
                for (Entity ent : p.getNearbyEntities(30, 30, 30)) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 255, false, false));
                    }
                }
            } else {
                p.sendMessage("§a§lTIME RESUMED");
                p.removeMetadata("time_frozen", SpcialSmp.get());
                for (Entity ent : p.getNearbyEntities(30, 30, 30)) {
                    if (ent instanceof LivingEntity) ((LivingEntity) ent).removePotionEffect(PotionEffectType.SLOWNESS);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && activeStorm.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

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

    /* ================= SHIFT + RIGHT CLICK: GIANT SWORD RAIN (1 MIN) ================ */
    @Override
    public void shiftRightClick(Player p) {
        // Cooldown check (taaki spam na ho)
        if (!isCool(p, "shift_right")) {
            p.sendMessage("§cWait for cooldown!");
            return;
        }

        p.sendMessage("§c§l⚠ GIANT SWORD RAIN ACTIVATED! (60s)");
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            final Random random = new Random();
            final Location center = p.getLocation();

            @Override
            public void run() {
                // 200 ticks = 10 seconds (10 seconda)
                if (ticks > 200 || !p.isOnline()) {
                    p.sendMessage("§e§l[!] §fThe Sword Rain has subsided.");
                    this.cancel();
                    return;
                }

                // Har 2 tick mein 1 Giant Sword giregi (Natural Random feel)
                if (ticks % 2 == 0) {
                    // 50-60 block radius mein RANDOM location
                    double xOff = (random.nextDouble() - 0.5) * 60;
                    double zOff = (random.nextDouble() - 0.5) * 60;
                    Location spawnLoc = center.clone().add(xOff, 45, zOff);

                    // Giant Sword spawn using ItemDisplay (Better visuals)
                    ItemDisplay sword = (ItemDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
                    sword.setItemStack(new ItemStack(Material.NETHERITE_SWORD));

                    // Transformation: 6x Size + Handle UP (Pointing Down)
                    Transformation tr = sword.getTransformation();
                    tr.getScale().set(6.5f, 6.5f, 6.5f);
                    tr.getLeftRotation().set(new AxisAngle4f((float) Math.PI,1.0f, 0.0f, 0.0f));
                    tr.getRightRotation().set(new AxisAngle4f((float) (Math.PI / 4), 0.0f, 0.0f, 1.0f));
                    sword.setTransformation(tr);

                    // Falling & Explosion Logic
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!sword.isValid()) { this.cancel(); return; }

                            // Falling speed
                            sword.teleport(sword.getLocation().subtract(0, 2.2, 0));

                            // Trail particles
                            sword.getWorld().spawnParticle(Particle.CRIT, sword.getLocation(), 1, 0, 0, 0, 0);

                            // Collision check (Ground impact)
                            if (sword.getLocation().getBlock().getType().isSolid()) {
                                // 2 TNT Power = 8.0F (1 TNT is 4.0F)
                                sword.getWorld().createExplosion(sword.getLocation(), 8.0f, false, false);
                                sword.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, sword.getLocation(), 1);
                                sword.remove();
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
                }
                ticks++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    private boolean isCool(Player p, String action) {
    // Purana 'seconds' wala logic hata do, manager config se khud seconds uthayega
    if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), action)) {
        long remaining = SpcialSmp.get().getCooldownManager().getRemainingSeconds(p, getName(), action);
        p.sendMessage("§cWait " + remaining + "s");
        return false;
    }

    // Cooldown apply manager ke through karo
    SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), action);
    return true;
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
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
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
          }
