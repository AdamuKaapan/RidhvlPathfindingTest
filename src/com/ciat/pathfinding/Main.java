package com.ciat.pathfinding;

import java.util.List;

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
	List<Step> found;

	final boolean debugDraw = true;
	final float enemySpeed = 64.0f, playerSpeed = 128.0f;

	float enemyX, enemyY;
	float playerX, playerY;

	int previousPlayerTileX, previousPlayerTileY, currentPlayerTileX, currentPlayerTileY;

	int currentNode = 0;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		super(60, 800, 600, "Pathfinding Test - Creeper in a TARDIS Programming", new HvlDisplayModeDefault());
	}

	@Override
	public void initialize() {
		getTextureLoader().loadResource("Tilesheet");

		map = HvlLayeredTileMap.load("SavedMap", true, getTexture(0), 0, 0, 32, 32);
		enemyX = 6 * map.getTileWidth();
		enemyY = 5 * map.getTileHeight();
		playerX = 11 * map.getTileWidth();
		playerY = 11 * map.getTileHeight();

		regenPath();
	}

	@Override
	public void update(float delta) {
		HvlCoord playerM = new HvlCoord(HvlInputSeriesAction.HORIZONTAL.getCurrentOutput(), HvlInputSeriesAction.VERTICAL.getCurrentOutput());
		playerM.normalize().fixNaN().mult(delta).mult(playerSpeed);
		handleCollision(playerX, playerY, playerM);
				
		playerX += playerM.x;
		playerY += playerM.y;

		playerX = Math.max(0, Math.min(map.getTileWidth() * map.getLayer(1).getMapWidth() - map.getTileWidth(), playerX));
		playerY = Math.max(0, Math.min(map.getTileHeight() * map.getLayer(1).getMapHeight(), playerY));

		HvlCamera.setX(playerX + (map.getTileWidth() / 2));
		HvlCamera.setY(playerY + (map.getTileHeight() / 2));
		HvlCamera.setAlignment(HvlCameraAlignment.CENTER);

		previousPlayerTileX = currentPlayerTileX;
		previousPlayerTileY = currentPlayerTileY;
		currentPlayerTileX = getTileX(playerX);
		currentPlayerTileY = getTileY(playerY);

		if (currentPlayerTileX != previousPlayerTileX || currentPlayerTileY != previousPlayerTileY) {
			regenPath();
			currentNode = 0;
		}

		if (found != null && found.size() > 0 && currentNode < found.size()) {
			HvlCoord enemyM = new HvlCoord(map.toWorldX(found.get(currentNode).getX()) - enemyX, map.toWorldY(found.get(currentNode).getY()) - enemyY);
			enemyM.normalize().fixNaN().mult(delta).mult(enemySpeed);
			handleCollision(enemyX, enemyY, enemyM);
			enemyX += enemyM.x;
			enemyY += enemyM.y;

			if (new HvlCoord(map.toWorldX(found.get(currentNode).getX()) - enemyX, map.toWorldY(found.get(currentNode).getY()) - enemyY).length() < delta
					* enemySpeed) {
				currentNode++;
			}
		}

		map.draw(delta);

		if (debugDraw) {
			if (found != null && found.size() > 0) {
				for (int i = currentNode; i < found.size() - 1; i++) {
					drawPathLine(found.get(i).getX(), found.get(i).getY(), found.get(i + 1).getX(), found.get(i + 1).getY());
				}
			}
		}

		HvlPainter2D.hvlDrawQuad(enemyX, enemyY, map.getTileWidth(), map.getTileHeight(), Color.red);
		HvlPainter2D.hvlDrawQuad(playerX, playerY, map.getTileWidth(), map.getTileHeight(), Color.green);
	}

	private int getTileX(float x) {
		if (((x - map.getX()) / map.getTileWidth()) - (int) ((x - map.getX()) / map.getTileWidth()) < 0.5f)
			return (int) ((x - map.getX()) / map.getTileWidth());
		else
			return (int) ((x - map.getX()) / map.getTileWidth()) + 1;

	}

	private int getTileY(float y) {

		if (((y - map.getX()) / map.getTileHeight()) - (int) ((y - map.getX()) / map.getTileHeight()) < 0.5f)
			return (int) ((y - map.getX()) / map.getTileHeight());
		else
			return (int) ((y - map.getX()) / map.getTileHeight()) + 1;
	}

	private void drawPathLine(int sX, int sY, int tX, int tY) {
		HvlPainter2D.hvlDrawLine(sX * map.getTileWidth() + (map.getTileWidth() / 2), sY * map.getTileHeight() + (map.getTileHeight() / 2),
				tX * map.getTileWidth() + (map.getTileWidth() / 2), tY * map.getTileHeight() + (map.getTileHeight() / 2), Color.blue, 4.0f);
	}

	private void regenPath() {
		found = new Pathfinder(map, 64, false).findPath(getTileX(enemyX), getTileY(enemyY), getTileX(playerX), getTileY(playerY));
		if (found == null)
			return;
		found.remove(0);
	}

	private void handleCollision(float x, float y, HvlCoord motion)
	{
		if (map.isTileInLocation(x + motion.x + map.getTileWidth(), y, 1))
			motion.x = Math.min(0, motion.x);
		
		if (map.isTileInLocation(x + motion.x + map.getTileWidth(), y + map.getTileHeight(), 1))
			motion.x = Math.min(0, motion.x);
		
		if (map.isTileInLocation(x + motion.x, y, 1))
			motion.x = Math.max(0, motion.x);
		
		if (map.isTileInLocation(x + motion.x, y + map.getTileHeight(), 1))
			motion.x = Math.max(0, motion.x);
		
		if (map.isTileInLocation(x, y + motion.y + map.getTileHeight(), 1))
			motion.y = Math.min(0, motion.y);
		
		if (map.isTileInLocation(x + map.getTileWidth(), y + motion.y + map.getTileHeight(), 1))
			motion.y = Math.min(0, motion.y);
		
		if (map.isTileInLocation(x, y + motion.y, 1))
			motion.y = Math.min(0, motion.y);
		
		if (map.isTileInLocation(x + map.getTileWidth(), y + motion.y, 1))
			motion.y = Math.max(0, motion.y);
	}
}
