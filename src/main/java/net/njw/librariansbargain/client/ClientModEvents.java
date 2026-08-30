package net.njw.librariansbargain.client;

import net.njw.librariansbargain.LibrariansBargain;
import net.njw.librariansbargain.menu.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = LibrariansBargain.MODID)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BARGAIN_MENU.get(), BargainScreen::new);
    }
}