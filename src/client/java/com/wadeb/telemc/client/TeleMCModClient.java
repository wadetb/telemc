package com.wadeb.telemc.client;

import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

class TeleMCWebSocketServer extends WebSocketServer {

	public TeleMCWebSocketServer(int port) {
		super(new InetSocketAddress(port));
		setReuseAddr(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println(
				"New websocket connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println(
				"Closed websocket connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Received message from client: " + message);
		if (message.startsWith("key_up")) {
			String[] parts = message.split(" ");
			int key = Integer.parseInt(parts[1]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeKeyUp(key);
		} else if (message.startsWith("key_down")) {
			String[] parts = message.split(" ");
			int key = Integer.parseInt(parts[1]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeKeyDown(key);
		} else if (message.startsWith("char")) {
			String[] parts = message.split(" ");
			int codepoint = Integer.parseInt(parts[1]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeChar(codepoint);
		} else if (message.startsWith("mouse_move_to")) {
			String[] parts = message.split(" ");
			double x = Double.parseDouble(parts[1]);
			double y = Double.parseDouble(parts[2]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeMouseMoveTo(x, y);
		} else if (message.startsWith("mouse_move")) {
			String[] parts = message.split(" ");
			double dx = Double.parseDouble(parts[1]);
			double dy = Double.parseDouble(parts[2]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeMouseMove(dx, dy);
		} else if (message.startsWith("mouse_down")) {
			String[] parts = message.split(" ");
			int button = Integer.parseInt(parts[1]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeMouseDown(button);
		} else if (message.startsWith("mouse_up")) {
			String[] parts = message.split(" ");
			int button = Integer.parseInt(parts[1]);
			TeleMCInputManager inputManager = TeleMCInputManager.getInstance();
			inputManager.fakeMouseUp(button);
		} else {
			System.out.println("Ignored message from client: " + message);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onStart() {
		System.out.println("TeleMC websocket server started.");
	}
}

public class TeleMCModClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("telemc-client");
	private static final int WEBSOCKET_PORT = 8025;

	private final Gson gson = new Gson();

	@Override
	public void onInitializeClient() {
		LOGGER.info("TeleMC starting websocket server.");

		TeleMCWebSocketServer server = new TeleMCWebSocketServer(WEBSOCKET_PORT);
		server.start();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Map<String, Object> data = new HashMap<>();
			if (client.player != null) {
				data.put("player", getPlayerData(client.player));
			}
			if (client.currentScreen != null) {
				data.put("screen", getScreenData(client.currentScreen));
			}

			String json = gson.toJson(data);
			server.broadcast(json);
		});
	}

	private static Map<String, Object> getScreenData(Screen screen) {
		Map<String, Object> screenData = new HashMap<>();
		screenData.put("type", screen.getClass().getName());
		screenData.put("title", screen.getTitle().getString());
		screenData.put("width", screen.width);
		screenData.put("height", screen.height);

		List<Map<String, Object>> selectables = new ArrayList<>();
		if (screen instanceof TeleMCScreenInterface) {
			for (Selectable selectable : ((TeleMCScreenInterface) screen).getSelectables()) {
				if (selectable instanceof TeleMCGetDataInterface) {
					selectables.add(((TeleMCGetDataInterface) selectable).getTeleMCData());
				} else {
					Map<String, Object> selectableData = new HashMap<>();
					selectableData.put("class", selectable.getClass().getName());
					selectables.add(selectableData);
				}
			}
		}
		screenData.put("selectables", selectables);

		if (screen instanceof ScreenHandlerProvider<?> screenHandlerProvider) {
			List<Map<String, Object>> slots = new ArrayList<>();
            for (Slot slot : screenHandlerProvider.getScreenHandler().slots) {
				Map<String, Object> slotData = new HashMap<>();
				slotData.put("class", slot.getClass().getName());
				slotData.put("index", slot.getIndex());
				slotData.put("x", slot.x);
				slotData.put("y", slot.y);
				ItemStack stack = slot.getStack();
				if (!stack.isEmpty()) {
					slotData.put("item", getItemData(stack));
				}
				slots.add(slotData);
			}
			screenData.put("slots", slots);
		}

		List<Map<String, Object>> children = new ArrayList<>();
		for (Element element : screen.children()) {
			Map<String, Object> elementData = new HashMap<>();
			if (element instanceof TeleMCGetDataInterface) {
				elementData = ((TeleMCGetDataInterface) element).getTeleMCData();
			} else {
				elementData.put("class", element.getClass().getName());
			}
			children.add(elementData);
		}
		screenData.put("children", children);

		if (screen instanceof TeleMCScreenInterface) {
			screenData.put("narration", ((TeleMCScreenInterface) screen).getNarratorText());
		}

		return screenData;
	}

	private static Map<String, Object> getItemData(ItemStack stack) {
		Map<String, Object> itemData = new HashMap<>();
		itemData.put("name", stack.getName().getString());
		itemData.put("count", stack.getCount());
		itemData.put("damage", stack.getDamage());
		return itemData;
	}

	private static Map<String, Object> getPlayerData(ClientPlayerEntity player) {
		Map<String, Object> playerData = new HashMap<>();

		ClientWorld world = player.clientWorld;
		if (world == null) {
			return playerData;
		}

		playerData.put("name", player.getGameProfile().getName());
		playerData.put("x", player.getX());
		playerData.put("y", player.getY());
		playerData.put("z", player.getZ());
		playerData.put("yaw", player.getYaw());
		playerData.put("pitch", player.getPitch());
		playerData.put("health", player.getHealth());
		playerData.put("hunger", player.getHungerManager().getFoodLevel());

		getPlayerInventoryData(player, playerData);
		getPlayerTargetedBlockData(player, world, playerData);
		getPlayerTargetedEntityData(player, world, playerData);

		// List<Map<String, Object>> nearbyEntities = world.getEntitiesByClass(
		// Entity.class,
		// new Box(player.getBlockPos()).expand(100),
		// e -> e != player).stream().map(entity -> {
		// Map<String, Object> entityData = new HashMap<>();
		// entityData.put("type", entity.getType().toString());
		// entityData.put("name", entity.getType().getTranslationKey());
		// entityData.put("x", entity.getX());
		// entityData.put("y", entity.getY());
		// entityData.put("z", entity.getZ());
		// return entityData;
		// }).collect(Collectors.toList());
		// playerData.put("entities", nearbyEntities);

		return playerData;
	}

	private static void getPlayerInventoryData(ClientPlayerEntity player, Map<String, Object> playerData) {
		Map<String, List<Map<String, Object>>> inventoryData = new HashMap<>();
		List<Map<String, Object>> mainInventoryData = new ArrayList<>();
		for (int i = 0; i < player.getInventory().main.size(); i++) {
			ItemStack stack = player.getInventory().main.get(i);
			mainInventoryData.add(getItemData(stack));
		}
		inventoryData.put("main", mainInventoryData);
		List<Map<String, Object>> armorInventoryData = new ArrayList<>();
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack stack = player.getInventory().armor.get(i);
			armorInventoryData.add(getItemData(stack));
		}
		inventoryData.put("armor", armorInventoryData);
		List<Map<String, Object>> offhandInventoryData = new ArrayList<>();
		ItemStack offhandStack = player.getInventory().offHand.get(0);
		offhandInventoryData.add(getItemData(offhandStack));
		inventoryData.put("offhand", offhandInventoryData);
		playerData.put("selectedSlot", player.getInventory().selectedSlot);
		playerData.put("inventory", inventoryData);
	}

	private static void getPlayerTargetedBlockData(ClientPlayerEntity player, ClientWorld world, Map<String, Object> playerData) {
		HitResult hitResult = player.raycast(30.0D, 0.0F, false);
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
			BlockState blockState = world.getBlockState(blockPos);
			Map<String, Object> targetedBlock = new HashMap<>();
			targetedBlock.put("id", blockState.getBlock().toString());
			targetedBlock.put("x", blockPos.getX());
			targetedBlock.put("y", blockPos.getY());
			targetedBlock.put("z", blockPos.getZ());

			for (Property<?> property : blockState.getProperties()) {
				Comparable<?> value = blockState.get(property);
				String propertyName = property.getName();
				String propertyValue = value.toString();
				targetedBlock.put(propertyName, propertyValue);
			}

			playerData.put("targetedBlock", targetedBlock);
		}
	}

	private static void getPlayerTargetedEntityData(ClientPlayerEntity player, ClientWorld world, Map<String, Object> playerData) {
		Vec3d rayFrom = player.getCameraPosVec(0.0f);
		Vec3d lookVector = player.getRotationVec(0.0f).multiply(50.0);
		Vec3d rayTo = rayFrom.add(lookVector);

		for (Entity entity : world.getEntities()) {
			if (entity == player) {
				continue; // Skip the player entity itself
			}

			Box entityAABB = entity.getBoundingBox().expand(0.1);
			Optional<Vec3d> intersection = entityAABB.raycast(rayFrom, rayTo);
			if (intersection.isPresent()) {
				Map<String, Object> entityData = new HashMap<>();
				entityData.put("id", entity.getType().toString());
				entityData.put("x", entity.getX());
				entityData.put("y", entity.getY());
				entityData.put("z", entity.getZ());
				playerData.put("targetedEntity", entityData);
			}
		}
	}

}
