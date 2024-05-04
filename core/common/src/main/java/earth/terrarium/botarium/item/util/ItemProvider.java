package earth.terrarium.botarium.item.util;

import earth.terrarium.botarium.context.ItemContext;
import earth.terrarium.botarium.resources.item.ItemResource;
import earth.terrarium.botarium.storage.base.CommonStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemProvider {
    private ItemProvider() {}

    public interface Block {
        CommonStorage<ItemResource> getItems(Level level, BlockPos pos, @Nullable BlockState state, @Nullable net.minecraft.world.level.block.entity.BlockEntity entity, @Nullable Direction direction);
    }

    public interface BlockEntity {
        CommonStorage<ItemResource> getItems(@Nullable Direction direction);
    }

    public interface Entity {
        CommonStorage<ItemResource> getItems();
    }

    public interface AutomationEntity {
        CommonStorage<ItemResource> getItems(@Nullable Direction entity);
    }

    public interface Item {
        CommonStorage<ItemResource> getItems(ItemStack stack, ItemContext context);
    }
}