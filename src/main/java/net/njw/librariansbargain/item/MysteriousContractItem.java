package net.njw.librariansbargain.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MysteriousContractItem extends Item {
    public MysteriousContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}