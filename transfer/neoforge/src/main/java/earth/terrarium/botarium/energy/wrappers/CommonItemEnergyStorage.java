package earth.terrarium.botarium.energy.wrappers;

import earth.terrarium.botarium.context.ItemContext;
import earth.terrarium.botarium.item.base.ItemUnit;
import earth.terrarium.botarium.storage.util.TransferUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public record CommonItemEnergyStorage(IEnergyStorage storage, ItemStack stack, ItemContext context) implements AbstractCommonEnergyStorage {
    @Override
    public long insert(long amount, boolean simulate) {
        long inserted = AbstractCommonEnergyStorage.super.insert(amount, simulate);
        if (!simulate) updateContext();
        return inserted;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        long extract = AbstractCommonEnergyStorage.super.extract(amount, simulate);
        if (!simulate) updateContext();
        return extract;
    }

    public void updateContext() {
        if (!context.getUnit().matches(stack)) {
            context.exchange(ItemUnit.of(stack), context.getAmount(), false);
        }
        if (context.getAmount() != stack.getCount()) {
            TransferUtil.equalize(context.mainSlot(), stack.getCount());
        }
    }
}