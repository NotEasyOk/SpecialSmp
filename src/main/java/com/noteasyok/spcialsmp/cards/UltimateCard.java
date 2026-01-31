package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.event.Listener;

import java.util.*;

public class UltimateCard extends BaseCard {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Ultimate Card";
    }
    
    @Override
    public int getModelData() {
        return 0; // Ab CMD ki zarurat nahi hai
    }

    // Naya method BaseCard ke hisaab se
    @Override
    public Material getMaterial() {
        return Material.GREEN_DYE; // Ultimate Card ke liye best item
    }

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
            lore.add("§eRight-Click: §fOrbit Shields");
            lore.add("§eShift + Right: §fGiant Sword Drop");
            meta.setLore(lore);
            meta.setCustomModelData(getModelData());
            
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /* ================= LEFT CLICK: LIGHTNING ================= */
    @Override
    public void leftClick(Player p) {
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 60);
        Location hitLoc = (r != null && r.getHitPosition() != null) 
                ? r.getHitPosition().toLocation(p.getWorld()) 
                : p.getLocation().add(p.getLocation().getDirection().multiply(20));

        p.getWorld().strikeLightning(hitLoc);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
    }

    /* ================= RIGHT CLICK: ORBIT ================= */
    @Override
    public void rightClick(Player p) {
        startOrbit(p);
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD ================= */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.ultimate.sword_cooldown", 30);
        if (!isCool(p, "ultimate_sword", cd)) return;

        Vector lookDir = p.getLocation().getDirection();
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), lookDir, 100);
        
        Location targetLoc = (ray != null && ray.getHitPosition() != null) 
                ? ray.getHitPosition().toLocation(p.getWorld()) 
                : p.getLocation().add(lookDir.multiply(25));

        Location spawnLoc = targetLoc.clone().add(0, 35, 0);
        
        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true);
        sword.setGravity(false);
        sword.setBasePlate(false);
        sword.setArms(true);
        sword.setMarker(true); 
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        
        sword.setRightArmPose(new EulerAngle(0, 0, 0));
        
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) {
            sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);
        }

        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                Location currentLoc = sword.getLocation().subtract(0, 1.2, 0);
                sword.teleport(currentLoc);

                boolean hitBlock = currentLoc.clone().add(0, -1.5, 0).getBlock().getType().isSolid();

                if (hitBlock || currentLoc.getY() <= targetLoc.getY() || life > 150) {
                    p.getWorld().createExplosion(currentLoc, 15F, true, true, p);
                    
                    currentLoc.getWorld().getNearbyEntities(currentLoc, 12, 12, 12).forEach(entity -> {
                        if (entity instanceof LivingEntity target && !entity.equals(p)) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                        }
                    });

                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, currentLoc, 5);
                    p.getWorld().playSound(currentLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= ORBIT LOGIC (FIXED FOR MULTI-MATERIAL) ================= */
    public void startOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) return;

        // Sabhi 9 cards ke materials ki list
        List<Material> cardMaterials = Arrays.asList(
            Material.DISC_FRAGMENT_5, // Creeper
            Material.CHORUS_FRUIT,     // Enderman
            Material.PURPLE_DYE,      // Herobrine
            Material.BLACK_DYE,       // Zombie
            Material.WHITE_DYE,       // Ghost
            Material.YELLOW_DYE,      // Lightning
            Material.GRAY_DYE,        // Ruin
            Material.MUSIC_DISC_5,    // Warden
            Material.RECOVERY_COMPASS // Nothing
        );

        List<ArmorStand> cards = new ArrayList<>();
        for (int i = 0; i < cardMaterials.size(); i++) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.setSmall(true);

            // FIXED: Ab ye Material.PAPER nahi lega, balki list se item uthayega
            ItemStack cardItem = new ItemStack(cardMaterials.get(i)); 
            as.getEquipment().setItemInMainHand(cardItem);

            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);

        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!p.isOnline() || !isHoldingCard(p)) {
                    cards.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                angle += 0.12; 
                double radius = 2.8;

                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    double x = radius * Math.cos(angle + offset);
                    double z = radius * Math.sin(angle + offset);
                    
                    Location loc = p.getLocation().clone().add(x, 1.2, z);
                    Vector dir = p.getLocation().toVector().subtract(loc.toVector());
                    loc.setDirection(dir);
                    cards.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String nbtId = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (nbtId != null && nbtId.equals(getName())) return true;
        
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name != null && name.equalsIgnoreCase(getName());
    }

    private boolean isCool(Player p, String key, int seconds) {
        if (seconds <= 0) return true;
        long now = System.currentTimeMillis();
        String mapKey = p.getUniqueId().toString() + "_" + key;
        if (cooldowns.containsKey(mapKey)) {
            long timeLeft = (cooldowns.get(mapKey) - now) / 1000;
            if (timeLeft > 0) {
                String rawMsg = SpcialSmp.get().getConfig().getString("messages.cooldown-active", "§cWait %time%s");
                p.sendMessage(rawMsg.replace("%time%", String.valueOf(timeLeft)));
                return false;
            }
        }
        cooldowns.put(mapKey, now + (seconds * 1000L));
        return true;
    }
            }
