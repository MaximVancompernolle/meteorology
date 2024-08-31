package net.pacmanmvc.meteorology.village;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.item.ModItems;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModTradeOffers {
    private static final Logger LOGGER = Meteorology.LOGGER;

    private static void registerOffers(Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap, int level, Consumer<List<TradeOffers.Factory>> factory) {
        final List<TradeOffers.Factory> list = new ArrayList<>();
        factory.accept(list);

        final TradeOffers.Factory[] originalEntries = leveledTradeMap.computeIfAbsent(level, key -> new TradeOffers.Factory[0]);
        final TradeOffers.Factory[] addedEntries = list.toArray(new TradeOffers.Factory[0]);

        final TradeOffers.Factory[] allEntries = ArrayUtils.addAll(originalEntries, addedEntries);
        leveledTradeMap.put(level, allEntries);
    }

    public static void registerTradeOffers() {
        LOGGER.info("Registering Trade Offers");

        Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap = TradeOffers.PROFESSION_TO_LEVELED_TRADE.computeIfAbsent(VillagerProfession.FISHERMAN, key -> new Int2ObjectOpenHashMap<>());
        registerOffers(leveledTradeMap, 4, factories -> factories.add(
                new TradeOffers.SellItemFactory(ModItems.GOOD_ROD, 12, 1, 2, 15)
        ));
        registerOffers(leveledTradeMap, 4, factories -> factories.add(
                new SellCoralFactory(5, 12)
        ));
        registerOffers(leveledTradeMap, 5, factories -> factories.add(
                new TradeOffers.SellItemFactory(ModItems.SUPER_ROD, 18, 1, 1, 20)
        ));
    }

    public static class SellCoralFactory implements TradeOffers.Factory {
        private final int experience;
        private final float multiplier;

        public SellCoralFactory(int experience, float multiplier) {
            this.experience = experience;
            this.multiplier = multiplier;
        }

        private ItemStack getCoralItem(Random random) {
            ItemStack[] coralItems = new ItemStack[]{
                    new ItemStack(Items.BRAIN_CORAL, 1),
                    new ItemStack(Items.BUBBLE_CORAL, 1),
                    new ItemStack(Items.FIRE_CORAL, 1),
                    new ItemStack(Items.HORN_CORAL, 1),
                    new ItemStack(Items.TUBE_CORAL, 1)
            };
            return coralItems[random.nextInt(coralItems.length)];
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack sellItem = this.getCoralItem(random);
            return new TradeOffer(new TradedItem(Items.EMERALD, 3), sellItem, 12, this.experience, this.multiplier);
        }
    }
}
