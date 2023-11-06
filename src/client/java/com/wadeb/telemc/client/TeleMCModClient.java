package com.wadeb.telemc.client;

import java.io.BufferedReader;
import java.io.IOException;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

class MinecraftWebSocketServer extends WebSocketServer {

	public MinecraftWebSocketServer(int port) {
		super(new InetSocketAddress(port));
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Ignored message from client: " + message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");
	}
}

public class TeleMCModClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("telemc-client");
	private static final int WEBSOCKET_PORT = 8025;

	private final Gson gson = new Gson();

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hello TeleMC Client!");

		MinecraftWebSocketServer server = new MinecraftWebSocketServer(8025);
		server.start();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				ClientPlayerEntity player = client.player;
				String playerData = getPlayerData();
				server.broadcast(playerData);
			}
		});
	}

	private String getPlayerData() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		ClientWorld world = MinecraftClient.getInstance().world;

		if (player == null || world == null) {
			return "{}";
		}

		Map<String, Object> data = new HashMap<>();
		data.put("player", player.getGameProfile().getName());
		data.put("x", player.getX());
		data.put("y", player.getY());
		data.put("z", player.getZ());
		data.put("yaw", player.getYaw());
		data.put("pitch", player.getPitch());
		data.put("health", player.getHealth());
		data.put("hunger", player.getHungerManager().getFoodLevel());

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

			data.put("targetedBlock", targetedBlock);
		}

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
				data.put("targetedEntity", entityData);
			}
		}

//		List<Map<String, Object>> nearbyEntities = world.getEntitiesByClass(
//				Entity.class,
//				new Box(player.getBlockPos()).expand(100),
//				e -> e != player).stream().map(entity -> {
//					Map<String, Object> entityData = new HashMap<>();
//					entityData.put("type", entity.getType().toString());
//					entityData.put("name", entity.getType().getTranslationKey());
//					entityData.put("x", entity.getX());
//					entityData.put("y", entity.getY());
//					entityData.put("z", entity.getZ());
//					return entityData;
//				}).collect(Collectors.toList());
//		data.put("entities", nearbyEntities);

		return gson.toJson(data);
	}
}
