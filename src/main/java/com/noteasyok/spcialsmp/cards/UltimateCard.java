package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class UltimateCard extends BaseCard {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();

    @Override
    public String getName() {
        return "Ultimate Card";
    }

    // --- LEFT CLICK (Lightning) ---
    @Override
    public void leftClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 2));
        p.sendMessage("§6§lULTIMATE: §eLightning Strike!");
        
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 50);
        if (r != null && r.getHitPosition() != null) {
            Location hit = r.getHitPosition().toLocation(p.getWorld());
            p.getWorld().strikeLightning(hit);
        }
    }

    // --- RIGHT CLICK (Orbit Manual Trigger) ---
    @Override
    public void rightClick(Player p) {
        startOrbit(p);
    }

    // --- SHIFT + RIGHT CLICK (Giant Sword Fixed) ---
    @Override
    public void shiftRightClick(Player p) {
        // ✅ FIX: p.getDirection() ko p.getLocation().getDirection() se replace kiya
        Vector lookDir = p.getLocation().getDirection();
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), lookDir, 60);
        
        Location targetLoc = (ray != null && ray.getHitPosition() != null) 
                ? ray.getHitPosition().toLocation(p.getWorld()) 
                : p.getLocation().add(lookDir.multiply(10));

        Location spawnLoc = targetLoc.clone().add(0, 35, 0);
        
        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true);
        sword.setGravity(false);
        sword.setBasePlate(false);
        sword.setArms(true);
        sword.setMarker(true); // Marker true taaki koi ise maar na sake
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        sword.setRightArmPose(new EulerAngle(Math.toRadians(180), 0, 0));
        
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) {
            sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);
        }

        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                // Sword niche girne ki speed
                Location loc = sword.getLocation().subtract(0, 1.8, 0);
                sword.teleport(loc);

                // ✅ FIX: Zameen check aur timeout taaki hawa mein na atke
                if (loc.getBlock().getType().isSolid() || loc.getY() <= targetLoc.getY() || life > 100) {
                    p.getWorld().createExplosion(loc, 15F, true, true, p);
                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5);
                    p.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // --- ORBIT LOGIC ---
    public void startOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) return;

        List<ArmorStand> stars = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true);
            as.setMarker(true);
            as.setGravity(false);
            as.getEquipment().setItemInMainHand(new ItemStack(Material.NETHER_STAR));
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            stars.add(as);
        }
        orbiting.put(p.getUniqueId(), stars);

        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                // ✅ Secure check using NBT and Name
                if (!p.isOnline() || !isHoldingCard(p)) {
                    stars.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                angle += 0.15;
                for (int i = 0; i < stars.size(); i++) {
                    double theta = angle + (Math.PI * 2 / stars.size()) * i;
                    // Radius thoda bada (2.8) taaki player ko clear dikhe
                    Location loc = p.getLocation().clone().add(2.8 * Math.cos(theta), 1.2, 2.8 * Math.sin(theta));
                    
                    Vector dir = p.getLocation().toVector().subtract(loc.toVector());
                    loc.setDirection(dir);
                    stars.get(i).teleport(loc);
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
        return name != null && name.equals(getName());
    }
    }
