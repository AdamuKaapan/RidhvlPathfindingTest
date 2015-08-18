package com.ciat.pathfinding;

import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.display.collection.HvlDisplayModeDefault;
import com.osreboot.ridhvl.input.HvlInputSeriesAction;
import com.osreboot.ridhvl.painter.HvlCamera;
import com.osreboot.ridhvl.painter.HvlCamera.HvlCameraAlignment;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;
import com.osreboot.ridhvl.template.HvlTemplateInteg2D;
import com.osreboot.ridhvl.tile.HvlLayeredTileMap;

public class Main extends HvlTemplateInteg2D {

	HvlLayeredTileMap map;

	final boolean debugDraw = true;
	final float playerSpeed = 128.0f, playerSize = 16.0f;

	public static float playerX, playerY;

	int previousPlayerTileX, previousPlayerTileY, currentPlayerTileX, currentPlayerTileY;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		super(60, 800, 600, "Pathfinding Test - Creeper in a TARDIS Programming", new HvlDisplayModeDefault());
	}

	@Override
	public void initialize() {
		getTextureLoader().loadResource("Tilesheet");

		map = HvlLayeredTileMap.load("SavedMap", true, 0, 0, 32, 32, getTexture(0));
		playerX = 11 * map.getTileWidth() + map.getTileWidth() / 2;
		playerY = 11 * map.getTileHeight() + map.getTileWidth() / 2;
	}

	@Override
	public void update(float delta) {
		HvlCoord playerM = new HvlCoord(HvlInputSeriesAction.HORIZONTAL.getCurrentOutput(), HvlInputSeriesAction.VERTICAL.getCurrentOutput());
		playerM.normalize().fixNaN().mult(delta).mult(playerSpeed);
		handleCollision(playerX, playerY, playerM, playerSize, map);

		playerX += playerM.x;
		playerY += playerM.y;

		playerX = Math.max(0, Math.min(map.getTileWidth() * map.getLayer(1).getMapWidth() - map.getTileWidth(), playerX));
		playerY = Math.max(0, Math.min(map.getTileHeight() * map.getLayer(1).getMapHeight(), playerY));

		HvlCamera.setX(playerX + (map.getTileWidth() / 2));
		HvlCamera.setY(playerY + (map.getTileHeight() / 2));
		HvlCamera.setAlignment(HvlCameraAlignment.CENTER);

		previousPlayerTileX = currentPlayerTileX;
		previousPlayerTileY = currentPlayerTileY;
		currentPlayerTileX = map.toTileX(playerX);
		currentPlayerTileY = map.toTileY(playerY);

		if (currentPlayerTileX != previousPlayerTileX || currentPlayerTileY != previousPlayerTileY) {
			Enemy.regenPaths(map);
		}

		map.draw(delta);

		HvlPainter2D.hvlDrawQuad(playerX - playerSize / 2, playerY - playerSize / 2, playerSize, playerSize, Color.green);
	}

	public static void drawPathLine(int sX, int sY, int tX, int tY, HvlLayeredTileMap map) {
		HvlPainter2D.hvlDrawLine(sX * map.getTileWidth() + (map.getTileWidth() / 2), sY * map.getTileHeight() + (map.getTileHeight() / 2),
				tX * map.getTileWidth() + (map.getTileWidth() / 2), tY * map.getTileHeight() + (map.getTileHeight() / 2), Color.blue, 4.0f);
	}

	public static void handleCollision(float x, float y, HvlCoord motion, float size, HvlLayeredTileMap map) {
		if (map.isTileInLocation(x + motion.x + size / 2, y - size / 2, 1))
			motion.x = Math.min(0, motion.x);

		if (map.isTileInLocation(x + motion.x + size / 2, y + size / 2, 1))
			motion.x = Math.min(0, motion.x);

		if (map.isTileInLocation(x + motion.x - size / 2, y - size / 2, 1))
			motion.x = Math.max(0, motion.x);

		if (map.isTileInLocation(x + motion.x - size / 2, y + size / 2, 1))
			motion.x = Math.max(0, motion.x);

		if (map.isTileInLocation(x - size / 2, y + motion.y + size / 2, 1))
			motion.y = Math.min(0, motion.y);

		if (map.isTileInLocation(x + size / 2, y + motion.y + size / 2, 1))
			motion.y = Math.min(0, motion.y);

		if (map.isTileInLocation(x - size / 2, y + motion.y - size / 2, 1))
			motion.y = Math.min(0, motion.y);

		if (map.isTileInLocation(x + size / 2, y + motion.y - size / 2, 1))
			motion.y = Math.max(0, motion.y);
	}
}
