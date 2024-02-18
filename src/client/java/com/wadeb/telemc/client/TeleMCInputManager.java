package com.wadeb.telemc.client;

import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class TeleMCInputManager {
	private static TeleMCInputManager instance;

	public boolean allowDeviceInput = true;
	public boolean logDeviceInput = false;
	
	public long originalWindow;
	
	public GLFWKeyCallbackI originalKeyCallback;
	public GLFWCharModsCallbackI originalCharModsCallback;
	
	public GLFWCursorPosCallbackI originalCursorPosCallback;
	public GLFWMouseButtonCallbackI originalMouseButtonCallback;
	public GLFWScrollCallbackI originalScrollCallback;
	public GLFWDropCallbackI originalDropCallback;
	
	public double lastCursorX;
	public double lastCursorY;

	private TeleMCInputManager() {
	}

	public static TeleMCInputManager getInstance() {
		if (instance == null) {
			instance = new TeleMCInputManager();
		}
		return instance;
	}

    public void fakeKeyDown(int key) {
        originalKeyCallback.invoke(originalWindow, key, 0, 1/*GLFW_PRESS*/, 0);
    }

    public void fakeKeyUp(int key) {
        originalKeyCallback.invoke(originalWindow, key, 0, 0/*GLFW_RELEASE*/, 0);
    }

	public void fakeMouseMove(double dx, double dy) {
		lastCursorX += dx;
		lastCursorY += dy;

		originalCursorPosCallback.invoke(originalWindow, lastCursorX, lastCursorY);
	}

	public void fakeMouseMoveTo(double x, double y) {
		lastCursorX = x;
		lastCursorY = y;

		originalCursorPosCallback.invoke(originalWindow, lastCursorX, lastCursorY);
	}

	public void fakeMouseDown(int button) {
		originalMouseButtonCallback.invoke(originalWindow, button, 1/*GLFW_PRESS*/, 0);
	}

    public void fakeMouseUp(int button) {
		originalMouseButtonCallback.invoke(originalWindow, button, 0/*GLFW_RELEASE*/, 0);
	}

}
