package net.pacmanmvc.meteorology.village;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.item.ModItems;
import org.apache.commons.lang3.ArrayUtils;
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
        registerOffers(leveledTradeMap, 3, factories -> factories.add(
                new TradeOffers.SellItemFactory(ModItems.GOOD_ROD, 12, 1, 2, 15)
        ));
        registerOffers(leveledTradeMap, 4, factories -> factories.add(
                new TradeOffers.SellItemFactory(ModItems.SUPER_ROD, 18, 1, 1, 20)
        ));
    }
}
