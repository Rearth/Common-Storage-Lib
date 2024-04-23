package earth.terrarium.botarium.common.lookup;

import earth.terrarium.botarium.common.lookup.impl.FabricEntityLookup;
import net.minecraft.resources.ResourceLocation;
import net.msrandom.multiplatform.annotations.Actual;

public interface EntityLookupActual {
    @Actual
    static <T, C> EntityLookup<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return new FabricEntityLookup<>(name, typeClass, contextClass);
    }
}