package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;

// Listener implement kiya taaki GUI click detect ho sake
public class EndermanCard extends BaseCard implements Listener {

    private final String GUI_TITLE = "§8Select Target to Pull"; // GUI ka naam

    // Constructor: Events register karne ke liye zaroori hai
    public EndermanCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() {
        return "Enderman Card";
    }

    @Override
    public int getModelData() {
        return 0;
    }

    @Override
public Material getMaterial() {
    return Material.CHORUS_FRUIT;
}

    /* ---------------- LEFT CLICK (Teleport) - NO CHANGE ---------------- */
    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "left")) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                60
        );

        Location base = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld())
                : p.getLocation().add(p.getLocation().getDirection().multiply(10));

        Location safe = findSafeLocation(base, p.getWorld());
        if (safe != null) {
            p.teleport(safe);
            p.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, safe, 80, 0.5, 1, 0.5, 0.2);
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        } else {
            return;
        }
    }

    /* ---------------- RIGHT CLICK (Open GUI) ---------------- */
    @Override
    public void rightClick(Player p) {
        // Cooldown check pehle karenge, par set baad mein (GUI click par)
        if (!isCool(p, "right")) return;
            }

        // GUI create karna
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        for (Player target : Bukkit.getOnlinePlayers()) {
            // Khud ko GUI mein nahi dikhana hai
            if (target.getUniqueId().equals(p.getUniqueId())) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName("§e" + target.getName());
                meta.setLore(java.util.List.of("§7Click to Pull this player!"));
                head.setItemMeta(meta);
            }
            inv.addItem(head);
        }

        p.openInventory(inv);
    }

    /* ---------------- GUI CLICK EVENT (Handle Pull) ---------------- */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;
        
        e.setCancelled(true); // Item uthana mana hai

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() != Material.PLAYER_HEAD) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // Player dhoondo
        SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;
        
        Player target = meta.getOwningPlayer().getPlayer();

        p.closeInventory();

        if (target != null && target.isOnline()) {
            // Ab Cooldown Set karo (120 seconds)
            int cd = SpcialSmp.get().getConfig().getInt("cards.enderman.pull_cooldown", 120); // 120s set kiya
            cooldowns.put(p.getUniqueId().toString() + "_pull", System.currentTimeMillis() + (cd * 1000L));

            // Teleport Logic
            target.teleport(p.getLocation());
            target.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, target.getLocation(), 60, 0.5, 1, 0.5, 0.2);
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            
            p.sendMessage(ChatColor.GREEN + "You pulled " + target.getName() + "!");
            target.sendMessage(ChatColor.RED + "You were pulled by " + p.getName() + "!");
        } else {
            p.sendMessage(ChatColor.RED + "Player offline ho gaya!");
        }
    }

    /* ---------------- SHIFT + RIGHT CLICK (Dragon Breath - Fixed) ---------------- */
    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "breath")) return;

        RayTraceResult r = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                40
        );

        Location loc = (r != null && r.getHitPosition() != null)
                ? r.getHitPosition().toLocation(p.getWorld()).add(0, 0.1, 0)
                : p.getLocation();

        AreaEffectCloud cloud = p.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius(4.0f);
        cloud.setDuration(200); 
        cloud.setWaitTime(0);             
        cloud.setReapplicationDelay(10);  // Fix: Jaldi damage dene ke liye kam kiya
        cloud.setRadiusOnUse(0.0f);       
        cloud.setRadiusPerTick(-0.01f);   // Fix: Dheere dheere shrink hoga
        cloud.setParticle(org.bukkit.Particle.DRAGON_BREATH);
        
        // Fix: Amplifier 1 (Harming II) kar diya taaki damage confirm ho
        // Note: PotionEffectType.HARM is Instant Damage
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true); 
        cloud.setSource(p); 

        p.sendMessage(ChatColor.DARK_PURPLE + "Dragon Breath Released!");
    }

    /* ---------------- HELPER METHODS ---------------- */
    private Location findSafeLocation(Location base, World w) {
        for (int i = 0; i < 12; i++) {
            double x = base.getX() + (Math.random() * 6 - 3);
            double z = base.getZ() + (Math.random() * 6 - 3);
            int y = w.getHighestBlockYAt((int) x, (int) z) + 1;

            Location loc = new Location(w, x, y, z);
            if (loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable()) {
                return loc;
            }
        }
        return null;
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
