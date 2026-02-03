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
            if (world != null) {
                world.setKeepSpawnInMemory(true);
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
        p.sendMessage("§2§l[!] §aOpening Toxic Rift...");
        
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= 20) {
                    teleportToRuin(p, loc);
                    this.cancel();
                    return;
                }
                loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 15, 0.2, 0.2, 0.2, 0.05);
                t += 2;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 2L);
        
        cd.applyCooldown(p, getName(), "left");
    }

    private void teleportToRuin(Player p, Location oldLoc) {
        World ruinWorld = Bukkit.getWorld(DIM_NAME);
        if (ruinWorld == null) return;

        Location target = ruinWorld.getHighestBlockAt(0, 0).getLocation().add(0.5, 2, 0.5);
        p.teleport(target);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);

        BossBar bar = Bukkit.createBossBar("§2§lTOXIC ATMOSPHERE", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        activeBars.put(p, bar);

        new BukkitRunnable() {
            int time = 60;
            @Override
            public void run() {
                if (time <= 0 || !p.isOnline() || !p.getWorld().getName().equals(DIM_NAME)) {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    bar.removeAll();
                    activeBars.remove(p);
                    this.cancel();
                    return;
                }

                // --- TOXIC WORLD LOGIC ---
                // 1. Water & Block Poisoning
                Material standingOn = p.getLocation().getBlock().getType();
                Material inBody = p.getLocation().add(0, 1, 0).getBlock().getType();
                if (standingOn != Material.AIR || inBody == Material.WATER) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 1));
                }

                // 2. Visual Effects
                p.getWorld().spawnParticle(Particle.WARPED_SPORE, p.getLocation().add(0, 3, 0), 20, 2, 2, 2, 0.01);
                
                // 3. Mutant Mob Spawning
                if (time % 8 == 0) spawnMutantMob(p.getLocation());

                bar.setProgress(time / 60.0);
                bar.setTitle("§2§lToxic Collapse in: §f" + time + "s");
                time--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 20L);
    }

    private void spawnMutantMob(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
        
        LivingEntity mutant;
        if (random.nextBoolean()) {
            mutant = (Zombie) center.getWorld().spawn(spawnLoc, Zombie.class);
            mutant.setCustomName("§4§lMutant Hulk");
        } else {
            mutant = (Skeleton) center.getWorld().spawn(spawnLoc, Skeleton.class);
            mutant.setCustomName("§b§lFrost Archer");
        }

        // --- SUN PROTECTION & BUFFS ---
        mutant.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET)); // Prevents burning
        mutant.getEquipment().setHelmetDropChance(0.0f);
        mutant.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1));
        mutant.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 99999, 1));
        
        if (mutant.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            mutant.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            mutant.setHealth(40.0);
        }
    }

    @Override public void rightClick(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 3));
        p.sendMessage("§8§l[!] §7Shadow Shield Active!");
    }

    @Override public void shiftRightClick(Player p) {
        p.sendMessage("§2§l[!] §7Summoning " + p.getName() + "'s Guards...");
        for (int i = 0; i < 4; i++) {
            Silverfish s = (Silverfish) p.getWorld().spawn(p.getLocation(), Silverfish.class);
            s.setCustomName("§a" + p.getName() + "'s Guard");
            s.setCustomNameVisible(true);
            
            // Protect Owner Logic
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!s.isValid() || !p.isOnline()) { this.cancel(); return; }
                    if (s.getTarget() != null && s.getTarget().getUniqueId().equals(p.getUniqueId())) {
                        s.setTarget(null); // Will not attack owner
                    }
                }
            }.runTaskTimer(SpcialSmp.get(), 0L, 5L);
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
            lore.add("§eShift + Right: §aGuard Summon");
            meta.setLore(lore);
            meta.setCustomModelData(getModelData());
            meta.getPersistentDataContainer().set(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }
                                             }
