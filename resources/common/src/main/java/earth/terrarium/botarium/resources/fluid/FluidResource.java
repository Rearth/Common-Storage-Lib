package earth.terrarium.botarium.resources.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.botarium.resources.TransferResource;
import earth.terrarium.botarium.resources.ResourceStack;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidResource extends TransferResource<Fluid, FluidResource> {
    public static final FluidResource BLANK = FluidResource.of(Fluids.EMPTY, DataComponentPatch.EMPTY);

    public static final MapCodec<FluidResource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidResource::getType),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidResource::getDataPatch)
    ).apply(instance, FluidResource::of));

    public static final Codec<FluidResource> CODEC = MAP_CODEC.codec();

    public static final Codec<FluidResource> SIMPLE_CODEC = BuiltInRegistries.FLUID.byNameCodec().xmap(FluidResource::of, FluidResource::getType);

    public static final StreamCodec<? super RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidResource::asHolder,
            DataComponentPatch.STREAM_CODEC,
            FluidResource::getDataPatch,
            FluidResource::of
    );

    public static FluidResource of(Fluid fluid) {
        return new FluidResource(fluid, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, DataComponentPatch.EMPTY));
    }

    public static FluidResource of(Holder<Fluid> holder) {
        return of(holder.value());
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch components) {
        return new FluidResource(fluid, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components));
    }

    public static FluidResource of(Holder<Fluid> holder, DataComponentPatch components) {
        return of(holder.value(), components);
    }

    public FluidResource(Fluid type, PatchedDataComponentMap components) {
        super(type, components);
    }

    @Override
    public boolean isBlank() {
        return getType() == Fluids.EMPTY;
    }

    @Override
    public <D> FluidResource set(DataComponentType<D> type, D value) {
        PatchedDataComponentMap copy = components.copy();
        copy.set(type, value);
        return new FluidResource(this.type, copy);
    }

    @Override
    public FluidResource modify(DataComponentPatch patch) {
        PatchedDataComponentMap copy = components.copy();
        copy.applyPatch(patch);
        return new FluidResource(this.type, copy);
    }

    @Override
    public ResourceStack<FluidResource> toStack(long amount) {
        return new ResourceStack<>(this, amount);
    }

    public Holder<Fluid> asHolder() {
        return type.builtInRegistryHolder();
    }

    public boolean is(TagKey<Fluid> tag) {
        return type.is(tag);
    }
}
