package com.bradykoenig.veinminer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class VeinMiner implements ModInitializer {

    private static final Set<Block> ORE_BLOCKS = Set.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.ANCIENT_DEBRIS);

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld))
                return;
            if (!player.isSneaking())
                return;

            ItemStack tool = player.getMainHandStack();
            if (!isPickaxe(tool))
                return;

            Block targetBlock = state.getBlock();
            if (ORE_BLOCKS.contains(targetBlock)) {
                Set<BlockPos> vein = findConnectedBlocks(serverWorld, pos, targetBlock, new HashSet<>());

                for (BlockPos veinPos : vein) {
                    if (!veinPos.equals(pos)) {
                        BlockState blockState = serverWorld.getBlockState(veinPos);
                        blockState.getBlock().onBreak(serverWorld, veinPos, blockState, player);
                        serverWorld.breakBlock(veinPos, false); // Don't drop here manually
                        blockState.getBlock().afterBreak(serverWorld, player, veinPos, blockState, null, tool);
                    }
                }
            }
        });
    }

    private boolean isPickaxe(ItemStack stack) {
        return stack.isOf(Items.WOODEN_PICKAXE) ||
                stack.isOf(Items.STONE_PICKAXE) ||
                stack.isOf(Items.IRON_PICKAXE) ||
                stack.isOf(Items.GOLDEN_PICKAXE) ||
                stack.isOf(Items.DIAMOND_PICKAXE) ||
                stack.isOf(Items.NETHERITE_PICKAXE);
    }

    private Set<BlockPos> findConnectedBlocks(ServerWorld world, BlockPos origin, Block targetBlock,
            Set<BlockPos> visited) {
        if (!visited.add(origin))
            return visited;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = origin.offset(dir);
            if (world.getBlockState(neighbor).getBlock().equals(targetBlock)) {
                findConnectedBlocks(world, neighbor, targetBlock, visited);
            }
        }

        return visited;
    }
}
