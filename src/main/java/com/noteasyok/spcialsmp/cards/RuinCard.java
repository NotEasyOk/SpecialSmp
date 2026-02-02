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
import org.bukkit.event.player.PlayerMoveEvent;
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
            lore.add("§eShift + Right: §aMinion Army");
            meta.setLore(lore);
            
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void leftClick(Player p) {
        int cd = 120;
        if (!isCool(p, "dimension", cd)) return;

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
                    bar.removePlayer(p);
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }
                if (time % 5 == 0) spawnWeirdMob(p.getLocation());
                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lCollapse in: " + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private void spawnWeirdMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 1, random.nextInt(10) - 5);
        if (spawnLoc.getBlock().getType().isSolid()) return;

        int type = random.nextInt(3);
        World w = center.getWorld();

        if (type == 0) {
            Zombie z = w.spawn(spawnLoc, Zombie.class);
            z.setCustomName("§2§lToxic Walker");
            z.setCustomNameVisible(true);
            z.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999, 2));
            EntityEquipment eq = z.getEquipment();
            if (eq != null) {
                ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
                LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
                if (meta != null) {
                    meta.setColor(Color.GREEN);
                    chest.setItemMeta(meta);
                }
                eq.setChestplate(chest);
            }
        } 
        else if (type == 1) {
            Spider s = w.spawn(spawnLoc, Spider.class);
            s.setCustomName("§c§lThe Watcher");
            s.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 1));
            s.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 9999, 1));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (s.isDead()) this.cancel();
                    s.getWorld().spawnParticle(Particle.ENTITY_EFFECT, s.getLocation(), 3, 0.2, 0.2, 0.2, 1);
                }
            }.runTaskTimer(SpcialSmp.get(), 0, 10);
        }
        else {
            Silverfish rat = w.spawn(spawnLoc, Silverfish.class);
            rat.setCustomName("§4§lBomb Rat");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (rat.isDead() || !rat.isValid()) { this.cancel(); return; }
                    if (rat.getTarget() != null && rat.getLocation().distance(rat.getTarget().getLocation()) < 2) {
                        rat.getWorld().createExplosion(rat.getLocation(), 2f, false, false);
                        rat.remove();
                        this.cancel();
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 0, 5);
        }
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
        p.sendMessage("§2§l[!] §7Summoning Corrupted Minions...");
        for (int i = 0; i < 4; i++) {
            Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
            z.setBaby(true);
            z.setCustomName("§aMinion");
            EntityEquipment eq = z.getEquipment();
            if (eq != null) eq.setHelmet(new ItemStack(Material.TURTLE_HELMET));
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
