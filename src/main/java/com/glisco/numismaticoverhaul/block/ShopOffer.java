package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.village.TradeOffer;

import java.util.List;

public class ShopOffer {

    private final ItemStack sell;
    private final int price;

    public ShopOffer(ItemStack sell, int price) {

        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

        this.sell = sell;
        this.price = price;
    }

    public TradeOffer toTradeOffer(ShopBlockEntity shop) {

        ItemStack buy = CurrencyConverter.getRequiredCurrencyTypes(price) == 1 ? CurrencyConverter.getAsItemStackList(price).get(0) : MoneyBagItem.create(price);

        int maxUses = shop.getItems().stream().filter(stack -> {
            ItemStack comparisonStack = stack.copy();
            comparisonStack.setCount(1);
            return ItemStack.areEqual(comparisonStack, sell);
        }).mapToInt(ItemStack::getCount).sum();

        return new TradeOffer(buy, sell, maxUses, 0, 0);
    }

    public int getPrice() {
        return price;
    }

    public ItemStack getSellStack() {
        return sell.copy();
    }

    public static NbtCompound toTag(NbtCompound tag, List<ShopOffer> offers) {

        NbtList offerList = new NbtList();

        for (ShopOffer offer : offers) {
            NbtCompound offerTag = new NbtCompound();
            offerTag.putInt("Price", offer.getPrice());

            NbtCompound item = new NbtCompound();
            offer.getSellStack().writeNbt(item);

            offerTag.put("Item", item);

            offerList.add(offerTag);
        }

        tag.put("Offers", offerList);

        return tag;
    }

    public static void fromTag(NbtCompound tag, List<ShopOffer> offers) {

        offers.clear();

        NbtList offerList = tag.getList("Offers", 10);

        for (NbtElement offerTag : offerList) {

            NbtCompound offer = (NbtCompound) offerTag;

            int price = offer.getInt("Price");

            ItemStack sell = ItemStack.fromNbt(offer.getCompound("Item"));

            offers.add(new ShopOffer(sell, price));
        }
    }

    @Override
    public String toString() {
        return this.sell + "@" + this.price + "coins";
    }
}
