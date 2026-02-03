package com.noteasyok.spcialsmp.cards;

import org.bukkit.event.Listener;
import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityMountEvent;
import java.util.*;

public class UltimateCard extends BaseCard implements Listener {

    private final Map<UUID, List<ArmorStand>> orbiting = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Map<Location, BlockData>> restoredBlocks = new HashMap<>();
    private final Set<UUID> activeDomain = new HashSet<>();
    private final Map<UUID, UUID> throneSeats = new HashMap<>(); // Seat Entity UUID -> Owner UUID

    public UltimateCard() {
        Bukkit.getPluginManager().registerEvents(this, SpcialSmp.get());
    }

    @Override public String getName() { return "Ultimate Card"; }
    @Override public int getModelData() { return 0; }
    @Override public Material getMaterial() { return Material.GREEN_DYE; }

    @Override
    public ItemStack getItemStackWithLore(String name) {
        ItemStack item = new ItemStack(getMaterial()); 
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7The most powerful card in existence.");
            lore.add("");
            lore.add("§4§lLeft-Click: §fKing's Blood Domain");
            lore.add("§eRight-Click: §fOrbit Shields");
            lore.add("§eShift + Right: §fGiant Sword Drop");
            meta.setLore(lore);
            meta.setCustomModelData(getModelData());
            NamespacedKey key = new NamespacedKey(SpcialSmp.get(), "card_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, getName());
            item.setItemMeta(meta);
        }
        return item;
    }

    /* ================= LEFT CLICK: BLOOD KING DOMAIN (REPLACED) ================= */
    @Override
    public void leftClick(Player p) {
        if (activeDomain.contains(p.getUniqueId()) || !isCool(p, "blood_domain", 60)) return;

        activeDomain.add(p.getUniqueId());
        Location center = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        
        // Spawn Throne
        spawnThrone(p, center);
        
        p.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
        p.sendMessage("§4§l☠ THE BLOOD KING HAS ASCENDED ☠");

        new BukkitRunnable() {
            int timer = 300; // 15 seconds
            @Override
            public void run() {
                if (!p.isOnline() || timer <= 0) {
                    cleanupThrone(p);
                    activeDomain.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                // Blood Rain & Domain Effects
                if (timer % 5 == 0) {
                    for (int i = 0; i < 20; i++) {
                        Location rainLoc = center.clone().add((Math.random()-0.5)*40, 15, (Math.random()-0.5)*40);
                        p.getWorld().spawnParticle(Particle.BLOCK, rainLoc, 1, Material.REDSTONE_BLOCK.createBlockData());
                    }
                }

                // Domain Defense
                center.getWorld().getNearbyEntities(center, 15, 15, 15).forEach(entity -> {
                    if (entity instanceof LivingEntity target && !entity.equals(p)) {
                        Vector dir = target.getLocation().toVector().subtract(center.toVector()).normalize();
                        target.setVelocity(dir.multiply(2.0).setY(0.5));
                        double maxH = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        target.setHealth(Math.min(target.getHealth(), maxH / 2));
                    }
                });

                timer--;
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private void spawnThrone(Player p, Location loc) {
        Map<Location, BlockData> backup = new HashMap<>();
        Block seat = loc.getBlock();
        backup.put(seat.getLocation(), seat.getBlockData());
        
        seat.setType(Material.NETHER_BRICK_STAIRS);
        Stairs data = (Stairs) seat.getBlockData();
        data.setFacing(p.getFacing().getOppositeFace());
        seat.setBlockData(data);

        restoredBlocks.put(p.getUniqueId(), backup);

        // Create Seat Entity
        ArmorStand seatEntity = p.getWorld().spawn(loc.clone().add(0.5, -0.5, 0.5), ArmorStand.class);
        seatEntity.setInvisible(true);
        seatEntity.setMarker(true);
        seatEntity.setGravity(false);
        seatEntity.addPassenger(p);
        throneSeats.put(seatEntity.getUniqueId(), p.getUniqueId());
    }

    @EventHandler
    public void onThroneSit(EntityMountEvent e) {
        if (throneSeats.containsKey(e.getMount().getUniqueId())) {
            UUID ownerUUID = throneSeats.get(e.getMount().getUniqueId());
            if (!e.getEntity().getUniqueId().equals(ownerUUID)) {
                e.setCancelled(true);
                if (e.getEntity() instanceof Player intruder) {
                    intruder.setHealth(0); // Instant Kill
                    intruder.sendTitle("§4§lUNWORTHY", "§cYou are not worthy of this throne!", 10, 40, 10);
                }
            }
        }
    }

    private void cleanupThrone(Player p) {
        if (restoredBlocks.containsKey(p.getUniqueId())) {
            restoredBlocks.get(p.getUniqueId()).forEach((loc, data) -> loc.getBlock().setBlockData(data));
            restoredBlocks.remove(p.getUniqueId());
            throneSeats.entrySet().removeIf(entry -> entry.getValue().equals(p.getUniqueId()));
        }
    }

    /* ================= RIGHT CLICK: ORBIT ================= */
    @Override
    public void rightClick(Player p) {
        startOrbit(p);
    }

    /* ================= SHIFT + RIGHT: GIANT SWORD ================= */
    @Override
    public void shiftRightClick(Player p) {
        int cd = SpcialSmp.get().getConfig().getInt("cards.ultimate.sword_cooldown", 30);
        if (!isCool(p, "ultimate_sword", cd)) return;

        RayTraceResult ray = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 100);
        Location targetLoc = (ray != null) ? ray.getHitPosition().toLocation(p.getWorld()) : p.getLocation().add(p.getLocation().getDirection().multiply(25));
        Location spawnLoc = targetLoc.clone().add(0, 35, 0);
        
        ArmorStand sword = p.getWorld().spawn(spawnLoc, ArmorStand.class);
        sword.setInvisible(true); sword.setGravity(false); sword.setMarker(true);
        sword.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        if (sword.getAttribute(Attribute.GENERIC_SCALE) != null) sword.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(8.0);

        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                Location cur = sword.getLocation().subtract(0, 1.2, 0);
                sword.teleport(cur);
                if (cur.getBlock().getType().isSolid() || cur.getY() <= targetLoc.getY() || life > 150) {
                    p.getWorld().createExplosion(cur, 15F, true, true, p);
                    sword.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    /* ================= ORBIT LOGIC ================= */
    public void startOrbit(Player p) {
        if (orbiting.containsKey(p.getUniqueId())) return;
        List<Material> cardMaterials = Arrays.asList(Material.DISC_FRAGMENT_5, Material.CHORUS_FRUIT, Material.PURPLE_DYE, Material.BLACK_DYE, Material.WHITE_DYE, Material.YELLOW_DYE, Material.GRAY_DYE, Material.MUSIC_DISC_5, Material.PINK_DYE);
        List<ArmorStand> cards = new ArrayList<>();
        for (Material mat : cardMaterials) {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
            as.setInvisible(true); as.setMarker(true); as.setGravity(false); as.getEquipment().setItemInMainHand(new ItemStack(mat));
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
                angle += 0.12;
                for (int i = 0; i < cards.size(); i++) {
                    double offset = (2 * Math.PI / cards.size()) * i;
                    Location loc = p.getLocation().clone().add(2.8 * Math.cos(angle + offset), 1.2, 2.8 * Math.sin(angle + offset));
                    cards.get(i).teleport(loc);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0L, 1L);
    }

    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        String nbtId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(SpcialSmp.get(), "card_id"), PersistentDataType.STRING);
        return nbtId != null && nbtId.equals(getName());
    }

    private boolean isCool(Player p, String key, int seconds) {
        long now = System.currentTimeMillis();
        String k = p.getUniqueId() + "_" + key;
        if (cooldowns.containsKey(k) && cooldowns.get(k) > now) {
            p.sendMessage("§cWait " + (cooldowns.get(k) - now)/1000 + "s");
            return false;
        }
        cooldowns.put(k, now + (seconds * 1000L));
        return true;
    }
    }
