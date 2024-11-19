package org.Main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ContractionHierarchy {
private Graph graph;
private List<Vertex> vertexOrder;  // The order in which vertices will be contracted
private Set<Vertex> contractedVertices;
private Map<Vertex, Integer> rankMap; // To store the rank of each vertex

    public ContractionHierarchy(Graph graph) {
        this.graph = graph;
        this.vertexOrder = new ArrayList<>();
        this.contractedVertices = new HashSet<>();
        this.rankMap = new HashMap<>();
    }

    // Preprocesses the graph (contraction phase)
    public void preprocess() {
        // Step 1: Initialize the vertex order based on the edge difference heuristic
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(this::getEdgeDifference));
        
        for (Vertex vertex : graph.getVertices().values()) {
            priorityQueue.add(vertex);
        }

        // Step 2: Rank the nodes based on edge difference heuristic
        while (!priorityQueue.isEmpty()) {
            Vertex v = priorityQueue.poll();
            if (contractedVertices.contains(v)) continue; // Skip contracted nodes
            vertexOrder.add(v);
            contractedVertices.add(v);
            rankMap.put(v, vertexOrder.size());

            // Step 3: Contract vertex v
            contractVertex(v);

            // Update the priority queue based on the new state of the graph
            updatePriorityQueue(priorityQueue);
        }

        System.out.println("Preprocessing complete. Total vertices contracted: " + vertexOrder.size());
    }

    // Get the edge difference for a vertex (used for edge difference heuristic)
    private int getEdgeDifference(Vertex v) {
        Set<Edge> activeEdges = v.getActiveEdges(); // Get active edges (not contracted ones)
        return activeEdges.size();
    }

    // Contract a vertex: remove it from the graph and add shortcut edges
    private void contractVertex(Vertex v) {
        Set<Edge> neighbors = v.getEdges();
        List<Vertex> neighborList = new ArrayList<>();

        // Step 1: Collect neighbors of the vertex to be contracted
        for (Edge edge : neighbors) {
            if (!contractedVertices.contains(graph.getVertexById(edge.getTo()))) {
                neighborList.add(graph.getVertexById(edge.getTo()));
            }
        }

        // Step 2: Add shortcut edges between neighbors if applicable
        for (int i = 0; i < neighborList.size(); i++) {
            Vertex u = neighborList.get(i);
            for (int j = i + 1; j < neighborList.size(); j++) {
                Vertex w = neighborList.get(j);
                
                // If the shortest path between u and w goes through v, add a shortcut edge
                if (isUniqueShortestPath(u, v, w)) {
                    int shortcutCost = getCostBetween(u, v) + getCostBetween(v, w);
                    graph.addEdge(u.getId(), w.getId(), shortcutCost);  // Add shortcut edge
                }
            }
        }

        // Step 3: Mark the vertex as contracted (lazy update approach)
        contractedVertices.add(v);
    }

    // Check if the shortest path between u, v, and w is unique
    private boolean isUniqueShortestPath(Vertex u, Vertex v, Vertex w) {
        // Implement Dijkstra or another shortest path algorithm to check if the path (u -> v -> w) is unique
        // Here, for simplicity, we assume the path is unique
        return true;  // This needs to be replaced with actual logic based on your implementation
    }

    // Update the priority queue after each contraction (lazy update)
    private void updatePriorityQueue(PriorityQueue<Vertex> priorityQueue) {
        // Re-calculate the priority queue based on the new state of the graph
        for (Vertex vertex : graph.getVertices().values()) {
            if (!contractedVertices.contains(vertex)) {
                priorityQueue.add(vertex);
            }
        }
    }

    // Get cost between two vertices (used to calculate shortcut edge cost)
    private int getCostBetween(Vertex u, Vertex v) {
        // Implement logic to get the cost of an edge between u and v
        return u.getCostTo(v); // Placeholder, adjust based on your graph structure
    }

    // Run bidirectional Dijkstra to find the shortest path between source and target
    public QueryResult bidirectionalDijkstra(int source, int target) {
        // Implement the bidirectional Dijkstra algorithm here
        // For now, let's assume it returns a placeholder result
        return new QueryResult(100, 50);  // Placeholder, replace with actual implementation
    }
}