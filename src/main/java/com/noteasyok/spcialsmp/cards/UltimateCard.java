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
        ItemStack item = createItem(); 
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7The most powerful card in existence.");
            lore.add("");
            lore.add("§eLeft-Click: §fLightning Strike");
            lore.add("§eRight-Click: §b[TOGGLE] Thor Mode"); 
            lore.add("§f  - Throw: §cAtomic Nuke");
            lore.add("§f  - Land: §4Earthquake Blast");
            lore.add("§eShift + Right: §fGiant Sword Drop");
            meta.setLore(lore);
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void rightClick(Player p) {
        UUID uuid = p.getUniqueId();
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!hammerMode.contains(uuid)) {
            hammerMode.add(uuid);
            item.setType(Material.WARPED_FUNGUS_ON_A_STICK);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 4, false, false));
            new BukkitRunnable() {
                @Override
                public void run() { p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); }
            }.runTaskLater(SpcialSmp.get(), 1L);

            p.sendMessage("§6§l[!] §eThor Mode: §aENABLED");
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
            stopOrbit(p);
        } else {
            disableThor(p, item);
        }
    }

    private void disableThor(Player p, ItemStack item) {
        hammerMode.remove(p.getUniqueId());
        if (item != null && item.getType() == Material.WARPED_FUNGUS_ON_A_STICK) {
            item.setType(Material.GREEN_DYE);
        }
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        p.sendMessage("§6§l[!] §eThor Mode: §cDISABLED");
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        startOrbit(p);
    }

    @EventHandler
    public void onThorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!hammerMode.contains(p.getUniqueId()) || !isHoldingCard(p)) return;

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true); 

            if (p.isSneaking()) {
                Vector dir = p.getLocation().getDirection().multiply(2.5);
                p.setVelocity(dir);
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.2f);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.isOnGround()) {
                            createAtomicBoom(p.getLocation(), p);
                            this.cancel();
                        }
                        if (!p.isOnline() || !hammerMode.contains(p.getUniqueId())) this.cancel();
                    }
                }.runTaskTimer(SpcialSmp.get(), 5L, 1L);
            } else {
                if (!isCool(p, "hammer_throw", 2)) return;
                
                Trident hammer = p.launchProjectile(Trident.class);
                hammer.setShooter(p); 
                hammer.setVelocity(p.getLocation().getDirection().multiply(3.0));
                hammer.setCustomName("AtomicHammer");
                hammer.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 0.5f);
            }
        }
    }

    private void createAtomicBoom(Location loc, Player p) {
        loc.getWorld().strikeLightning(loc);
        loc.getWorld().createExplosion(loc, 18.0f, false, false, p);
        
        new BukkitRunnable() {
            int y = 0;
            @Override
            public void run() {
                loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, y, 0), 3, 1, 0.5, 1, 0.1);
                loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, y, 0), 15, 0.8, 0.5, 0.8, 0.05);
                y++;
                if (y > 6) this.cancel();
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 2);

        loc.getWorld().getNearbyEntities(loc, 12, 6, 12).forEach(en -> {
            if (en instanceof LivingEntity le && !en.equals(p)) {
                le.setVelocity(new Vector(0, 1.5, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
            }
        });
    }

    @EventHandler
    public void onNukeHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident hammer && "AtomicHammer".equals(hammer.getCustomName())) {
            createAtomicBoom(hammer.getLocation(), (Player) hammer.getShooter());
            hammer.remove();
        }
    }

    @Override
    public void leftClick(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return;
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 60);
        Location hitLoc = (r != null && r.getHitPosition() != null) ? r.getHitPosition().toLocation(p.getWorld()) : p.getLocation().add(p.getLocation().getDirection().multiply(20));
        p.getWorld().strikeLightning(hitLoc);
    }

    @Override
    public void shiftRightClick(Player p) {
        if (hammerMode.contains(p.getUniqueId())) return; // Thor mode mein sword off rakhi hai
        if (!isCool(p, "ultimate_sword", 20)) return;

        p.sendMessage("§6§l[!] §eSummoning Giant Sword...");
        Location targetLoc = p.getTargetBlock(null, 50).getLocation();
        Location spawnLoc = targetLoc.clone().add(0, 30, 0);

        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true); sword.setGravity(false); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location current = sword.getLocation().subtract(0, 1.5, 0);
                sword.teleport(current);
                p.getWorld().spawnParticle(Particle.CRIT, current, 10, 0.5, 0.5, 0.5, 0.1);
                
                if (current.getY() <= targetLoc.getY() || current.getBlock().getType().isSolid()) {
                    p.getWorld().createExplosion(current, 15f, false, false, p);
                    p.getWorld().strikeLightning(current);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);
    }

    public void startOrbit(Player p) {
        stopOrbit(p);
        if (hammerMode.contains(p.getUniqueId())) return;

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
            @Override
            public void run() {
                // FIX: Agar haath mein card nahi hai to orbit khatam
                if (!p.isOnline() || !isHoldingCard(p) || hammerMode.contains(p.getUniqueId())) {
                    stopOrbit(p);
                    this.cancel();
                    return;
                }
                angle += 0.15;
                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    double x = 3.0 * Math.cos(angle + offset);
                    double z = 3.0 * Math.sin(angle + offset);
                    cards.get(i).teleport(p.getLocation().clone().add(x, 1.2, z));
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
