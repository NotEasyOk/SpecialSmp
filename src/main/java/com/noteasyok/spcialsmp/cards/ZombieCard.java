package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class ZombieCard extends BaseCard implements Listener {

    private final Map<UUID, Integer> active = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    
    // Horde mode players ko track karne ke liye
    private final Set<UUID> zombieModePlayers = new HashSet<>();
    
    // Unka pura Armor (Helmet, Chest, Leggings, Boots) save karne ke liye
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

    /* ---------------- LEFT CLICK (Spawn Baby Zombie) ---------------- */
    @Override
    public void leftClick(Player p) {
        int cooldownSec = SpcialSmp.get().getConfig().getInt("cards.zombie.summon_cooldown", 60);
        if (!isCool(p, "spawn", cooldownSec)) return;

        int max = SpcialSmp.get().getConfig().getInt("zombie-card.max-zombies", 2);
        int time = SpcialSmp.get().getConfig().getInt("zombie-card.duration-seconds", 60);

        int count = active.getOrDefault(p.getUniqueId(), 0);
        if (count >= max) {
            p.sendMessage("§cZombie limit reached!");
            cooldowns.remove(p.getUniqueId().toString() + "_spawn");
            return;
        }

        Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
        z.setBaby(true);
        z.setTarget(null);
        z.setMetadata("owner", new FixedMetadataValue(SpcialSmp.get(), p.getUniqueId().toString()));

        // Minion ko strong armor dena
        EntityEquipment eq = z.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        active.put(p.getUniqueId(), count + 1);

        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (z.isValid()) z.remove();
            active.put(p.getUniqueId(), Math.max(0, active.getOrDefault(p.getUniqueId(), 1) - 1));
        }, time * 20L);
    }

    /* ---------------- RIGHT CLICK (Instant Feed) ---------------- */
    @Override
    public void rightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.zombie.feed_cooldown", 60);
        if (!isCool(p, "feed", cd)) return;

        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
        p.sendMessage(ChatColor.GREEN + "You have been fed!");
    }

    /* ---------------- SHIFT + RIGHT CLICK (Full Zombie Disguise) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.zombie.horde_mode_cooldown", 120);
        if (!isCool(p, "horde", cd)) return;

        // 1. Player ko list mein daalo
        zombieModePlayers.add(p.getUniqueId());

        // 2. Current Armor Save karo (Pura Set)
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());

        // 3. Zombie Costume banao (Green Leather + Zombie Head)
        ItemStack zHead = new ItemStack(Material.ZOMBIE_HEAD);
        ItemStack zChest = createColoredArmor(Material.LEATHER_CHESTPLATE, Color.fromRGB(95, 133, 73)); // Zombie Green
        ItemStack zLegs = createColoredArmor(Material.LEATHER_LEGGINGS, Color.fromRGB(95, 133, 73));
        ItemStack zBoots = createColoredArmor(Material.LEATHER_BOOTS, Color.fromRGB(95, 133, 73));

        // 4. Costume Pehnao
        p.getInventory().setHelmet(zHead);
        p.getInventory().setChestplate(zChest);
        p.getInventory().setLeggings(zLegs);
        p.getInventory().setBoots(zBoots);

        // 5. Sound aur Effect
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1f, 0.5f);
        p.sendMessage(ChatColor.DARK_GREEN + "Horde Mode Active! You look like a Zombie for 15s.");

        // 6. Timer 15 seconds baad hatane ke liye
        Bukkit.getScheduler().runTaskLater(SpcialSmp.get(), () -> {
            if (p.isOnline()) {
                zombieModePlayers.remove(p.getUniqueId());
                
                // Asli Armor Wapas karo
                if (savedArmor.containsKey(p.getUniqueId())) {
                    p.getInventory().setArmorContents(savedArmor.remove(p.getUniqueId()));
                } else {
                    p.getInventory().setArmorContents(null); // Agar kuch nahi tha to clear karo
                }

                p.sendMessage(ChatColor.RED + "Horde Mode expired.");
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, 1f, 1f);
            }
        }, 15 * 20L); 
    }

    /* ---------------- HELPER: Colored Leather Armor ---------------- */
    private ItemStack createColoredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    /* ---------------- EVENT: STOP MOBS ATTACKING ---------------- */
    @EventHandler
    public void onMobTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player p && zombieModePlayers.contains(p.getUniqueId())) {
            if (e.getEntity() instanceof Monster) {
                e.setCancelled(true); 
            }
        }
    }

    // --- COOLDOWN HELPER ---
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
