package earth.terrarium.botarium.resources.item.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.botarium.resources.ResourceStack;
import earth.terrarium.botarium.resources.item.ItemResource;
import earth.terrarium.botarium.resources.util.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public final class SizedItemIngredient {
    public static final Codec<SizedItemIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemIngredient.NON_EMPTY_MAP_CODEC.forGetter(SizedItemIngredient::ingredient),
                    CodecUtils.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "count", 1).forGetter(SizedItemIngredient::count))
            .apply(instance, SizedItemIngredient::new));

    public static final Codec<SizedItemIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(SizedItemIngredient::ingredient),
                    CodecUtils.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "count", 1).forGetter(SizedItemIngredient::count))
            .apply(instance, SizedItemIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedItemIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            SizedItemIngredient::ingredient,
            ByteBufCodecs.VAR_INT,
            SizedItemIngredient::count,
            SizedItemIngredient::new);

    public static SizedItemIngredient of(ItemLike item, int count) {
        return new SizedItemIngredient(Ingredient.of(item), count);
    }

    public static SizedItemIngredient of(TagKey<Item> tag, int count) {
        return new SizedItemIngredient(Ingredient.of(tag), count);
    }

    public static SizedItemIngredient of(ResourceStack<ItemResource> stack) {
        return new SizedItemIngredient(Ingredient.of(stack.resource()), (int) stack.amount());
    }

    private final Ingredient ingredient;
    private final int count;
    @Nullable
    private ItemStack[] cachedStacks;

    public SizedItemIngredient(Ingredient ingredient, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int count() {
        return count;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    public boolean test(ResourceStack<ItemResource> stack) {
        return ingredient.test(stack.resource().getCachedStack()) && stack.amount() >= count;
    }

    public ItemStack[] getItems() {
        if (cachedStacks == null) {
            cachedStacks = Stream.of(ingredient.getItems())
                    .map(s -> s.copyWithCount(count))
                    .toArray(ItemStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public String toString() {
        return count + "x " + ingredient;
    }
}