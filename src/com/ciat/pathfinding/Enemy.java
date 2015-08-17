package com.ciat.pathfinding;

import java.util.LinkedList;
import java.util.List;

import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;
import com.osreboot.ridhvl.tile.HvlLayeredTileMap;
import com.osreboot.ridhvl.tile.entity.HvlEntity;

public class Enemy extends HvlEntity {

	public static final float enemySize = 16.0f, enemySpeed = 64.0f;

	public static final boolean debugDraw = true;

	private static List<Enemy> enemies;

	public static void regenPaths(HvlLayeredTileMap map) {
		for (Enemy enemy : enemies) {
			enemy.regenPath(map);
		}
	}

	private int currentNode = 0;
	private List<Step> path;

	static {
		enemies = new LinkedList<>();
	}

	public Enemy(String inArg, float xArg, float yArg, HvlLayeredTileMap parentArg) {
		super(inArg, xArg, yArg, parentArg);
		enemies.add(this);
	}

	@Override
	public void update(float delta) {
		if (path == null || path.isEmpty() || currentNode >= path.size())
			return;

		HvlCoord enemyM = new HvlCoord(getParent().toWorldX(path.get(currentNode).getX()) + (getParent().getTileWidth() / 2) - getX(), getParent().toWorldY(
				path.get(currentNode).getY())
				+ (getParent().getTileHeight() / 2) - getY());
		enemyM.normalize().fixNaN().mult(delta).mult(enemySpeed);
		Main.handleCollision(getX(), getY(), enemyM, enemySize, getParent());
		setX(getX() + enemyM.x);
		setY(getY() + enemyM.y);

		if (new HvlCoord(getParent().toWorldX(path.get(currentNode).getX()) + (getParent().getTileWidth() / 2) - getX(), getParent().toWorldY(
				path.get(currentNode).getY())
				+ (getParent().getTileHeight() / 2) - getY()).length() < delta * enemySpeed) {
			currentNode++;
		}
	}

	@Override
	public void draw(float delta) {
		HvlPainter2D.hvlDrawQuad(getX() - enemySize / 2, getY() - enemySize / 2, enemySize, enemySize, Color.red);

		if (debugDraw) {
			if (path != null && path.size() > 0) {
				for (int i = currentNode; i < path.size() - 1; i++) {
					Main.drawPathLine(path.get(i).getX(), path.get(i).getY(), path.get(i + 1).getX(), path.get(i + 1).getY(), getParent());
				}
			}
		}
	}

	public void regenPath(HvlLayeredTileMap map) {
		path = new Pathfinder(map, 64, true).findPath(map.toTileX(getX()), map.toTileY(getY()), map.toTileX(Main.playerX), map.toTileY(Main.playerY));
		if (path == null)
			return;
		path.remove(0);

		currentNode = 0;
	}
}
