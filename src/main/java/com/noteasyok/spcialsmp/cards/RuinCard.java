package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import com.noteasyok.spcialsmp.manager.CooldownManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
        preLoadDimension();
    }

    public static void preLoadDimension() {
        if (Bukkit.getWorld(DIM_NAME) == null) {
            WorldCreator wc = new WorldCreator(DIM_NAME);
            wc.environment(World.Environment.NORMAL);
            World world = wc.createWorld();
            if (world != null) world.setKeepSpawnInMemory(true);
        }
    }

    @Override public String getName() { return "Ruin Card"; }
    @Override public int getModelData() { return 7; }
    @Override public Material getMaterial() { return Material.GRAY_DYE; }

    // --- LEFT CLICK: TARGET TELEPORT (OWNER SAFE) ---
    @Override
    public void leftClick(Player p) {
        CooldownManager cd = SpcialSmp.get().getCooldownManager();
        if (!cd.canUse(p, getName(), "left")) return;

        // Aim-Based Target Selection
        Entity targetEntity = getTarget(p, 25);
        if (!(targetEntity instanceof Player victim) || victim.equals(p)) {
            p.sendMessage("§c§l[!] §7Look at a player to send them to Ruin!");
            return;
        }

        Location riftLoc = victim.getLocation().add(0, 1, 0);
        p.sendMessage("§2§l[!] §aSending §f" + victim.getName() + " §ato the Toxic Rift...");
        
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= 10) {
                    teleportToRuin(victim, p); // Victim goes, P is owner
                    this.cancel();
                    return;
                }
                riftLoc.getWorld().spawnParticle(Particle.SQUID_INK, riftLoc, 20, 0.5, 0.5, 0.5, 0.05);
                t++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L);
        
        cd.applyCooldown(p, getName(), "left");
    }

    private void teleportToRuin(Player victim, Player owner) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        if (ruinWorld == null) return;

        Location targetLoc = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0.5, 2, 0.5);
        victim.teleport(targetLoc);
        victim.playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        victim.sendMessage("§2§l[!] §cYou have been banished to the Ruin by " + owner.getName());

        BossBar bar = Bukkit.createBossBar("§2§lTOXIC ATMOSPHERE", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.addPlayer(victim);
        activeBars.put(victim, bar);

        new BukkitRunnable() {
            int time = 60;
            @Override
            public void run() {
                if (time <= 0 || !victim.isOnline() || !victim.getWorld().getName().equals(DIM_NAME)) {
                    victim.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    bar.removeAll();
                    activeBars.remove(victim);
                    this.cancel();
                    return;
                }

                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                victim.getWorld().spawnParticle(Particle.WARPED_SPORE, victim.getLocation().add(0, 3, 0), 20, 2, 2, 2, 0.01);
                
                if (time % 8 == 0) spawnMutantMob(victim.getLocation());

                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lToxic Collapse in: §f" + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    // --- RIGHT CLICK: DARK AURA ANIMATION ---
    @Override
    public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 3));
        p.sendMessage("§8§l[!] §7Shadow Shield & Aura Active!");
        
        // Dark Aura Particle Task
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100 || !p.isOnline()) { this.cancel(); return; }
                
                Location loc = p.getLocation();
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.cos(i) * 1.5;
                    double z = Math.sin(i) * 1.5;
                    loc.add(x, 0.2, z);
                    p.getWorld().spawnParticle(Particle.SQUID_INK, loc, 1, 0, 0, 0, 0);
                    p.getWorld().spawnParticle(Particle.SMOKE, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, 0.2, z);
                }
                ticks += 5;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
    }

    @Override
    public void shiftRightClick(Player p) {
        p.sendMessage("§2§l[!] §7Summoning Guards...");
        for (int i = 0; i < 4; i++) {
            Silverfish s = (Silverfish) p.getWorld().spawn(p.getLocation(), Silverfish.class);
            s.setCustomName("§a" + p.getName() + "'s Guard");
            s.setCustomNameVisible(true);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!s.isValid() || !p.isOnline()) { this.cancel(); return; }
                    if (s.getTarget() != null && s.getTarget().getUniqueId().equals(p.getUniqueId())) {
                        s.setTarget(null);
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
        }
    }

    private Entity getTarget(Player p, int range) {
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, e -> !e.equals(p));
        return (ray != null) ? ray.getHitEntity() : null;
    }

    private void spawnMutantMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
        LivingEntity mutant = (LivingEntity) center.getWorld().spawn(spawnLoc, random.nextBoolean() ? Zombie.class : Skeleton.class);
        mutant.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        mutant.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        mutant.setHealth(40.0);
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
            lore.add("§eLeft-Click: §2Send Victim to Ruin");
            lore.add("§eRight-Click: §8Dark Shield & Aura");
            lore.add("§eShift + Right: §aGuard Summon");
            meta.setLore(lore);
            meta.setCustomModelData(getModelData());
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
    }
