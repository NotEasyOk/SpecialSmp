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

    // --- SHIFT + RIGHT CLICK (Giant Sword) ---
    @Override
    public void shiftRightClick(Player p) {
        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 60);
        Location targetLoc = (ray != null && ray.getHitPosition() != null) 
                ? ray.getHitPosition().toLocation(p.getWorld()) 
                : p.getLocation().add(p.getDirection().multiply(10));

        Location spawnLoc = targetLoc.clone().add(0, 30, 0);
        
        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true);
        sword.setGravity(false);
        sword.setBasePlate(false);
        sword.setArms(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        sword.setRightArmPose(new EulerAngle(Math.toRadians(180), 0, 0));
        
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) {
            sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(7.0);
        }

        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                // Niche move karo
                Location loc = sword.getLocation().subtract(0, 1.5, 0);
                sword.teleport(loc);

                // Zameen check ya timeout (taki hawa mein na atke)
                if (loc.getY() <= targetLoc.getY() || loc.getBlock().getType().isSolid() || life > 100) {
                    p.getWorld().createExplosion(loc, 15F, true, true, p);
                    p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5);
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
                // ✅ Check using the safe method below
                if (!p.isOnline() || !isHoldingCard(p)) {
                    stars.forEach(Entity::remove);
                    orbiting.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                angle += 0.15;
                for (int i = 0; i < stars.size(); i++) {
                    double theta = angle + (Math.PI * 2 / stars.size()) * i;
                    double x = 2.5 * Math.cos(theta);
                    double z = 2.5 * Math.sin(theta);
                    Location loc = p.getLocation().clone().add(x, 1.2, z);
                    
                    Vector dir = p.getLocation().toVector().subtract(loc.toVector());
                    loc.setDirection(dir);
                    stars.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    // ✅ FIXED: Secure Check (NBT + Name)
    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        
        // 1. Check NBT first
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String nbtId = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (nbtId != null && nbtId.equals(getName())) return true;
        
        // 2. Fallback to Name (Strip colors)
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals(getName());
    }
            }
