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
        Set<Long> visited = new HashSet<>();
        long relaxedEdges = 0;  // Track number of relaxed edges
        
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
    
            // If we reached the target, return the result
            if (u == target) return new QueryResult(dist, relaxedEdges);
            
            // Skip already visited nodes
            if (visited.contains(u)) continue;
            visited.add(u);
    
            // Get neighbors of the current vertex
            List<Edge> neighbors = adjList.get(u);
            if (neighbors == null) continue; // Skip if no neighbors
    
            for (Edge edge : neighbors) {
                long v = edge.getTo();
                long newDist = dist + edge.getCost();
    
                // Update the distance if a shorter path is found
                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    queue.add(new Node(v, newDist));
                    relaxedEdges++;  // Count the relaxed edge
                }
            }
        }
        return new QueryResult(-1, relaxedEdges); // No path found
    }
}