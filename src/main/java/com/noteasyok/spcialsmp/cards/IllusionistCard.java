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
    public Material getMaterial() { return Material.AMETHYST_SHARD; }
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
    public void LeftClick(Player p) {
        if (!isHoldingCard(p)) return; // Card haath mein nahi toh kuch nahi hoga
        
        if (!SpcialSmp.get().getCooldownManager().canUse(p, getName(), "left")) return;

        Entity target = getTarget(p, 25);
        if (target instanceof LivingEntity victim && !victim.equals(p)) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1));
            
            p.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.2f);
            p.sendMessage("§d§l[!] §fMind Fractured!");
            SpcialSmp.get().getCooldownManager().applyCooldown(p, getName(), "left");
        }
    }

    @Override
    public void ShiftRightClick(Player p, PlayerInteractEvent e) {
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

                for (Vindicator c : clones) {
                    if (!c.isValid()) continue;

                    // OWNER SAFETY & TARGETING
                    if (c.getTarget() == null || c.getTarget().equals(p) || clones.contains(c.getTarget())) {
                        p.getNearbyEntities(15, 15, 15).stream()
                            .filter(en -> en instanceof LivingEntity && !en.equals(p) && !clones.contains(en))
                            .findFirst().ifPresent(en -> c.setTarget((LivingEntity) en));
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
        clone.setJohnny(true);
        
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
    public void RightClick(Player p, PlayerInteractEvent e) {}
  }
