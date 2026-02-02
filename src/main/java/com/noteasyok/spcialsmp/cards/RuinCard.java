package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RuinCard extends BaseCard implements Listener {

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final String DIM_NAME = "world_ruin_dimension";
    private final Map<Player, BossBar> activeBars = new HashMap<>();
    private final Random random = new Random();

    public RuinCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override
    public String getName() { return "Ruin Card"; }
    @Override
    public int getModelData() { return 7; }
    @Override
    public Material getMaterial() { return Material.GRAY_DYE; }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = createItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Power of the corrupted world.");
            lore.add("");
            lore.add("§eLeft-Click: §2Ruin Dimension");
            lore.add("§eRight-Click: §8Dark Shield");
            lore.add("§eShift + Right: §aSilverfish Guard");
            meta.setLore(lore);
            
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void leftClick(Player p) {
        if (!isCool(p, "dimension", 120)) return;

        Block target = p.getTargetBlock(null, 5);
        if (target.getType() == Material.AIR) {
            p.sendMessage("§c§l[!] §7Look at a block to summon the portal!");
            return;
        }

        Location loc = target.getLocation().add(0.5, 1, 0.5);
        p.sendMessage("§2§l[!] §aOpening Toxic Rift...");
        p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1f, 0.5f);

        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= 30) { 
                    teleportToRuin(p, loc);
                    this.cancel();
                    return;
                }
                loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 10, 0.5, 1, 0.5, 0.1);
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.5, 1, 0.5); 
                t += 5;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
    }

    private void teleportToRuin(Player p, Location oldLoc) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        if (ruinWorld == null) {
            WorldCreator wc = new WorldCreator(DIM_NAME);
            wc.generator(new RuinWorldGenerator());
            wc.type(WorldType.NORMAL); // FIX: Flat nahi, normal world terrain hoga
            ruinWorld = wc.createWorld();
        }

        Location target = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0, 2, 0);
        p.teleport(target);
        p.playSound(p.getLocation(), Sound.AMBIENT_BASALT_DELTAS_LOOP, 2f, 0.5f);

        BossBar bar = Bukkit.createBossBar("§2§lTOXIC ATMOSPHERE", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        activeBars.put(p, bar);

        new BukkitRunnable() {
            int time = 60;
            @Override
            public void run() {
                if (time <= 0 || !p.isOnline() || !p.getWorld().getName().equals(DIM_NAME)) {
                    p.teleport(oldLoc);
                    bar.removeAll();
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }
                
                // Environment: Particles dropping from sky
                p.getWorld().spawnParticle(Particle.FALLING_SPORE_BLOSSOM, p.getLocation().add(0, 10, 0), 50, 5, 5, 5, 0.05);

                if (time % 5 == 0) spawnToxicMob(p.getLocation());
                
                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lCollapse in: " + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private void spawnToxicMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(14) - 7, 1, random.nextInt(14) - 7);
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);

        World w = center.getWorld();
        LivingEntity mob;
        int type = random.nextInt(3);

        if (type == 0) {
            mob = (Zombie) w.spawn(spawnLoc, Zombie.class);
            mob.setCustomName("§2§lToxic Walker");
        } else if (type == 1) {
            mob = (Skeleton) w.spawn(spawnLoc, Skeleton.class);
            mob.setCustomName("§8§lRuin Archer");
        } else {
            mob = (Enderman) w.spawn(spawnLoc, Enderman.class);
            mob.setCustomName("§5§lVoid Stalker");
        }

        // Hostile Mob Protection (Sun se nahi marenge)
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 99999, 1, false, false));
        mob.setCustomNameVisible(true);
        mob.setRemoveWhenFarAway(true);
    }

    @Override
    public void rightClick(Player p) {
        if (!isCool(p, "shield", 40)) return;
        p.sendMessage("§8§l[!] §7Shadow Shield Active!");
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2));
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation(), 50, 0.5, 1, 0.5, 0.1);
    }

    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "summon", 60)) return;
        p.sendMessage("§2§l[!] §7Summoning Silverfish Guards...");
        
        for (int i = 0; i < 6; i++) {
            Silverfish s = (Silverfish) p.getWorld().spawn(p.getLocation(), Silverfish.class);
            s.setCustomName("§a" + p.getName() + "'s Guard");
            s.setCustomNameVisible(true);
            
            // Silverfish Owner ko attack nahi karegi
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (s.isDead() || !s.isValid()) { this.cancel(); return; }
                    if (s.getTarget() != null && s.getTarget().equals(p)) {
                        s.setTarget(null);
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 0, 5);
        }
    }

    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String k = p.getUniqueId() + "_" + key;
        if (cooldowns.containsKey(k) && cooldowns.get(k) > now) return false;
        cooldowns.put(k, now + (seconds * 1000L));
        return true;
    }
            }
