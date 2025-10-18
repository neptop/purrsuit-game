package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Pathfinder {
    private final WorldGrid grid;
    private final int width;
    private final int height;
    private final float [][] dist;
    private final Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

    public Pathfinder(WorldGrid grid) {
        this.grid = grid;
        this.width = grid.getWidth();
        this.height = grid.getHeight();
        this.dist = new float[width][height];
    }

    private static class Node implements Comparable<Node> {
        public final int x;
        public final int y;
        public final float cost;
        public Node(int x, int y, float cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
        @Override
        public int compareTo(Node other) {
            return Float.compare(this.cost, other.cost);
        }
    }

    public float getCost(Cell c) {
        return dist[c.x()][c.y()];
    }

    public void computeDist(Cell targetCell, HeatField heat){
        for (int x = 0; x < width; x++) {
            Arrays.fill(dist[x], Float.POSITIVE_INFINITY);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>();
        dist[targetCell.x()][targetCell.y()] = 0f;
        pq.add(new Node(targetCell.x(), targetCell.y(), 0f));

        while (!pq.isEmpty()){
            Node n = pq.poll();
            if (n.cost > dist[n.x][n.y]) continue; // already found a better path

            for (Direction dir : dirs) {
                int nx = n.x + dir.dx;
                int ny = n.y + dir.dy;
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue; // out of bounds
                if (grid.isWall(new Cell(nx, ny))) continue; // wall

                float newCost = n.cost + 1f + heat.getHeat(nx, ny); // base cost 1 + heat penalty
                if (newCost < dist[nx][ny]) {
                    dist[nx][ny] = newCost;
                    pq.add(new Node(nx, ny, newCost));
                }
            }
        }
    }

    public Direction bestStep(Cell fromCell){
        float best = Float.POSITIVE_INFINITY;
        Direction bestDir = null;
        for (Direction dir : dirs) {
            int nx = fromCell.x() + dir.dx;
            int ny = fromCell.y() + dir.dy;
            if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue; // out of bounds
            if (grid.isWall(new Cell(nx, ny))) continue; // wall
            if (dist[nx][ny] < best) {
                best = dist[nx][ny];
                bestDir = dir;
            }
        }
        return bestDir; // may be null if no valid steps
    }
}
