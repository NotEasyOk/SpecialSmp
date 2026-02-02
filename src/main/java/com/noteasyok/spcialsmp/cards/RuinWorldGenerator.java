package com.noteasyok.spcialsmp.cards;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RuinWorldGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        generator.setScale(0.005); // Terrain Scale (Jitna kam, utna bada pahad)

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                // Strange Terrain Height
                double noise = generator.noise(realX, realZ, 0.5, 0.5) * 15;
                int currentHeight = (int) (65 + noise);

                for (int y = 0; y < currentHeight; y++) {
                    if (y == currentHeight - 1) {
                        // TOP LAYER: Ajeeb Blocks
                        int r = random.nextInt(10);
                        if (r < 5) chunk.setBlock(x, y, z, Material.SCULK); // Dark Blue Weird Block
                        else if (r < 8) chunk.setBlock(x, y, z, Material.MOSS_BLOCK); // Green Moss
                        else chunk.setBlock(x, y, z, Material.SOUL_SAND); // Creepy Sand
                    } else {
                        // UNDERGROUND
                        chunk.setBlock(x, y, z, Material.BLACKSTONE);
                    }
                }
                
                // Set Biome to SWAMP (Taaki paani ganda/green dikhe)
                for (int y = 0; y < 100; y++) {
                    biome.setBiome(x, y, z, Biome.SWAMP); 
                }
            }
        }
        return chunk;
    }

    // --- CUSTOM TREE GENERATOR (Poison Trees) ---
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.singletonList(new BlockPopulator() {
            @Override
            public void populate(World world, Random random, org.bukkit.Chunk chunk) {
                if (random.nextInt(10) < 3) { // 30% chance per chunk
                    int x = random.nextInt(15);
                    int z = random.nextInt(15);
                    int y = world.getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
                    
                    if (world.getBlockAt(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z).getType() != Material.WATER) {
                        createPoisonTree(world, chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z, random);
                    }
                }
            }
        });
    }

    private void createPoisonTree(World world, int x, int y, int z, Random random) {
        // Strange Tree Stem (Warped Stem - Green/Blue Wood)
        for (int i = 0; i < 5 + random.nextInt(3); i++) {
            world.getBlockAt(x, y + i, z).setType(Material.WARPED_STEM);
        }
        
        // Strange Leaves (Nether Wart & Shroomlight)
        int top = y + 4;
        for (int lx = x - 2; lx <= x + 2; lx++) {
            for (int lz = z - 2; lz <= z + 2; lz++) {
                for (int ly = top; ly <= top + 2; ly++) {
                    if (world.getBlockAt(lx, ly, lz).getType() == Material.AIR) {
                        if (random.nextBoolean()) {
                            world.getBlockAt(lx, ly, lz).setType(Material.WARPED_WART_BLOCK);
                        } else if (random.nextInt(10) == 0) {
                            world.getBlockAt(lx, ly, lz).setType(Material.SHROOMLIGHT); // Glow wala block
                        }
                    }
                }
            }
        }
    }
                          }
