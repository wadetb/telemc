package com.wadeb.telemc.client.mixin;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.util.InputUtil;

import com.wadeb.telemc.client.TeleMCInputManager;

@Mixin(InputUtil.class)
public class TeleMCInputUtilMixin {
	// @Inject(at = @At("HEAD"), method = "isKeyPressed", cancellable = true)
	// private static void isKeyPressed(CallbackInfoReturnable<Boolean> info) {
	// 	// Don't report any keys as being pressed. Unsure what the impact will be.
	// 	info.setReturnValue(false);
	// }

    @Inject(at = @At("HEAD"), method = "setKeyboardCallbacks", cancellable = true)
	private static void setKeyboardCallbacks(long handle, GLFWKeyCallbackI keyCallback, GLFWCharModsCallbackI charModsCallback, CallbackInfo info) {
		TeleMCInputManager inputManager = TeleMCInputManager.getInstance();

		inputManager.originalWindow = handle;

		inputManager.originalKeyCallback = keyCallback;
		inputManager.originalCharModsCallback = charModsCallback;

		GLFW.glfwSetKeyCallback(handle, (_handle, key, scancode, action, modifiers) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Key: key=" + key + ", scancode=" + scancode + ", action=" + action + ", modifiers=" + modifiers);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.originalKeyCallback.invoke(handle, key, scancode, action, modifiers);
			}
		});
		GLFW.glfwSetCharModsCallback(handle, (_handle, codePoint, modifiers) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Char: codepoint=" + codePoint +", modifiers=" + modifiers);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.originalCharModsCallback.invoke(handle, codePoint, modifiers);
			}
		});

		System.out.println("TeleMC keyboard hooks installed.");

		info.cancel();
	}

    @Inject(at = @At("HEAD"), method = "setMouseCallbacks", cancellable = true)
	private static void setMouseCallbacks(long handle, GLFWCursorPosCallbackI cursorPosCallback, GLFWMouseButtonCallbackI mouseButtonCallback, GLFWScrollCallbackI scrollCallback, GLFWDropCallbackI dropCallback, CallbackInfo info) {
		TeleMCInputManager inputManager = TeleMCInputManager.getInstance();

		inputManager.originalWindow = handle;

		inputManager.originalCursorPosCallback = cursorPosCallback;
		inputManager.originalMouseButtonCallback = mouseButtonCallback;
		inputManager.originalScrollCallback = scrollCallback;
		inputManager.originalDropCallback = dropCallback;

		GLFW.glfwSetCursorPosCallback(handle, (_handle, x, y) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Mouse: x=" + x + ", y=" + y);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.lastCursorX = x;
				inputManager.lastCursorY = y;
	
				inputManager.originalCursorPosCallback.invoke(handle, x, y);
			}
		});
		GLFW.glfwSetMouseButtonCallback(handle, (_handle, button, action, modifiers) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Mouse button: button=" + button + ", action=" + action + ", modifiers=" + modifiers);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.originalMouseButtonCallback.invoke(handle, button, action, modifiers);
			}
		});
		GLFW.glfwSetScrollCallback(handle, (_handle, offsetX, offsetY) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Mouse scroll: offsetX=" + offsetX + ", offsetY=" + offsetY);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.originalScrollCallback.invoke(handle, offsetX, offsetY);
			}
		});
		GLFW.glfwSetDropCallback(handle, (_handle, count, names) -> {
			if (inputManager.logDeviceInput) {
				System.out.println("Mouse drop: count=" + count + ", names=" + names);
			}
			if (inputManager.allowDeviceInput) {
				inputManager.originalDropCallback.invoke(handle, count, names);
			}
		});

		System.out.println("TeleMC mouse hooks installed.");

		info.cancel();
	}

    @Inject(at = @At("HEAD"), method = "setCursorParameters", cancellable = true)
	private static void setCursorParameters(long handler, int inputModeValue, double x, double y, CallbackInfo info) {
		TeleMCInputManager inputManager = TeleMCInputManager.getInstance();

		// GLFW_CURSOR_NORMAL   = 0x34001 = 212993
		// GLFW_CURSOR_DISABLED = 0x34003 = 212995
		// GLFW_CURSOR_CAPTURED = 0x34004 = 212996 (not used)
		if (inputManager.logDeviceInput) {
			System.out.println("setCursorParameters: inputModeValue=" + inputModeValue + ", x=" + x + ", y=" + y);
		}
		if (inputManager.allowDeviceInput) {
			GLFW.glfwSetCursorPos(handler, x, y);
			GLFW.glfwSetInputMode(handler, GLFW.GLFW_CURSOR, inputModeValue);
		}
		
		info.cancel();
	}

    @Inject(at = @At("HEAD"), method = "isRawMouseMotionSupported", cancellable = true)
	private static void isRawMouseMotionSupported(CallbackInfoReturnable<Boolean> info) {
		// Don't advertise raw mouse motion support.
		info.setReturnValue(false);
	}

    @Inject(at = @At("HEAD"), method = "setRawMouseMotionMode", cancellable = true)
	private static void setRawMouseMotionMode(CallbackInfo info) {
		// Ignore raw mouse motion mode requests. Shouldn't be called given above, but just in case.
		info.cancel();
	}
}
