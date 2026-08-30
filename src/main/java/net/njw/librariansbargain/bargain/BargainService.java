package net.njw.librariansbargain.bargain;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public final class BargainService {
    private BargainService() {}

    public static ValidationResult validate(Entity target) {
        if (!(target instanceof Villager villager)) return ValidationResult.NOT_LIBRARIAN;
        if (!villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)) {
            return ValidationResult.NOT_LIBRARIAN;
        }
        if (villager.getVillagerXp() > 0 || villager.getVillagerData().level() > 1) {
            return ValidationResult.ALREADY_TRADED;
        }
        if (findEnchantedBookOffer(villager) == null) return ValidationResult.NO_ENCHANTED_BOOK;
        return ValidationResult.VALID;
    }

    public static MerchantOffer findEnchantedBookOffer(Villager villager) {
        return villager.getOffers().stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK))
                .findFirst().orElse(null);
    }

    public static EnchantmentData getEnchantmentData(ItemStack book) {
        ItemEnchantments enchantments = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS,
                ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return null;
        var entry = enchantments.entrySet().iterator().next();
        return new EnchantmentData(entry.getKey(), entry.getIntValue());
    }

    public static MerchantOffer createRandomOffer(ServerLevel level, RandomSource random,
                                                  EnchantmentData current, boolean lockEnchantment,
                                                  boolean lockLevel) {
        Holder<Enchantment> enchantment = lockEnchantment
                ? current.enchantment()
                : getRandomTradeableEnchantment(level, random);
        int enchantmentLevel = lockEnchantment && lockLevel
                ? current.level()
                : Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
        int price = getRandomPrice(random, enchantment, enchantmentLevel);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(enchantment, enchantmentLevel);
        return new MerchantOffer(new ItemCost(Items.EMERALD, price),
                Optional.of(new ItemCost(Items.BOOK, 1)), book, 12, 1, 0.2f);
    }

    private static Holder<Enchantment> getRandomTradeableEnchantment(ServerLevel level, RandomSource random) {
        List<Holder<Enchantment>> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(EnchantmentTags.TRADEABLE).stream().toList();
        if (enchantments.isEmpty()) throw new IllegalStateException("No tradeable enchantments are available.");
        return enchantments.get(random.nextInt(enchantments.size()));
    }

    private static int getRandomPrice(RandomSource random, Holder<Enchantment> enchantment, int level) {
        int price = 2 + random.nextInt(5 + level * 10) + 3 * level;
        if (enchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) price *= 2;
        return Mth.clamp(price, 1, Items.EMERALD.getDefaultMaxStackSize());
    }

    public static Component getEnchantmentName(ItemStack book) {
        EnchantmentData data = getEnchantmentData(book);
        return data == null ? Component.literal("-") : Enchantment.getFullname(data.enchantment(), data.level());
    }

    public record EnchantmentData(Holder<Enchantment> enchantment, int level) {}

    public enum ValidationResult {
        VALID,
        NOT_LIBRARIAN,
        ALREADY_TRADED,
        NO_ENCHANTED_BOOK
    }
}