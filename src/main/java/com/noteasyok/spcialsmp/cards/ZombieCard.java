package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ZombieCard extends BaseCard implements Listener {

    private final Map<UUID, Integer> active = new HashMap<>();
    private final Set<UUID> zombieModePlayers = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    public ZombieCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() {
        return "Zombie Card";
    }

    @Override
    public int getModelData() {
        return 4; 
    }

    @Override
    public Material getMaterial() {
        return Material.BLACK_DYE;
    }

    /* ---------------- LEFT CLICK (Aggressive Baby Zombie Minion) ---------------- */
    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "left")) return;

        for (int i = 0; i < 5; i++) {

        // 1. Baby Zombie Summon
            double offsetX = (Math.random() * 10) - 5; 
    double offsetZ = (Math.random() * 10) - 5;
    Location spawnLoc = p.getLocation().add(offsetX, 0, offsetZ);
            
        Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
        z.setBaby(true);
        z.setMetadata("owner", new FixedMetadataValue(SpcialSmp.get(), p.getUniqueId().toString()));
        
        // 2. Name & Glow Fix: Malik ka naam aur chamak
        z.setCustomName("§a" + p.getName() + "'s Minion");
        z.setCustomNameVisible(true);
        z.setGlowing(true);

        // 3. Equipment (Wahi strong armor)
        EntityEquipment eq = z.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
             z.setMetadata("minion", new FixedMetadataValue(SpcialSmp.get(), true));
        }

        // 4. Aggressive AI Logic: 15 seconds hunt
        new BukkitRunnable() {
            int timer = 20;

            @Override
            public void run() {
                if (z.isDead() || timer <= 0) {
                    if (!z.isDead()) z.remove();
                    this.cancel();
                    return;
                }

                // Target logic: Sabko maarega (LivingEntities) siwaye malik ke
                if (z.getTarget() == null || z.getTarget().isDead()) {
                    z.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .filter(entity -> !entity.hasMetadata("minion"))
                        .filter(entity -> !entity.getUniqueId().equals(p.getUniqueId()))
                        .filter(entity -> !entity.getUniqueId().equals(z.getUniqueId()))
                        .map(entity -> (LivingEntity) entity)
                        .findFirst()
                        .ifPresent(z::setTarget);
                }

                // Follow Malik: Agar koi dushman nahi hai
                if (z.getTarget() == null) {
                    z.getPathfinder().moveTo(p.getLocation());
                }

                timer--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
     }

        p.sendMessage("§2§lZOMBIE §8» §fYour glowing minion is summoned for 20s!");
    }

    /* ---------------- RIGHT CLICK (Instant Feed - No Change) ---------------- */
    @Override
    public void rightClick(Player p) {
        if (!isCool(p, "right")) return;

        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
        p.sendMessage(ChatColor.GREEN + "You have been fed!");
    }

    /* ---------------- SHIFT + RIGHT CLICK (Disguise - No Change) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "shift_right")) return;

        zombieModePlayers.add(p.getUniqueId());
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());

        ItemStack zHead = new ItemStack(Material.ZOMBIE_HEAD);
        ItemStack zChest = createColoredArmor(Material.LEATHER_CHESTPLATE, Color.fromRGB(95, 133, 73));
        ItemStack zLegs = createColoredArmor(Material.LEATHER_LEGGINGS, Color.fromRGB(95, 133, 73));
        ItemStack zBoots = createColoredArmor(Material.LEATHER_BOOTS, Color.fromRGB(95, 133, 73));

        p.getInventory().setHelmet(zHead);
        p.getInventory().setChestplate(zChest);
        p.getInventory().setLeggings(zLegs);
        p.getInventory().setBoots(zBoots);

        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1f, 0.5f);
        p.sendMessage(ChatColor.DARK_GREEN + "Horde Mode Active! You look like a Zombie for 15s.");

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline()) {
                zombieModePlayers.remove(p.getUniqueId());
                if (savedArmor.containsKey(p.getUniqueId())) {
                    p.getInventory().setArmorContents(savedArmor.remove(p.getUniqueId()));
                }
                p.sendMessage(ChatColor.RED + "Horde Mode expired.");
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, 1f, 1f);
            }
        }, 15 * 20L); 
    }

    private ItemStack createColoredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void stopFriendlyFire(EntityTargetEvent e) {
        if (e.getEntity().hasMetadata("minion") && e.getTarget() != null && e.getTarget().hasMetadata("minion")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void stopMinionDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager().hasMetadata("minion") && e.getEntity().hasMetadata("minion")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player p && zombieModePlayers.contains(p.getUniqueId())) {
            if (e.getEntity() instanceof Monster) {
                e.setCancelled(true); 
            }
        }
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
                }
