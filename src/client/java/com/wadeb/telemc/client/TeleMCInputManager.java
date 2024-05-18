package com.wadeb.telemc.client;

import org.lwjgl.glfw.*;

public class TeleMCInputManager {
	private static TeleMCInputManager instance;

	public boolean allowDeviceInput = true;
	public boolean logDeviceInput = false;
	public boolean logFakeInput = false;
	
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

	public void fakeChar(int codepoint) {
		if (logFakeInput) {
			System.out.println("FakeChar: codepoint=" + codepoint);
		}
		originalCharModsCallback.invoke(originalWindow, codepoint, 0);
	}

    public void fakeKeyDown(int key) {
        int scancode = GLFW.glfwGetKeyScancode(key);
		if (logFakeInput) {
			System.out.println("FakeKey: key=" + key + ", scancode=" + scancode + ", action=1, modifiers=0");
		}
		originalKeyCallback.invoke(originalWindow, key, scancode, 1/*GLFW_PRESS*/, 0);
    }

    public void fakeKeyUp(int key) {
		int scancode = GLFW.glfwGetKeyScancode(key);
		if (logFakeInput) {
			System.out.println("FakeKey: key=" + key + ", scancode=" + scancode + ", action=0, modifiers=0");
		}
		originalKeyCallback.invoke(originalWindow, key, scancode, 0/*GLFW_RELEASE*/, 0);
    }

	public void fakeMouseMove(double dx, double dy) {
		lastCursorX += dx;
		lastCursorY += dy;
		if (logFakeInput) {
			System.out.println("FakeMouseMove: dx=" + dx + ", dy=" + dy + ", lastCursorX=" + lastCursorX + ", lastCursorY" + lastCursorY);
		}
		originalCursorPosCallback.invoke(originalWindow, lastCursorX, lastCursorY);
	}

	public void fakeMouseMoveTo(double x, double y) {
		lastCursorX = x;
		lastCursorY = y;
		if (logFakeInput) {
			System.out.println("FakeMouseMoveTo: x=" + x + ", y" + y);
		}
		originalCursorPosCallback.invoke(originalWindow, lastCursorX, lastCursorY);
	}

	public void fakeMouseDown(int button) {
		if (logFakeInput) {
			System.out.println("FakeMouseMoveDown: button=" + button);
		}
		originalMouseButtonCallback.invoke(originalWindow, button, 1/*GLFW_PRESS*/, 0);
	}

    public void fakeMouseUp(int button) {
		if (logFakeInput) {
			System.out.println("FakeMouseMoveUp: button=" + button);
		}
		originalMouseButtonCallback.invoke(originalWindow, button, 0/*GLFW_RELEASE*/, 0);
	}

}
