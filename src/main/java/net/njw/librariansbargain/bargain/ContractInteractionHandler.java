package net.njw.librariansbargain.bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.njw.librariansbargain.item.ModItems;
import net.njw.librariansbargain.menu.BargainMenu;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ContractInteractionHandler {
    private ContractInteractionHandler() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItems.MYSTERIOUS_CONTRACT.get())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        BargainService.ValidationResult result = BargainService.validate(event.getTarget());
        if (result == BargainService.ValidationResult.VALID) {
            if (player instanceof ServerPlayer serverPlayer && event.getTarget() instanceof Villager villager) {
                openBargainMenu(serverPlayer, villager);
            }
            return;
        }
        String messageKey = switch (result) {
            case NOT_LIBRARIAN -> "message.njw_librarians_bargain.not_librarian";
            case ALREADY_TRADED -> "message.njw_librarians_bargain.already_traded";
            case NO_ENCHANTED_BOOK -> "message.njw_librarians_bargain.no_enchanted_book";
            case VALID -> throw new IllegalStateException();
        };
        player.sendOverlayMessage(Component.translatable(messageKey));
    }

    private static void openBargainMenu(ServerPlayer player, Villager villager) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new BargainMenu(containerId, inventory, villager),
                Component.translatable("menu.njw_librarians_bargain.bargain")));
    }
}