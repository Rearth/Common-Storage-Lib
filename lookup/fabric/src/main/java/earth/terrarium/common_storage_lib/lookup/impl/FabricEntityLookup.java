package earth.terrarium.common_storage_lib.lookup.impl;

import earth.terrarium.common_storage_lib.lookup.EntityLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FabricEntityLookup<T, C> implements EntityLookup<T, C> {
    private final EntityApiLookup<T, C> lookup;

    public FabricEntityLookup(EntityApiLookup<T, C> lookup) {
        this.lookup = lookup;
    }

    public FabricEntityLookup(ResourceLocation id, Class<T> type, Class<C> contextType) {
        this(EntityApiLookup.get(id, type, contextType));
    }

    @Override
    public @Nullable T find(Entity entity, C context) {
        return lookup.find(entity, context);
    }

    @Override
    public void onRegister(Consumer<EntityRegistrar<T, C>> registrar) {
        registrar.accept((getter, containers) -> lookup.registerForTypes(getter::getContainer, containers));
    }

    @Override
    public void registerFallback(EntityGetter<T, C> getter) {
        lookup.registerFallback(getter::getContainer);
    }
}
