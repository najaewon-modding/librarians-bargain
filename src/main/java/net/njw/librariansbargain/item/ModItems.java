package net.njw.librariansbargain.item;

import net.njw.librariansbargain.LibrariansBargain;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LibrariansBargain.MODID);
    public static final DeferredItem<MysteriousContractItem> MYSTERIOUS_CONTRACT = ITEMS.registerItem(
            "mysterious_contract", MysteriousContractItem::new, properties -> properties.stacksTo(1));

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}