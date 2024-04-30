package earth.terrarium.botarium.item.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.botarium.storage.unit.TransferUnit;
import earth.terrarium.botarium.storage.unit.UnitStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public final class ItemUnit implements TransferUnit<Item, ItemUnit>, Predicate<ItemUnit> {
    public static final ItemUnit BLANK = ItemUnit.of(Items.AIR, DataComponentPatch.EMPTY);

    public static final Codec<ItemUnit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemUnit::getType),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemUnit::getDataPatch)
    ).apply(instance, ItemUnit::of));

    public static final MapCodec<ItemUnit> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemUnit::getType),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemUnit::getDataPatch)
    ).apply(instance, ItemUnit::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemUnit> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemUnit decode(RegistryFriendlyByteBuf object) {
            Holder<Item> holder = ITEM_HOLDER_STREAM_CODEC.decode(object);
            DataComponentPatch dataComponentPatch = DataComponentPatch.STREAM_CODEC.decode(object);
            return ItemUnit.of(holder.value(), dataComponentPatch);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf object, ItemUnit object2) {
            ITEM_HOLDER_STREAM_CODEC.encode(object, object2.type.builtInRegistryHolder());
            DataComponentPatch.STREAM_CODEC.encode(object, object2.getDataPatch());
        }
    };

    private final Item type;
    private final PatchedDataComponentMap components;

    private ItemStack cache;

    public ItemUnit(Item type, PatchedDataComponentMap components) {
        this.type = type;
        this.components = components;
    }

    public static ItemUnit of(ItemLike item) {
        return new ItemUnit(item.asItem(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, DataComponentPatch.EMPTY));
    }

    public static ItemUnit of(ItemLike item, DataComponentPatch components) {
        return new ItemUnit(item.asItem(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components));
    }

    public static ItemUnit of(ItemStack stack) {
        return of(stack.getItem(), stack.getComponentsPatch());
    }

    @Override
    public boolean isBlank() {
        return type == Items.AIR;
    }

    public boolean matches(ItemStack stack) {
        return isOf(stack.getItem()) && componentsMatch(stack.getComponents());
    }

    public ItemStack toItemStack(int count) {
        ItemStack stack = new ItemStack(type, count);
        stack.applyComponents(components);
        return stack;
    }

    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    public ItemStack getCachedStack() {
        ItemStack ret = cache;

        if (ret == null) {
            cache = ret = toItemStack();
        }

        return ret;
    }

    @Override
    public boolean test(ItemUnit unit) {
        return isOf(unit.type) && componentsMatch(unit.components);
    }

    @Override
    public Item getType() {
        return type;
    }

    @Override
    public DataComponentPatch getDataPatch() {
        return components.asPatch();
    }

    @Override
    public <D> ItemUnit set(DataComponentType<D> type, D value) {
        PatchedDataComponentMap copy = components.copy();
        copy.set(type, value);
        return new ItemUnit(this.type, copy);
    }

    @Override
    public ItemUnit modify(DataComponentPatch patch) {
        PatchedDataComponentMap copy = components.copy();
        copy.applyPatch(patch);
        return new ItemUnit(this.type, copy);
    }

    @Override
    public UnitStack<ItemUnit> toStack(long amount) {
        return new UnitStack<>(this, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemUnit) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, components);
    }

    @Override
    public String toString() {
        return "ItemUnit[" +
                "type=" + type + ", " +
                "components=" + components + ']';
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return components;
    }
}