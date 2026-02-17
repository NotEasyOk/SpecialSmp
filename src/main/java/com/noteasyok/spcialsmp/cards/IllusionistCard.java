package com.noteasyok.spcialsmp.cards;

import com.noteasyok.spcialsmp.SpcialSmp;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class IllusionistCard extends BaseCard {

    public IllusionistCard() {
        super(); }

    @Override
    public String getName() { return "Illusionist Card"; }
    @Override
    public Material getMaterial() { return Material.ECHO_SHARD; }
    @Override
    public int getModelData() { return 101; }

    // --- CHECK IF PLAYER IS HOLDING THE CARD ---
    private boolean isHoldingCard(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != getMaterial()) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().contains(getName());
    }

    @Override
    public void leftClick(Player p) {
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        Location targetLoc = p.getTargetBlockExact(30) != null ? p.getTargetBlockExact(30).getLocation() : p.getLocation().add(p.getEyeLocation().getDirection().multiply(10));
        p.sendMessage("§6§l[!] §fSummoning Flame Tornado!");

        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = targetLoc.clone();
            final Vector direction = p.getEyeLocation().getDirection().setY(0).normalize().multiply(0.2);

            @Override
            public void run() {
                if (ticks++ > 200 || !currentLoc.getChunk().isLoaded()) { this.cancel(); return; }

                currentLoc.add(direction); // Tornado moves forward

                // Visual Spiral (Optimized)
                for (int i = 0; i < 15; i++) {
                    double radius = 0.5 + (i * 0.3);
                    double angle = (ticks * 0.4) + i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location pLoc = currentLoc.clone().add(x, i * 0.6, z);
                    p.getWorld().spawnParticle(Particle.FLAME, pLoc, 2, 0.05, 0.05, 0.05, 0.02);
                    if (ticks % 2 == 0) p.getWorld().spawnParticle(Particle.LARGE_SMOKE, pLoc, 1, 0, 0, 0, 0.01);
                }

                // Entity Pull & Spin
                for (Entity e : currentLoc.getWorld().getNearbyEntities(currentLoc, 6, 12, 6)) {
                    if (e.equals(p) || !(e instanceof LivingEntity)) continue;
                    
                    Vector vec = currentLoc.toVector().subtract(e.getLocation().toVector());
                    double dist = vec.length();
                    
                    // Rotate and Lift
                    Vector rotate = new Vector(-vec.getZ(), 0.5, vec.getX()).normalize().multiply(0.5);
                    e.setVelocity(vec.normalize().multiply(0.3).add(rotate));
                    ((LivingEntity) e).damage(1.0, p);
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 1);

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
                                  }

    @Override
    public void shiftRightClick(Player p) {
        if (!isHoldingCard(p)) return; // Security Check

        int duration = SpcialSmp.get().getConfig().getInt("cards.illusionist.clone-duration", 10);
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "shift_right")) return;

        p.sendMessage("§d§l[!] §5Shadow Clones Summoned!");
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0));

        List<Vindicator> clones = new ArrayList<>();
        Location center = p.getLocation();

        for (int i = 0; i < 5; i++) {
            double angle = 2 * Math.PI * i / 5;
            Location spawnLoc = center.clone().add(Math.cos(angle) * 3, 0.5, Math.sin(angle) * 3);
            Vindicator clone = (Vindicator) p.getWorld().spawnEntity(spawnLoc, EntityType.VINDICATOR);
            setupClone(p, clone);
            clones.add(clone);
        }

        // --- THE MONITORING LOOP ---
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // AGAR CARD HAATH SE HATAYA TOH CLONES GAYAB (Anti-Abuse)
                if (!isHoldingCard(p) || ticks++ >= (duration * 2)) {
                    this.cancel();
                    clones.forEach(c -> {
                        if (c.isValid()) {
                            c.getWorld().spawnParticle(Particle.CLOUD, c.getLocation().add(0, 1, 0), 20);
                            c.remove();
                        }
                    });
                    if (ticks >= (duration * 2)) Bukkit.broadcastMessage("§e" + p.getName() + " left the game");
                    return;
                }

                // --- Line 89 se 93 ka naya logic ---
                for (Vindicator c : clones) {
              if (!c.isValid()) continue;

              LivingEntity currentTarget = c.getTarget();

           // AGAR TARGET OWNER HAI YA DOOSRA CLONE HAI, TOH TARGET SAFF KARO
           if (currentTarget != null && (currentTarget.equals(p) || clones.contains(currentTarget))) {
            c.setTarget(null);
        }

    // NAYA TARGET DHUNDO (JO OWNER NA HO)
    if (c.getTarget() == null) {
        p.getNearbyEntities(15, 15, 15).stream()
            .filter(en -> en instanceof LivingEntity && !en.equals(p) && !clones.contains(en))
            .map(en -> (LivingEntity) en)
            .findFirst().ifPresent(c::setTarget);
        }

                    // ATTACK ANIMATION
                    if (c.getTarget() != null && c.getLocation().distance(c.getTarget().getLocation()) < 3.5) {
                        c.setVelocity(c.getLocation().getDirection().multiply(0.3).setY(0.2));
                        c.getTarget().getWorld().spawnParticle(Particle.CRIT, c.getTarget().getLocation().add(0, 1, 0), 10);
                    }
                }
            }
        }.runTaskTimer(SpcialSmp.get(), 0, 10);

        SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "shift_right");
    }

    private void setupClone(Player owner, Vindicator clone) {
        clone.setCustomName(owner.getName());
        clone.setCustomNameVisible(true);
        clone.setSilent(true);
        clone.setCanJoinRaid(false);
        
        clone.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.48);
        clone.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(6.5);
        clone.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false));
        
        var inv = clone.getEquipment();
        inv.setHelmet(getOwnerHead(owner));
        inv.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        inv.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        inv.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
    }

    private ItemStack getOwnerHead(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(p);
            head.setItemMeta(meta);
        }
        return head;
    }

    private Entity getTarget(Player p, int range) {
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, 
                e -> e instanceof LivingEntity && !e.equals(p));
        return (ray != null) ? ray.getHitEntity() : null;
    }

    @Override
    public void rightClick(Player p) {}
  }
