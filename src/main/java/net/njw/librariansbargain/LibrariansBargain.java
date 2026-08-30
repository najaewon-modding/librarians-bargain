package net.njw.librariansbargain;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(LibrariansBargain.MODID)
public class LibrariansBargain {
    public static final String MODID = "njw_librarians_bargain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LibrariansBargain(IEventBus modEventBus, ModContainer modContainer) {
    }
}
