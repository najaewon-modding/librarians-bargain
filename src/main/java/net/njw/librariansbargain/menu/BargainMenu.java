package net.njw.librariansbargain.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.njw.librariansbargain.bargain.BargainService;

public class BargainMenu extends AbstractContainerMenu {
    private static final int DISPLAY_SLOT_COUNT = 4;
    private static final int DATA_COUNT = 9;
    private static final int CURRENT_PRICE = 0;
    private static final int READY = 4;
    private static final int SELECTED = 5;
    private static final int LOCK_ENCHANTMENT = 6;
    private static final int LOCK_LEVEL = 7;
    private static final int DIAMOND_COUNT = 8;
    private final Inventory inventory;
    private final Villager villager;
    private final Container displayItems;
    private final ContainerData data;
    private final MerchantOffer[] proposals = new MerchantOffer[3];

    public BargainMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null, new SimpleContainer(DISPLAY_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    public BargainMenu(int containerId, Inventory inventory, Villager villager) {
        this(containerId, inventory, villager, new SimpleContainer(DISPLAY_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
        MerchantOffer currentOffer = BargainService.findEnchantedBookOffer(villager);
        if (currentOffer == null) return;
        displayItems.setItem(0, currentOffer.getResult().copy());
        data.set(CURRENT_PRICE, currentOffer.getCostA().getCount());
        data.set(DIAMOND_COUNT, countDiamonds(inventory));
    }

    private BargainMenu(int containerId, Inventory inventory, Villager villager, Container displayItems,
                        ContainerData data) {
        super(ModMenus.BARGAIN_MENU.get(), containerId);
        this.inventory = inventory;
        this.villager = villager;
        this.displayItems = displayItems;
        this.data = data;
        for (int i = 0; i < DISPLAY_SLOT_COUNT; i++) {
            addSlot(new DisplaySlot(displayItems, i));
        }
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    public ItemStack getCurrentBook() {
        return displayItems.getItem(0);
    }

    public int getCurrentPrice() {
        return data.get(CURRENT_PRICE);
    }

    public ItemStack getProposalBook(int index) {
        return displayItems.getItem(index + 1);
    }

    public int getProposalPrice(int index) {
        return data.get(index + 1);
    }

    public boolean areProposalsReady() {
        return data.get(READY) == 1;
    }

    public boolean isEnchantmentLocked() {
        return data.get(LOCK_ENCHANTMENT) == 1;
    }

    public boolean isLevelLocked() {
        return data.get(LOCK_LEVEL) == 1;
    }

    public int getDiamondCost() {
        if (!areProposalsReady()) return 1;
        return 1 + (isEnchantmentLocked() ? 1 : 0) + (isLevelLocked() ? 1 : 0);
    }

    public int getDiamondCount() {
        return data.get(DIAMOND_COUNT);
    }

    public boolean hasEnoughDiamonds() {
        return getDiamondCount() >= getDiamondCost();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (villager == null) return false;
        if (buttonId == 0) return generateProposals(player);
        if (buttonId >= 1 && buttonId <= 3) return applyProposal(buttonId - 1);
        if (buttonId == 4) return toggleEnchantmentLock();
        if (buttonId == 5) return toggleLevelLock();
        return false;
    }

    private boolean applyProposal(int index) {
        if (!areProposalsReady() || index < 0 || index >= proposals.length) return false;

        MerchantOffer proposal = proposals[index];
        if (proposal == null) return false;

        if (BargainService.validate(villager) != BargainService.ValidationResult.VALID) {
            return false;
        }

        MerchantOffer currentOffer = BargainService.findEnchantedBookOffer(villager);
        if (currentOffer == null) return false;

        int currentOfferIndex = villager.getOffers().indexOf(currentOffer);
        if (currentOfferIndex < 0) return false;

        villager.getOffers().set(currentOfferIndex, proposal);
        displayItems.setItem(0, proposal.getResult().copy());
        data.set(CURRENT_PRICE, proposal.getCostA().getCount());

        clearProposals();
        broadcastChanges();
        return true;
    }

    private boolean toggleEnchantmentLock() {
        if (!areProposalsReady()) return false;

        boolean locked = !isEnchantmentLocked();
        data.set(LOCK_ENCHANTMENT, locked ? 1 : 0);

        if (!locked) {
            data.set(LOCK_LEVEL, 0);
        }

        broadcastChanges();
        return true;
    }

    private boolean toggleLevelLock() {
        if (!areProposalsReady() || !isEnchantmentLocked()) return false;

        data.set(LOCK_LEVEL, isLevelLocked() ? 0 : 1);
        broadcastChanges();
        return true;
    }

    private boolean generateProposals(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        if (BargainService.validate(villager) != BargainService.ValidationResult.VALID) {
            return false;
        }

        boolean rerolling = areProposalsReady();
        BargainService.EnchantmentData[] previous =
                new BargainService.EnchantmentData[proposals.length];

        if (rerolling) {
            for (int i = 0; i < proposals.length; i++) {
                MerchantOffer proposal = proposals[i];
                if (proposal == null) return false;

                previous[i] = BargainService.getEnchantmentData(proposal.getResult());
                if (previous[i] == null) return false;
            }
        }

        int diamondCost = getDiamondCost();

        if (!takeDiamonds(player, diamondCost)) {
            broadcastChanges();
            return false;
        }

        if (!rerolling) {
            return generateInitialProposals(level, player);
        }

        generateRerolledProposals(level, player, previous);
        return true;
    }

    private boolean generateInitialProposals(ServerLevel level, Player player) {
        MerchantOffer currentOffer = BargainService.findEnchantedBookOffer(villager);
        if (currentOffer == null) return false;

        proposals[0] = currentOffer;

        for (int i = 1; i < proposals.length; i++) {
            proposals[i] = BargainService.createRandomOffer(
                    level,
                    player.getRandom(),
                    null,
                    false,
                    false
            );
        }

        syncProposals();
        return true;
    }

    private void generateRerolledProposals(ServerLevel level, Player player,
                                           BargainService.EnchantmentData[] previous) {
        boolean lockEnchantment = isEnchantmentLocked();
        boolean lockLevel = lockEnchantment && isLevelLocked();

        for (int i = 0; i < proposals.length; i++) {
            proposals[i] = BargainService.createRandomOffer(
                    level,
                    player.getRandom(),
                    previous[i],
                    lockEnchantment,
                    lockLevel
            );
        }

        syncProposals();
    }

    private void syncProposals() {
        for (int i = 0; i < proposals.length; i++) {
            displayItems.setItem(i + 1, proposals[i].getResult().copy());
            data.set(i + 1, proposals[i].getCostA().getCount());
        }

        data.set(READY, 1);
        data.set(SELECTED, 0);
        broadcastChanges();
    }

    private void clearProposals() {
        for (int i = 0; i < proposals.length; i++) {
            proposals[i] = null;
            displayItems.setItem(i + 1, ItemStack.EMPTY);
            data.set(i + 1, 0);
        }

        data.set(READY, 0);
        data.set(SELECTED, 0);
        data.set(LOCK_ENCHANTMENT, 0);
        data.set(LOCK_LEVEL, 0);
    }

    private boolean takeDiamonds(Player player, int amount) {
        int found = countDiamonds(inventory);
        data.set(DIAMOND_COUNT, found);

        if (found < amount) return false;

        int remaining = amount;

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(Items.DIAMOND)) continue;

            int removed = Math.min(stack.getCount(), remaining);
            stack.shrink(removed);
            remaining -= removed;
        }

        inventory.setChanged();
        data.set(DIAMOND_COUNT, countDiamonds(inventory));

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }

        return true;
    }

    private static int countDiamonds(Inventory inventory) {
        int count = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.DIAMOND)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    @Override
    public void broadcastChanges() {
        if (villager != null) {
            data.set(DIAMOND_COUNT, countDiamonds(inventory));
        }

        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return villager == null || villager.isAlive() && player.distanceToSqr(villager) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static class DisplaySlot extends Slot {
        private DisplaySlot(Container container, int index) {
            super(container, index, -1000, -1000);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}