package com.noteasyok.spcialsmp.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

public class WGUtils {

    // Sirf ye check karne ke liye ki player kisi protected area mein hai ya nahi
    public static boolean isInsideRegion(Player player) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

        return set.size() > 0; // Agar 1 bhi region hai toh true
    }
}
