package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.block.Action;

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

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial()); 
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7The most powerful card in existence.");
            lore.add("");
            lore.add("§eLeft-Click: §fLightning Strike");
            lore.add("§eRight-Click: §bToggle Hammer Mode");
            lore.add("§eShift + Right: §fGiant Sword Drop");
            meta.setLore(lore);
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    /* ================= RIGHT CLICK: TOGGLE & THOR ABILITIES ================= */
    @Override
    public void rightClick(Player p) {
        UUID uuid = p.getUniqueId();
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!hammerMode.contains(uuid)) {
            hammerMode.add(uuid);
            item.setType(Material.WARPED_FUNGUS_ON_A_STICK);
            p.sendMessage("§6§l[!] §eThor Mode: §aENABLED §7(Shift+Right to Fly)");
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
            if (orbiting.containsKey(uuid)) {
                orbiting.get(uuid).forEach(Entity::remove);
                orbiting.remove(uuid);
            }
        } else {
            hammerMode.remove(uuid);
            item.setType(Material.GREEN_DYE);
            p.sendMessage("§6§l[!] §eThor Mode: §cDISABLED §7(Orbit Active)");
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            startOrbit(p);
        }
    }

    @EventHandler
    public void onThorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!hammerMode.contains(p.getUniqueId()) || !isHoldingCard(p)) return;

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true); 
            if (p.isSneaking()) {
                // RIPTIDE FLIGHT
                Vector dir = p.getLocation().getDirection().multiply(2.5);
                p.setVelocity(dir);
                p.getWorld().spawnParticle(Particle.SONIC_BOOM, p.getLocation(), 1);
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.2f);

                // THOR LANDING + EARTHQUAKE
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.isOnGround()) {
                            Location loc = p.getLocation();
                            loc.getWorld().createExplosion(loc, 20.0f, true, true);
                            loc.getWorld().strikeLightning(loc);
                            
                            // Earthquake Effect
                            loc.getWorld().getNearbyEntities(loc, 15, 8, 15).forEach(entity -> {
                                if (entity instanceof LivingEntity le && !entity.equals(p)) {
                                    le.setVelocity(new Vector(0, 1.2, 0)); // Upar uchalna
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
                                }
                            });
                            this.cancel();
                        }
                        if (!p.isOnline() || !hammerMode.contains(p.getUniqueId())) this.cancel();
                    }
                }.runTaskTimer(SpcialSmp.get(), 5L, 1L);
            } else {
                // ATOMIC THROW
                if (!isCool(p, "hammer_throw", 3)) return;
                Trident hammer = p.launchProjectile(Trident.class);
                hammer.setVelocity(p.getLocation().getDirection().multiply(3.0));
                hammer.setCustomName("AtomicHammer");
                hammer.setItemStack(new ItemStack(Material.FISHING_ROD));
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 0.5f);
            }
        }
    }

    @EventHandler
    public void onNukeHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident hammer && "AtomicHammer".equals(hammer.getCustomName())) {
            Location loc = hammer.getLocation();
            loc.getWorld().createExplosion(loc, 20.0f, true, true);
            loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5);
            hammer.remove();
        }
    }

    /* ================= LEFT CLICK: LIGHTNING (UNCHANGED) ================= */
    @Override
    public void leftClick(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return;
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 60);
        Location hitLoc = (r != null && r.getHitPosition() != null) ? r.getHitPosition().toLocation(p.getWorld()) : p.getLocation().add(p.getLocation().getDirection().multiply(20));
        p.getWorld().strikeLightning(hitLoc);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD (UNCHANGED) ================= */
    @Override
    public void shiftRightClick(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return;
        int cd = 30;
        if (!isCool(p, "ultimate_sword", cd)) return;
        Vector lookDir = p.getLocation().getDirection();
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), lookDir, 100);
        Location targetLoc = (ray != null && ray.getHitPosition() != null) ? ray.getHitPosition().toLocation(p.getWorld()) : p.getLocation().add(lookDir.multiply(25));
        Location spawnLoc = targetLoc.clone().add(0, 35, 0);
        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true); sword.setGravity(false); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location currentLoc = sword.getLocation().subtract(0, 1.2, 0);
                sword.teleport(currentLoc);
                if (currentLoc.getBlock().getType().isSolid() || currentLoc.getY() <= targetLoc.getY()) {
                    p.getWorld().createExplosion(currentLoc, 15F, true, true, p);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= ORBIT LOGIC (UNCHANGED) ================= */
    public void startOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId()) || hammerMode.contains(p.getUniqueId())) return;
        List<Material> cardMaterials = Arrays.asList(Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, Material.GRAY_DYE, Material.MUSIC_DISC_5, Material.PINK_DYE);
        List<ArmorStand> cards = new ArrayList<>();
        for (Material mat : cardMaterials) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true); as.setMarker(true); as.setGravity(false); as.setSmall(true);
            as.getEquipment().setItemInMainHand(new ItemStack(mat));
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!p.isOnline() || !isHoldingCard(p) || hammerMode.contains(p.getUniqueId())) {
                    cards.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }
                angle += 0.12; 
                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    double x = 2.8 * Math.cos(angle + offset);
                    double z = 2.8 * Math.sin(angle + offset);
                    Location loc = p.getLocation().clone().add(x, 1.2, z);
                    loc.setDirection(p.getLocation().toVector().subtract(loc.toVector()));
                    cards.get(i).teleport(loc);
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

    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        if (cooldowns.containsKey(mapKey) && cooldowns.get(mapKey) > now) return false;
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
                              }
