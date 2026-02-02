package com.noteasyok.spcialsmp.cards;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RuinWorldGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Simplex Noise for terrain height (1.21 compatible)
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(worldInfo.getSeed()), 8);
        generator.setScale(0.005); 

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                // Terrain height calculation
                double noise = generator.noise(realX, realZ, 0.5, 0.5) * 30;
                int currentHeight = (int) (65 + noise);

                // Filling blocks from bottom to top
                for (int y = worldInfo.getMinHeight(); y < currentHeight; y++) {
                    if (y <= worldInfo.getMinHeight()) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else if (y > currentHeight - 2) {
                        // Surface logic inside noise generation for 1.21
                        int r = random.nextInt(10);
                        if (r < 6) chunkData.setBlock(x, y, z, Material.SCULK);
                        else if (r < 9) chunkData.setBlock(x, y, z, Material.MOSS_BLOCK);
                        else chunkData.setBlock(x, y, z, Material.SOUL_SAND);
                    } else if (y > currentHeight - 8) {
                        chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                    } else {
                        chunkData.setBlock(x, y, z, Material.BLACKSTONE);
                    }
                }

                // Custom Water (Poison Water logic)
                for (int y = currentHeight; y < 62; y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
            }
        }
    }

    // 1.21 requires these to be explicitly handled if you want custom surface
    @Override
    public boolean shouldGenerateSurface() { return false; } // Humne generateNoise mein hi surface handle kar li hai
    
    @Override
    public boolean shouldGenerateBedrock() { return true; }
}
