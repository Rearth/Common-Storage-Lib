package earth.terrarium.botarium.item.impl;

import earth.terrarium.botarium.item.base.ItemUnit;
import earth.terrarium.botarium.storage.base.StorageSlot;
import earth.terrarium.botarium.storage.base.UpdateManager;
import earth.terrarium.botarium.storage.unit.UnitStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class SimpleItemSlot implements StorageSlot<ItemUnit>, UpdateManager<UnitStack<ItemUnit>> {
    private final Runnable update;
    private ItemUnit unit;
    private long amount;

    public SimpleItemSlot(Runnable update) {
        this.unit = ItemUnit.BLANK;
        this.amount = getAmount();
        this.update = update;
    }

    public SimpleItemSlot(ItemStack stack) {
        this.unit = ItemUnit.of(stack);
        this.amount = stack.getCount();
        this.update = () -> {};
    }

    @Override
    public long getLimit() {
        return unit.isBlank() ? Item.ABSOLUTE_MAX_STACK_SIZE : unit.getCachedStack().getMaxStackSize();
    }

    @Override
    public boolean isValueValid(ItemUnit unit) {
        return true;
    }

    @Override
    public ItemUnit getUnit() {
        return unit;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isBlank() {
        return unit.isBlank();
    }

    public void set(ItemUnit unit, long amount) {
        this.unit = unit;
        this.amount = amount;
    }

    public void set(ItemStack stack) {
        this.unit = ItemUnit.of(stack);
        this.amount = stack.getCount();
    }

    public void set(UnitStack<ItemUnit> data) {
        this.unit = data.unit();
        this.amount = data.amount();
    }

    @Override
    public long insert(ItemUnit unit, long amount, boolean simulate) {
        if (!isValueValid(unit)) return 0;
        if (this.unit.isBlank()) {
            long inserted = Math.min(amount, unit.getCachedStack().getMaxStackSize());
            if (!simulate) {
                this.unit = unit;
                this.amount = inserted;
            }
            return inserted;
        } else if (this.unit.matches(unit)) {
            long inserted = Math.min(amount, getLimit() - this.amount);
            if (!simulate) {
                this.amount += inserted;
            }
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(ItemUnit unit, long amount, boolean simulate) {
        if (this.unit.matches(unit)) {
            long extracted = Math.min(amount, this.amount);
            if (!simulate) {
                this.amount -= extracted;
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public UnitStack<ItemUnit> createSnapshot() {
        return new UnitStack<>(unit, amount);
    }

    @Override
    public void readSnapshot(UnitStack<ItemUnit> snapshot) {
        this.unit = snapshot.unit();
        this.amount = snapshot.amount();
    }

    @Override
    public void update() {
        update.run();
    }

    public static class Filtered extends SimpleItemSlot {
        private final Predicate<ItemUnit> filter;

        public Filtered(Runnable update, Predicate<ItemUnit> filter) {
            super(update);
            this.filter = filter;
        }

        @Override
        public boolean isValueValid(ItemUnit unit) {
            return filter.test(unit);
        }
    }
}