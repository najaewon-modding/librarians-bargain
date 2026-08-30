package net.njw.librariansbargain.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.njw.librariansbargain.bargain.BargainService;
import net.njw.librariansbargain.menu.BargainMenu;

public class BargainScreen extends AbstractContainerScreen<BargainMenu> {
    private static final int RIGHT_X = 132;
    private static final int RIGHT_WIDTH = 240;
    private Button enchantmentLockButton;
    private Button levelLockButton;
    private Button bargainButton;
    private final Button[] proposalButtons = new Button[3];

    public BargainScreen(BargainMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 380, 208);
    }

    @Override
    protected void init() {
        super.init();
        enchantmentLockButton = Button.builder(Component.empty(), button -> sendMenuButton(4))
                .bounds(leftPos + 8, topPos + 34, 104, 20).build();
        levelLockButton = Button.builder(Component.empty(), button -> sendMenuButton(5))
                .bounds(leftPos + 8, topPos + 61, 104, 20).build();
        bargainButton = Button.builder(Component.empty(), button -> sendMenuButton(0))
                .bounds(leftPos + 8, topPos + 157, 104, 20).build();
        addRenderableWidget(enchantmentLockButton);
        addRenderableWidget(levelLockButton);
        addRenderableWidget(bargainButton);

        for (int i = 0; i < proposalButtons.length; i++) {
            int index = i;
            int y = topPos + 97 + i * 32;
            proposalButtons[i] = Button.builder(Component.empty(), button -> sendMenuButton(index + 1))
                    .bounds(leftPos + RIGHT_X, y, RIGHT_WIDTH, 28).build();
            addRenderableWidget(proposalButtons[i]);
        }

        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void sendMenuButton(int buttonId) {
        if (minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = super.mouseReleased(event);
        setFocused(null);
        return handled;
    }

    private void updateButtons() {
        if (bargainButton == null) return;

        boolean proposalsReady = menu.areProposalsReady();

        enchantmentLockButton.active = proposalsReady;
        enchantmentLockButton.setMessage(Component.translatable(menu.isEnchantmentLocked()
                ? "menu.njw_librarians_bargain.enchantment_locked"
                : "menu.njw_librarians_bargain.enchantment_lock"));

        levelLockButton.active = proposalsReady && menu.isEnchantmentLocked();
        levelLockButton.setMessage(Component.translatable(menu.isLevelLocked()
                ? "menu.njw_librarians_bargain.level_locked"
                : "menu.njw_librarians_bargain.level_lock"));

        bargainButton.setMessage(Component.translatable(proposalsReady
                ? "menu.njw_librarians_bargain.rebargain"
                : "menu.njw_librarians_bargain.bargain_action"));

        for (int i = 0; i < proposalButtons.length; i++) {
            boolean ready = proposalsReady && !menu.getProposalBook(i).isEmpty();
            proposalButtons[i].active = ready;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawMainBackground(graphics);
        drawDivider(graphics);
        drawCurrentOffer(graphics);
        drawDiamondCost(graphics);
    }

    private void drawMainBackground(GuiGraphicsExtractor graphics) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + imageWidth, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + imageHeight, 0xFFFFFFFF);
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF555555);
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF555555);
    }

    private void drawDivider(GuiGraphicsExtractor graphics) {
        int x = leftPos + 123;
        graphics.fill(x, topPos + 24, x + 1, topPos + 198, 0xFF8B8B8B);
        graphics.fill(x + 1, topPos + 24, x + 2, topPos + 198, 0xFFFFFFFF);
    }

    private void drawCurrentOffer(GuiGraphicsExtractor graphics) {
        ItemStack book = menu.getCurrentBook();
        if (book.isEmpty()) return;

        int x = leftPos + RIGHT_X;
        int y = topPos + 42;
        drawLightTradeRow(graphics, x, y, RIGHT_WIDTH, 30);
        drawTrade(graphics, book, menu.getCurrentPrice(), x, y, 30);
    }

    private void drawLightTradeRow(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF000000);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFFC6C6C6);
    }

    private void drawTrade(GuiGraphicsExtractor graphics, ItemStack enchantedBook, int price, int x, int y,
                           int height) {
        int itemY = y + (height - 16) / 2;
        int textY = y + (height - 9) / 2;
        ItemStack emeralds = new ItemStack(Items.EMERALD, price);
        ItemStack normalBook = new ItemStack(Items.BOOK);
        Component enchantment = BargainService.getEnchantmentName(enchantedBook).copy()
                .withStyle(style -> style.withColor(0x404040));

        graphics.item(emeralds, x + 7, itemY - 1);
        graphics.itemDecorations(font, emeralds, x + 8, itemY - 1);
        graphics.text(font, "+", x + 31, textY, 0xFF404040, false);
        graphics.item(normalBook, x + 43, itemY);
        graphics.text(font, "→", x + 65, textY, 0xFF404040, false);
        graphics.item(enchantedBook, x + 80, itemY);
        graphics.text(font, enchantment, x + 102, textY, 0xFF404040, false);
    }

    private void drawProposalTrade(GuiGraphicsExtractor graphics, ItemStack enchantedBook, int price, int x,
                                   int y) {
        int itemY = y + 6;
        int textY = y + 9;
        ItemStack emeralds = new ItemStack(Items.EMERALD, price);
        ItemStack normalBook = new ItemStack(Items.BOOK);
        Component enchantment = BargainService.getEnchantmentName(enchantedBook).copy()
                .withStyle(style -> style.withColor(0xFFFFFF));

        graphics.item(emeralds, x + 7, itemY - 1);
        graphics.itemDecorations(font, emeralds, x + 8, itemY - 2);
        graphics.text(font, "+", x + 31, textY, 0xFFFFFFFF, true);
        graphics.item(normalBook, x + 43, itemY);
        graphics.text(font, "→", x + 65, textY, 0xFFFFFFFF, true);
        graphics.item(enchantedBook, x + 80, itemY);
        graphics.text(font, enchantment, x + 102, textY, 0xFFFFFFFF, true);
    }

    private void drawDiamondCost(GuiGraphicsExtractor graphics) {
        ItemStack diamond = new ItemStack(Items.DIAMOND);
        graphics.item(diamond, leftPos + 10, topPos + 132);
        graphics.text(font,
                Component.translatable("menu.njw_librarians_bargain.diamond_cost_value",
                        menu.getDiamondCost()),
                leftPos + 32, topPos + 136, 0xFF404040, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 8, 0xFF404040, false);
        graphics.text(font,
                Component.translatable("menu.njw_librarians_bargain.bargain_cost"),
                8, 116, 0xFF404040, false);
        graphics.text(font,
                Component.translatable("menu.njw_librarians_bargain.current_offer"),
                RIGHT_X, 27, 0xFF404040, false);
        graphics.text(font,
                Component.translatable("menu.njw_librarians_bargain.new_offers"),
                RIGHT_X, 82, 0xFF404040, false);

        for (int i = 0; i < proposalButtons.length; i++) {
            ItemStack book = menu.getProposalBook(i);
            if (book.isEmpty()) continue;
            int y = 97 + i * 32;
            drawProposalTrade(graphics, book, menu.getProposalPrice(i), RIGHT_X, y);
        }
    }
}