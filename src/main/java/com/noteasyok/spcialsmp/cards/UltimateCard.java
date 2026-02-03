package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeDomain = new HashSet<>();
    private final Map<UUID, UUID> throneSeats = new HashMap<>();

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    /* ================= LEFT CLICK: DEMON KING ASCENSION ================= */
    @Override
    public void leftClick(Player p) {
        if (activeDomain.contains(p.getUniqueId()) || !isCool(p, "blood_domain", 60)) return;

        activeDomain.add(p.getUniqueId());
        Location loc = p.getLocation().clone();
        List<ArmorStand> parts = new ArrayList<>();
        
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        p.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 2f, 0.5f);

        // Constructing the Demon Throne using Armor Stands
        ArmorStand seat = createThronePart(loc.clone().add(0, -2, 0), Material.CRYING_OBSIDIAN);
        parts.add(seat);
        parts.add(createThronePart(loc.clone().add(0, -1.2, 0.4), Material.RED_NETHER_BRICK_STAIRS)); // Backrest
        parts.add(createThronePart(loc.clone().add(-0.6, -1.6, 0), Material.COMMAND_BLOCK)); // Power Core L
        parts.add(createThronePart(loc.clone().add(0.6, -1.6, 0), Material.COMMAND_BLOCK)); // Power Core R
        parts.add(createThronePart(loc.clone().add(0, -0.5, 0.5), Material.GOLD_BLOCK)); // Top Ornament

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step < 20) { // Rise up 2 blocks
                    for (ArmorStand as : parts) as.teleport(as.getLocation().add(0, 0.1, 0));
                    // Ground burst effect
                    loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 10, 0.5, 0.1, 0.5, Material.DIRT.createBlockData());
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 2, 0.3, 0, 0.3, 0.05);
                } else if (step == 20) {
                    loc.getWorld().strikeLightningEffect(loc);
                    seat.addPassenger(p);
                    throneSeats.put(seat.getUniqueId(), p.getUniqueId());
                    p.sendTitle("§4§lDEMON KING", "§fThe Throne has Ascended", 10, 40, 10);
                } else if (step > 400 || !p.isOnline()) { // 20 Seconds
                    parts.forEach(as -> {
                        new BukkitRunnable() { // Sink back effect
                            int sink = 0;
                            @Override public void run() {
                                if (sink > 20) { as.remove(); this.cancel(); }
                                as.teleport(as.getLocation().subtract(0, 0.1, 0));
                                sink++;
                            }
                        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
                    });
                    activeDomain.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }
                step++;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private ArmorStand createThronePart(Location l, Material m) {
        ArmorStand as = l.getWorld().spawn(l, ArmorStand.class);
        as.setInvisible(true); as.setGravity(false); as.setMarker(true);
        as.getEquipment().setHelmet(new ItemStack(m));
        return as;
    }

    @EventHandler
    public void onThroneSit(EntityMountEvent e) {
        if (throneSeats.containsKey(e.getMount().getUniqueId())) {
            if (!e.getEntity().getUniqueId().equals(throneSeats.get(e.getMount().getUniqueId()))) {
                e.setCancelled(true);
                if (e.getEntity() instanceof Player intruder) intruder.damage(10);
            }
        }
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD (FIXED) ================= */
    @Override
    public void shiftRightClick(Player p) {
        if (!isCool(p, "ultimate_sword", 30)) return;

        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 50);
        Location target = (ray != null) ? ray.getHitPosition().toLocation(p.getWorld()) : p.getLocation().add(p.getLocation().getDirection().multiply(15));
        Location spawn = target.clone().add(0, 30, 0);

        ArmorStand sword = p.getWorld().spawn(spawn, ArmorStand.class);
        sword.setInvisible(true); sword.setGravity(false); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        // Vertical Position: Tip pointing down
        sword.setRightArmPose(new EulerAngle(Math.toRadians(180), 0, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                sword.teleport(sword.getLocation().subtract(0, 1.5, 0));
                sword.getWorld().spawnParticle(Particle.FLAME, sword.getLocation(), 5, 0.1, 0.5, 0.1, 0.05);
                if (sword.getLocation().getY() <= target.getY() || sword.getLocation().getBlock().getType().isSolid()) {
                    sword.getWorld().createExplosion(sword.getLocation(), 8f, false, false);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= RIGHT CLICK: STABLE ORBIT ================= */
    @Override
    public void rightClick(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) return;
        List<Material> mats = Arrays.asList(Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, Material.GRAY_DYE);
        List<ArmorStand> cards = new ArrayList<>();
        for (Material m : mats) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setSmall(true); as.setInvisible(true); as.setMarker(true); as.setGravity(false);
            as.getEquipment().setItemInMainHand(new ItemStack(m));
            as.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            cards.add(as);
        }
        orbiting.put(p.getUniqueId(), cards);
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!p.isOnline() || !isHoldingCard(p)) {
                    cards.forEach(Entity::remove); orbiting.remove(p.getUniqueId()); this.cancel(); return;
                }
                angle += 0.1;
                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    Location loc = p.getLocation().clone().add(2.2 * Math.cos(angle + offset), 1.2, 2.2 * Math.sin(angle + offset));
                    cards.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return id != null && id.equals(getName());
    }

    private boolean isCool(Player p, String key, int sec) {
        long now = System.currentTimeMillis();
        String k = p.getUniqueId() + "_" + key;
        if (cooldowns.containsKey(k) && cooldowns.get(k) > now) return false;
        cooldowns.put(k, now + (sec * 1000L));
        return true;
    }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§l" + name);
        item.setItemMeta(meta);
        return item;
    }
    }
