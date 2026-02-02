package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager; // Cooldown fix ke liye
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RuinCard extends BaseCard implements Listener {

    private final String DIM_NAME = "world_ruin_dimension";
    private final Map<Player, BossBar> activeBars = new HashMap<>();
    private final Random random = new Random();

    public RuinCard() {
        // BaseCard constructor ke baad registerEvents zaroori hai
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Ruin Card"; }
    @Override
    public int getModelData() { return 7; }
    @Override
    public Material getMaterial() { return Material.GRAY_DYE; }

    @Override
    public void leftClick(Player p) {
        // --- FIXED COOLDOWN CHECK ---
        // Aapke CardUseListener mein pehle se cooldown manager hai, iska use karna better hai
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        if (!cd.canUse(p, getName(), "left")) return;

        Block target = p.getTargetBlockExact(5); // targetBlock(null, 5) 1.21 mein deprecated hai
        if (target == null || target.getType() == Material.AIR) {
            p.sendMessage("§c§l[!] §7Look at a block to summon the portal!");
            return;
        }

        Location loc = target.getLocation().add(0.5, 1.1, 0.5);
        p.sendMessage("§2§l[!] §aOpening Toxic Rift... You are being pulled in!");
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);

        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= 40) {
                    teleportToRuin(p, loc);
                    this.cancel();
                    return;
                }

                Location stuckLoc = loc.clone();
                stuckLoc.setDirection(p.getLocation().getDirection());
                p.teleport(stuckLoc);
                p.setVelocity(new Vector(0, 0, 0));

                loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 25, 0.3, 0.5, 0.3, 0.05);
                loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 15, 0.4, 0.4, 0.4, 0.02);
                
                if (t % 10 == 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1f, 0.5f);
                }
                t += 2;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L);
        
        cd.applyCooldown(p, getName(), "left"); // Apply cooldown after use
    }

    private void teleportToRuin(Player p, Location oldLoc) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        if (ruinWorld == null) {
            WorldCreator wc = new WorldCreator(DIM_NAME);
            // --- CRITICAL FIX: Custom Generator link karna zaroori hai ---
            wc.generator(new RuinWorldGenerator()); 
            wc.environment(World.Environment.NORMAL);
            ruinWorld = wc.createWorld();
        }

        if (ruinWorld == null) {
            p.sendMessage("§c§l[!] §7Dimension creation failed!");
            return;
        }

        // Teleport to Y=65 (Kyunki hamara terrain pahaadi hai)
        Location target = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0.5, 2, 0.5);
        p.teleport(target);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));

        BossBar bar = Bukkit.createBossBar("§2§lTOXIC ATMOSPHERE", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        activeBars.put(p, bar);

        new BukkitRunnable() {
            int time = 60;
            @Override
            public void run() {
                if (time <= 0 || !p.isOnline() || !p.getWorld().getName().equals(DIM_NAME)) {
                    // Overworld wapas bhejna
                    World overworld = Bukkit.getWorld("world");
                    p.teleport(overworld != null ? overworld.getSpawnLocation() : oldLoc);
                    p.sendMessage("§c§l[!] §7The Rift has collapsed.");
                    bar.removeAll();
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }
                
                // Falling Particles effect
                p.getWorld().spawnParticle(Particle.WARPED_SPORE, p.getLocation().add(0, 5, 0), 50, 4, 3, 4, 0.02);

                if (time % 10 == 0) spawnToxicMob(p.getLocation());
                
                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lCollapse in: §f" + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private void spawnToxicMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);

        World w = center.getWorld();
        LivingEntity mob;
        int type = random.nextInt(3);

        if (type == 0) mob = (Zombie) w.spawn(spawnLoc, Zombie.class);
        else if (type == 1) mob = (Skeleton) w.spawn(spawnLoc, Skeleton.class);
        else mob = (Enderman) w.spawn(spawnLoc, Enderman.class);

        mob.setCustomName("§8[§2Corrupted§8] §fEntity");
        mob.setCustomNameVisible(true);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1));
    }

    @Override
    public void rightClick(Player p) {
        // Shield logic fine hai
        p.sendMessage("§8§l[!] §7Shadow Shield Active!");
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 3));
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0,1,0), 40, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public void shiftRightClick(Player p) {
        // Silverfish guard logic fine hai
        p.sendMessage("§2§l[!] §7Summoning Silverfish Guards...");
        for (int i = 0; i < 6; i++) {
            Silverfish s = (Silverfish) p.getWorld().spawn(p.getLocation(), Silverfish.class);
            s.setCustomName("§a" + p.getName() + "'s Guard");
            s.setCustomNameVisible(true);
        }
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§3§l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Power of the corrupted world.");
            lore.add("");
            lore.add("§eLeft-Click: §2Ruin Dimension");
            lore.add("§eRight-Click: §8Dark Shield");
            lore.add("§eShift + Right: §aSilverfish Guard");
            meta.setLore(lore);
            meta.setCustomModelData(getModelData());
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
                           }
