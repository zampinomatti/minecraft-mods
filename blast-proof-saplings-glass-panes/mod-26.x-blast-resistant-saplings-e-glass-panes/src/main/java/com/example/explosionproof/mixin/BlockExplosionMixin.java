package com.example.explosionproof.mixin;

import com.example.explosionproof.ExplosionProofDecor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin su {@code Block} (non su {@code BlockBehaviour}!).
 * <p>
 * Correzione rispetto alla prima versione: in vanilla/Fabric il metodo
 * {@code getExplosionResistance()} è dichiarato direttamente su
 * {@code Block} ed è SENZA PARAMETRI (è deprecato, ma è ancora il metodo
 * che il motore delle esplosioni interroga). La firma a 4 parametri
 * ({@code BlockState, LevelReader, BlockPos, Explosion}) usata nella prima
 * versione esiste solo in NeoForge (interfaccia {@code IBlockExtension}),
 * non in vanilla: per questo il mixin non trovava alcun target.
 * <p>
 * {@code ResourceLocation} è stato rinominato {@code Identifier} da Mojang
 * stessa a partire da 1.21.11 (l'ultima versione obfuscata prima di 26.1) —
 * per una bella ironia, lo stesso nome che Yarn/Fabric usava da sempre.
 */
@Mixin(Block.class)
public abstract class BlockExplosionMixin {

	@Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
	private void explosionproofDecor$boostResistance(CallbackInfoReturnable<Float> cir) {
		Block self = (Block) (Object) this;
		Identifier id = BuiltInRegistries.BLOCK.getKey(self);
		if (id != null && ExplosionProofDecor.isTargetBlockPath(id.getPath())) {
			cir.setReturnValue(ExplosionProofDecor.TARGET_RESISTANCE);
		}
	}
}
