package earth.terrarium.botarium.common.context.impl;

import earth.terrarium.botarium.common.context.ItemContext;
import earth.terrarium.botarium.common.storage.base.ContainerSlot;
import earth.terrarium.botarium.common.storage.base.SlottedContainer;
import net.minecraft.world.item.ItemStack;

public record SimpleItemContext(SlottedContainer<ItemStack> outerContainer, ContainerSlot<ItemStack> mainSlot) implements ItemContext {
}
