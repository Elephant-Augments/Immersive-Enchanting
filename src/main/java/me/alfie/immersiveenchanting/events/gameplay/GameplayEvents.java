package me.alfie.immersiveenchanting.events.gameplay;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;

public class GameplayEvents {

    /**
     * Remove vanilla enchanted books from villager trades if enabled in config.
     * @param event
     */
    @SubscribeEvent
    public static void removeEnchantedBookVillagerTrades(VillagerTradesEvent event) {
        if (ServerConfig.isAllowEnchantedBookTrades()) return;

        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (int level : trades.keySet()) {
                List<VillagerTrades.ItemListing> tradeList = trades.get(level);
                if (tradeList == null) continue;

                tradeList.removeIf(
                        listing -> listing instanceof VillagerTrades.EnchantBookForEmeralds);
            }
        }
    }

    /**
     * Spawns the Biblioclasm music disc if an ancient book is destroyed in soul fire.
     * @param event
     */
    @SubscribeEvent
    public static void spawnBiblioclasmMusicDisc(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        if(level.isClientSide()) return;

        if(event.getEntity() instanceof ItemEntity itemEntity) {
            if(itemEntity.getItem().is(ModItems.ANCIENT_BOOK.get())) {
                if(itemEntity.isOnFire()) {
                    BlockPos blockPos = itemEntity.getOnPos();
                    Block block = level.getBlockState(blockPos).getBlock();

                    if(block instanceof SoulFireBlock
                            || block instanceof SoulSandBlock
                            || block.equals(Blocks.SOUL_SOIL)) {

                        ItemEntity musicDisc = new ItemEntity(
                                level,
                                blockPos.getX() + 0.5,
                                blockPos.getY() + 0.5,
                                blockPos.getZ() + 0.5,
                                new ItemStack(ModItems.BIBLIOCLASM_MUSIC_DISC.get()));

                        musicDisc.setDeltaMovement(0, 0.3, 0);
                        musicDisc.setInvulnerable(true);
                        level.addFreshEntity(musicDisc);

                        FxHelper.playBiblioclasmSpawnFx(level, blockPos);
                    }
                }
            }
        }
    }
}
