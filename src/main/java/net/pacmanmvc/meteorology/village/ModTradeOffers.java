package net.pacmanmvc.meteorology.village;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.pacmanmvc.meteorology.item.ModItems;

public class ModTradeOffers {
    public static void registerTradeOffers() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 3,
                factories -> factories.add(
                        new TradeOffers.SellItemFactory(ModItems.GOOD_ROD, 12, 1, 2, 15, 0.3F)
                ));
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 4,
                factories -> factories.add(
                        new TradeOffers.SellItemFactory(ModItems.SUPER_ROD, 18, 1, 1, 20, 0.4F)
                ));
    }
}
