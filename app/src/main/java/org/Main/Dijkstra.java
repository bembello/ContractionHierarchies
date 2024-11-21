package org.Main;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Dijkstra {
    public static QueryResult dijkstra(Graph graph, long source, long target) {
        Map<Long, List<Edge>> adjList = graph.getAdjacencyList();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(a -> a.distance));
        Map<Long, Long> distances = new HashMap<>();
        long relaxedEdges = 0;
    
        // Initialize distances
        for (long v : adjList.keySet()) {
            distances.put(v, Long.MAX_VALUE);
        }
        distances.put(source, 0L);
    
        // Add source node to the priority queue
        queue.add(new Node(source, 0L));
    
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            long u = current.vertex;
            long dist = current.distance;
    
            // Skip processing if the node is already processed (relaxed)
            if (dist > distances.get(u)) continue;
    
            // Stop early if we reach the target
            if (u == target) {
                return new QueryResult(dist, relaxedEdges);
            }
    
            // Get neighbors of the current vertex
            List<Edge> neighbors = adjList.get(u);
            if (neighbors == null) continue; // Skip if no neighbors
    
            for (Edge edge : neighbors) {
                long v = edge.getTo();
                long newDist = dist + edge.getCost();
    
                // Relax the edge if a shorter path is found
                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    queue.add(new Node(v, newDist));
                    relaxedEdges++; // Count the relaxation only when the distance is updated
                }
            }
        }
    
        return new QueryResult(-1, relaxedEdges); // No path found
    }
    
    
    
    /*public static void main(String[] args) {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addVertex(2, 2, 2);
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 4);
        graph.addEdge(0, 2, 10);

        QueryResult result = Dijkstra.dijkstra(graph, 0, 2);
        System.out.println(result.getRelaxedEdges());
    } */
    
}