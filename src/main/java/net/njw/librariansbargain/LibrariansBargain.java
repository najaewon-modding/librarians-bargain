package net.njw.librariansbargain;

import com.mojang.logging.LogUtils;
import net.njw.librariansbargain.bargain.ContractInteractionHandler;
import net.njw.librariansbargain.item.ModItems;
import net.njw.librariansbargain.menu.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(LibrariansBargain.MODID)
public class LibrariansBargain {
    public static final String MODID = "njw_librarians_bargain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LibrariansBargain(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ContractInteractionHandler::onEntityInteract);
    }
}