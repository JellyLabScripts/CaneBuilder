package com.jelly.CaneBuilder.mixins;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer {
    @Shadow protected abstract void updateActivePotionEffects();
}
