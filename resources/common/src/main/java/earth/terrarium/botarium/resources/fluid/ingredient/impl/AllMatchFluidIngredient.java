package earth.terrarium.botarium.resources.fluid.ingredient.impl;

import com.mojang.serialization.MapCodec;
import earth.terrarium.botarium.resources.fluid.FluidResource;
import earth.terrarium.botarium.resources.fluid.ingredient.FluidIngredient;
import earth.terrarium.botarium.resources.fluid.ingredient.FluidIngredientType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record AllMatchFluidIngredient(List<FluidIngredient> children) implements ListFluidIngredient {
    public static final MapCodec<AllMatchFluidIngredient> CODEC = FluidIngredient.CODEC.listOf().fieldOf("children").xmap(AllMatchFluidIngredient::new, AllMatchFluidIngredient::children);
    public static final FluidIngredientType<AllMatchFluidIngredient> TYPE = new FluidIngredientType<>(new ResourceLocation("botarium", "all_match"), CODEC);

    @Override
    public boolean test(FluidResource fluidResource) {
        for (FluidIngredient fluidIngredient : children) {
            if (!fluidIngredient.test(fluidResource)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<FluidResource> getMatchingFluids() {
        List<FluidResource> previewStacks = new ArrayList<>();

        for (FluidIngredient ingredient : children) {
            previewStacks.addAll(ingredient.getMatchingFluids());
        }

        return previewStacks;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }
}