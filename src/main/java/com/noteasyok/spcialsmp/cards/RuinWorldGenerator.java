package com.noteasyok.spcialsmp.cards;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RuinWorldGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        
        // Seed ke saath noise generator setup
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        
        // Scale ko 0.005 se 0.01 kiya (Mountains ke liye)
        generator.setScale(0.01); 

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                // Noise logic for Mountains (Normal Terrain feel)
                // Yahan 40 base height hai aur 35 amplitude (Total height variation 75 blocks tak jayegi)
                double noise = generator.noise(realX, realZ, 0.5, 0.5) * 35;
                int currentHeight = (int) (60 + noise);

                // Bedrock Layer
                chunk.setBlock(x, 0, z, Material.BEDROCK);

                for (int y = 1; y < currentHeight; y++) {
                    if (y == currentHeight - 1) {
                        // Top Layer (Surface)
                        int r = random.nextInt(10);
                        if (r < 5) chunk.setBlock(x, y, z, Material.SCULK);
                        else if (r < 8) chunk.setBlock(x, y, z, Material.MOSS_BLOCK);
                        else chunk.setBlock(x, y, z, Material.SOUL_SAND);
                    } else if (y > currentHeight - 4) {
                        // Middle Layer (Mitti/Stone blend)
                        chunk.setBlock(x, y, z, Material.DEEPSLATE);
                    } else {
                        // Deep Layer
                        chunk.setBlock(x, y, z, Material.BLACKSTONE);
                    }
                }
                
                // Set Biome for the whole column
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    biome.setBiome(x, y, z, Biome.SWAMP); 
                }
            }
        }
        return chunk;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(new BlockPopulator() {
            @Override
            public void populate(World world, Random random, org.bukkit.Chunk chunk) {
                int cx = chunk.getX() * 16;
                int cz = chunk.getZ() * 16;

                // 1. Poison Trees (30% Chance)
                if (random.nextInt(100) < 30) {
                    int x = cx + random.nextInt(15);
                    int z = cz + random.nextInt(15);
                    int y = world.getHighestBlockYAt(x, z);
                    if (y > 1 && world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        createPoisonTree(world, x, y, z, random);
                    }
                }

                // 2. Corrupted Houses (5% Chance)
                if (random.nextInt(100) < 5) {
                    int x = cx + random.nextInt(15);
                    int z = cz + random.nextInt(15);
                    int y = world.getHighestBlockYAt(x, z);
                    if (y > 50) {
                        createCorruptedHouse(world, x, y, z, random);
                    }
                }
            }
        });
    }

    private void createPoisonTree(World world, int x, int y, int z, Random random) {
        int height = 5 + random.nextInt(3);
        for (int i = 0; i < height; i++) {
            world.getBlockAt(x, y + i, z).setType(Material.WARPED_STEM);
        }
        int top = y + height - 1;
        for (int lx = x - 2; lx <= x + 2; lx++) {
            for (int lz = z - 2; lz <= z + 2; lz++) {
                for (int ly = top; ly <= top + 2; ly++) {
                    if (world.getBlockAt(lx, ly, lz).getType() == Material.AIR) {
                        if (random.nextInt(10) < 6) {
                            world.getBlockAt(lx, ly, lz).setType(Material.WARPED_WART_BLOCK);
                        } else if (random.nextInt(12) == 0) {
                            world.getBlockAt(lx, ly, lz).setType(Material.SHROOMLIGHT);
                        }
                    }
                }
            }
        }
    }

    private void createCorruptedHouse(World world, int x, int y, int z, Random random) {
        int width = 5 + random.nextInt(3);
        int height = 4 + random.nextInt(2);
        int depth = 5 + random.nextInt(3);

        for (int bx = x; bx < x + width; bx++) {
            for (int bz = z; bz < z + depth; bz++) {
                for (int by = y; by < y + height; by++) {
                    if (random.nextInt(10) < 7) {
                        if (by == y || by == y + height - 1 || bx == x || bx == x + width - 1 || bz == z || bz == z + depth - 1) {
                            Material mat = (random.nextBoolean()) ? Material.POLISHED_BLACKSTONE_BRICKS : Material.SCULK;
                            world.getBlockAt(bx, by, bz).setType(mat);
                        }
                    }
                }
            }
        }
        world.getBlockAt(x + 1, y + 1, z + 1).setType(Material.REINFORCED_DEEPSLATE);
    }
                    }
