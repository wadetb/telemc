package com.wadeb.telemc.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class TeleMCClientMixin {

	@Inject(at = @At("HEAD"), method = "run")
	private void run(CallbackInfo info) {
		((MinecraftClient)(Object)this).getWindow().setTitle("Crafty");
	}

	@Inject(at = @At("HEAD"), method = "updateWindowTitle", cancellable = true)
	private void updateWindowTitle(CallbackInfo info) {
		// Suppress Minecraft window title updates.
		info.cancel();
	}

	@Inject(at = @At("HEAD"), method = "isWindowFocused", cancellable = true)
	private void isWindowFocused(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
	}

}