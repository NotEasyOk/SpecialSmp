package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager;
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

    private static final String DIM_NAME = "world_ruin_dimension";
    private final Map<Player, BossBar> activeBars = new HashMap<>();
    private final Random random = new Random();

    public RuinCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
        // Server start hote hi dimension ready karne ka logic
        preLoadDimension();
    }

    // Is method ko humne static banaya hai taaki ye bina card use kiye world create kar sake
    public static void preLoadDimension() {
        if (Bukkit.getWorld(DIM_NAME) == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§2RuinCard§8] §aPre-loading Ruin Dimension...");
            WorldCreator wc = new WorldCreator(DIM_NAME);
            wc.environment(World.Environment.NORMAL);
            // Agar aapka koi custom generator hai to yahan uncomment karein
            // wc.generator(new RuinWorldGenerator()); 
            World world = wc.createWorld();
            if (world != null) {
                world.setKeepSpawnInMemory(true); // Isse dimension hamesha load rahega
                world.getChunkAt(0, 0).load();   // Spawn area ko pehle hi load kar lo
                Bukkit.getConsoleSender().sendMessage("§8[§2RuinCard§8] §eDimension is READY and INSTANT!");
            }
        }
    }

    @Override public String getName() { return "Ruin Card"; }
    @Override public int getModelData() { return 7; }
    @Override public Material getMaterial() { return Material.GRAY_DYE; }

    @Override
    public void leftClick(Player p) {
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        if (!cd.canUse(p, getName(), "left")) return;

        Block target = p.getTargetBlockExact(5);
        if (target == null || target.getType() == Material.AIR) {
            p.sendMessage("§c§l[!] §7Look at a block to open the rift!");
            return;
        }

        Location loc = target.getLocation().add(0.5, 1.1, 0.5);
        p.sendMessage("§2§l[!] §aRift Opened! Teleporting...");
        
        // Portal Animation (Ab ye sirf 1 second ki hogi kyunki world pehle se ready hai)
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= 20) { // 1 Second animation
                    teleportToRuin(p, loc);
                    this.cancel();
                    return;
                }
                
                // Player ko Rift par lock karna
                p.teleport(loc);
                loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 15, 0.2, 0.2, 0.2, 0.05);
                if (t % 5 == 0) p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.5f);
                t += 2;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L);
        
        cd.applyCooldown(p, getName(), "left");
    }

    private void teleportToRuin(Player p, Location oldLoc) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        
        if (ruinWorld == null) {
            p.sendMessage("§c§l[!] §7Error: Dimension not loaded. Please contact Admin.");
            return;
        }

        // Instant Teleport
        Location target = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0.5, 2, 0.5);
        if (target.getY() < 5) target.setY(70); 

        p.teleport(target);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));

        // BossBar aur Baaki logic (Same as before)
        BossBar bar = Bukkit.createBossBar("§2§lTOXIC ATMOSPHERE", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        activeBars.put(p, bar);

        new BukkitRunnable() {
            int time = 60;
            @Override
            public void run() {
                if (time <= 0 || !p.isOnline() || !p.getWorld().getName().equals(DIM_NAME)) {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    p.sendMessage("§c§l[!] §7The Rift has collapsed.");
                    bar.removeAll();
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }
                p.getWorld().spawnParticle(Particle.WARPED_SPORE, p.getLocation().add(0, 5, 0), 30, 3, 2, 3, 0.01);
                if (time % 10 == 0) spawnToxicMob(p.getLocation());
                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lCollapse in: §f" + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private void spawnToxicMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(8) - 4, 0, random.nextInt(8) - 4);
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
        Entity mob = center.getWorld().spawn(spawnLoc, random.nextBoolean() ? Zombie.class : Skeleton.class);
        mob.setCustomName("§2Corrupted Entity");
    }

    @Override public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 3));
        p.sendMessage("§8§l[!] §7Shadow Shield Active!");
    }

    @Override public void shiftRightClick(Player p) {
        p.sendMessage("§2§l[!] §7Summoning Silverfish Guards...");
        for (int i = 0; i < 4; i++) p.getWorld().spawn(p.getLocation(), Silverfish.class);
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
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
    }
