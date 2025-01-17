package earth.terrarium.common_storage_lib.storage.fabric;

import earth.terrarium.common_storage_lib.resources.fluid.FluidResource;
import earth.terrarium.common_storage_lib.resources.item.ItemResource;
import earth.terrarium.common_storage_lib.storage.ConversionUtils;
import earth.terrarium.common_storage_lib.resources.Resource;
import earth.terrarium.common_storage_lib.storage.base.CommonStorage;
import earth.terrarium.common_storage_lib.storage.base.StorageSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;

public class FabricWrappedContainer<U extends Resource, V extends TransferVariant<?>> implements SlottedStorage<V> {
    private final CommonStorage<U> container;
    private final OptionalSnapshotParticipant<?> updateManager;
    private final Function<U, V> toVariant;
    private final Function<V, U> toResource;

    public FabricWrappedContainer(
            CommonStorage<U> container, OptionalSnapshotParticipant<?> updateManager, Function<U, V> toVariant,
            Function<V, U> toResource) {
        this.container = container;
        this.updateManager = updateManager;
        this.toVariant = toVariant;
        this.toResource = toResource;
    }

    public FabricWrappedContainer(CommonStorage<U> container, Function<U, V> toVariant, Function<V, U> toResource) {
        this(container, OptionalSnapshotParticipant.of(container), toVariant, toResource);
    }

    public U toResource(V variant) {
        return toResource.apply(variant);
    }

    public V toVariant(U resource) {
        return toVariant.apply(resource);
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        U holder = toResource(resource);
        updateSnapshots(transaction);
        return container.insert(holder, maxAmount, false);
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        U holder = toResource(resource);
        updateSnapshots(transaction);
        return container.extract(holder, maxAmount, false);
    }

    @Override
    public @NotNull Iterator<StorageView<V>> iterator() {
        return new Iterator<>() {
            int slot = 0;

            @Override
            public boolean hasNext() {
                return slot < container.size();
            }

            @Override
            public StorageView<V> next() {
                return FabricWrappedContainer.this.getSlot(slot++);
            }
        };
    }

    @Override
    public int getSlotCount() {
        return container.size();
    }

    @Override
    public SingleSlotStorage<V> getSlot(int slot) {
        StorageSlot<U> storageSlot = container.get(slot);
        return new FabricWrappedSlot<>(storageSlot, this::toVariant, this::toResource);
    }

    private void updateSnapshots(TransactionContext transaction) {
        if (updateManager != null) {
            updateManager.updateSnapshots(transaction);
        }
    }

    public CommonStorage<U> container() {
        return container;
    }

    public OptionalSnapshotParticipant<?> updateManager() {
        return updateManager;
    }

    public static class OfFluid extends FabricWrappedContainer<FluidResource, FluidVariant> {
        public OfFluid(CommonStorage<FluidResource> container) {
            super(container, ConversionUtils::toVariant, ConversionUtils::toResource);
        }
    }

    public static class OfItem extends FabricWrappedContainer<ItemResource, ItemVariant> {
        public OfItem(CommonStorage<ItemResource> container) {
            super(container, ConversionUtils::toVariant, ConversionUtils::toResource);
        }
    }
}
