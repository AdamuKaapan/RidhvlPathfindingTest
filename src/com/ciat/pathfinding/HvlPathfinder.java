package com.ciat.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.tile.HvlLayeredTileMap;

public class HvlPathfinder {

	private class Node implements Comparable<Node> {
		private int x;
		private int y;
		private float cost;
		private Node parent;
		private float heuristic;
		private int depth;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void setParent(Node parent) {
			depth = parent.depth + 1;
			this.parent = parent;
		}

		public int compareTo(Node other) {
			float f = heuristic + cost;
			float of = other.heuristic + other.cost;

			if (f < of) {
				return -1;
			} else if (f > of) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public class HvlPathfinderStep {
		private int x;
		private int y;

		public HvlPathfinderStep(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	/**
	 * Dictates the restrictions on how a pathfinder can find a path.
	 */
	public static enum HvlPathfinderRestriction {
		/**
		 * Only moves horizontally and vertically
		 */
		NO_DIAGONAL,
		/**
		 * Moves diagonally (has the chance to cut across the corner of tiles)
		 */
		DIAGONAL,
		/**
		 * Moves diagonally and prevents cuting across the corner of tiles.
		 */
		DIAGONAL_NO_CUTTING
	}

	private ArrayList<Node> closed = new ArrayList<>();
	private PriorityQueue<Node> open = new PriorityQueue<>();

	private HvlLayeredTileMap map;

	private HvlPathfinderRestriction restriction;

	private int maxSearchDistance;

	private Node[][] nodes;

	private int searchLayer;

	public HvlPathfinder(HvlLayeredTileMap mapArg, int searchLayerArg, HvlPathfinderRestriction restrictionArg, int maxSearchDistanceArg) {
		this.map = mapArg;
		this.searchLayer = searchLayerArg;
		this.restriction = restrictionArg;
		this.maxSearchDistance = maxSearchDistanceArg;

		nodes = new Node[mapArg.getLayer(searchLayerArg).getMapWidth()][mapArg.getLayer(searchLayerArg).getMapHeight()];
		for (int x = 0; x < mapArg.getLayer(searchLayerArg).getMapWidth(); x++) {
			for (int y = 0; y < mapArg.getLayer(searchLayerArg).getMapHeight(); y++) {
				nodes[x][y] = new Node(x, y);
			}
		}
	}

	public List<HvlPathfinderStep> findPath(int startX, int startY, int targetX, int targetY) {
		// Easy check to stop right away if the target is in a wall
		if (map.isTileInLocation(targetX, targetY, searchLayer))
			return null;

		// Initial setup: all costs/depths = 0, open and closed are empty.
		nodes[startX][startY].cost = 0;
		nodes[startX][startY].depth = 0;
		closed.clear();
		open.clear();

		// Add starting tile to open list (because it's where we're starting).
		open.add(nodes[startX][startY]);

		// The target tile doesn't have a parent yet. That's what we're trying
		// to find.
		nodes[targetX][targetY].parent = null;

		// How far we've gotten into searching
		int currentDepth = 0;

		// While we have tiles to search with and we haven't gone too far
		while (currentDepth < maxSearchDistance && !open.isEmpty()) {
			// Get the next node to search
			Node current = open.poll();

			// We've found the target!
			if (current == nodes[targetX][targetY])
				break;

			// This node is now for sure on the path
			closed.add(current);

			// Loop through the tiles around the current one
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					// Don't check the current tile...
					if (x == 0 && y == 0)
						continue;

					// Check for invalid diagonal movement
					// (if it's not diagonal or diagonal no-cutting)
					if (restriction == HvlPathfinderRestriction.NO_DIAGONAL && x != 0 && y != 0)
						continue;

					int xp = x + current.x;
					int yp = y + current.y;

					// Don't let us cut corners TOO close (can't clip through
					// corner of block)
					if (restriction == HvlPathfinderRestriction.DIAGONAL_NO_CUTTING) {
						if (x != 0 && y != 0) {
							if (!isValidLocation(current.x, current.y + 1) || !isValidLocation(current.x, current.y - 1)
									|| !isValidLocation(current.x - 1, current.y) || !isValidLocation(current.x + 1, current.y)) {
								continue;
							}
						}
					}

					// If we can actually go to this tile...
					if (isValidLocation(xp, yp)) {
						// Get the cost to move to the next tile (this could
						// account for different terrain if we wanted it to)
						float nextStepCost = current.cost + ((xp == 0 || yp == 0) ? 10 : 14);
						Node neighbor = nodes[xp][yp];

						// If this has a smaller cost than the neighbor does,
						// kick them out and use this one instead
						if (nextStepCost < neighbor.cost) {
							open.remove(neighbor);
							closed.remove(neighbor);
						}

						// Add the neighbor to the open list if it's not there
						// already
						if (!open.contains(neighbor) && !closed.contains(neighbor)) {
							neighbor.cost = nextStepCost;
							neighbor.heuristic = HvlMath.distance(xp, yp, targetX, targetY);
							neighbor.setParent(current);
							currentDepth = Math.max(currentDepth, neighbor.depth);
							open.add(neighbor);
						}
					}
				}
			}
		}

		// We couldn't find a path.
		if (nodes[targetX][targetY].parent == null)
			return null;

		List<HvlPathfinderStep> tr = new ArrayList<>();

		// Backtrack from the end to the beginning by following the parents
		Node target = nodes[targetX][targetY];
		while (target != nodes[startX][startY]) {
			tr.add(0, new HvlPathfinderStep(target.x, target.y));
			target = target.parent;
		}
		// Add the start tile to the path.
		tr.add(0, new HvlPathfinderStep(target.x, target.y));

		return tr;
	}

	private boolean isValidLocation(int x, int y) {
		// Out of bounds is invalid, duh
		if (x < 0 || y < 0 || x >= map.getLayer(searchLayer).getMapWidth() || y >= map.getLayer(searchLayer).getMapHeight())
			return false;

		return !map.isTileInLocation(x, y, searchLayer);
	}
}
