package com.ciat.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.tile.HvlLayeredTileMap;

public class Pathfinder {

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

	private ArrayList<Node> closed = new ArrayList<>();
	private PriorityQueue<Node> open = new PriorityQueue<>();

	private HvlLayeredTileMap map;

	private int maxSearchDistance;

	private Node[][] nodes;

	private boolean allowDiagMovement;

	public Pathfinder(HvlLayeredTileMap map, int maxSearchDistance, boolean allowDiagMovement) {
		this.map = map;
		this.maxSearchDistance = maxSearchDistance;
		this.allowDiagMovement = allowDiagMovement;

		nodes = new Node[map.getLayer(1).getMapWidth()][map.getLayer(1).getMapHeight()];
		for (int x = 0; x < map.getLayer(1).getMapWidth(); x++) {
			for (int y = 0; y < map.getLayer(1).getMapHeight(); y++) {
				nodes[x][y] = new Node(x, y);
			}
		}
	}

	public List<Step> findPath(int startX, int startY, int targetX, int targetY) {
		// Easy check to stop right away if the target is in a wall
		if (map.isTileInLocation(targetX, targetY, 1))
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
					if (!allowDiagMovement && x != 0 && y != 0)
						continue;

					int xp = x + current.x;
					int yp = y + current.y;

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

		List<Step> tr = new ArrayList<>();

		// Backtrack from the end to the beginning by following the parents
		Node target = nodes[targetX][targetY];
		while (target != nodes[startX][startY]) {
			tr.add(0, new Step(target.x, target.y));
			target = target.parent;
		}
		// Add the start tile to the path.
		tr.add(0, new Step(target.x, target.y));

		return tr;
	}

	private boolean isValidLocation(int x, int y) {
		// Out of bounds = invalid, duh
		if (x < 0 || y < 0 || x >= map.getLayer(1).getMapWidth() || y >= map.getLayer(1).getMapHeight())
			return false;

		return map.getLayer(1).getTile(x, y) == null; // No tile = valid
	}
}
