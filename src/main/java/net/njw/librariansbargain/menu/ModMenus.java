package net.njw.librariansbargain.menu;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.njw.librariansbargain.LibrariansBargain;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, LibrariansBargain.MODID);
    public static final Supplier<MenuType<BargainMenu>> BARGAIN_MENU =
            MENUS.register("bargain", () -> new MenuType<>(BargainMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}