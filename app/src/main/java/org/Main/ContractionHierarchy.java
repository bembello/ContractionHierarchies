package org.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ContractionHierarchy {
    private Graph graph;
    private List<Vertex> vertexOrder;  // The order in which vertices will be contracted
    private Set<Vertex> contractedVertices;
    private Map<Vertex, Integer> rankMap; // To store the rank of each vertex
    private List<Edge> allEdges; // To track all edges including shortcut edges
    
    public ContractionHierarchy(Graph graph) {
        this.graph = graph;
        this.vertexOrder = new ArrayList<>();
        this.contractedVertices = new HashSet<>();
        this.rankMap = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }

    // Preprocesses the graph (contraction phase)
    public void preprocess() {
        long startTime = System.nanoTime();  // Start time for preprocessing
        
        // Step 1: Initialize the vertex order based on the edge difference heuristic
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(this::getEdgeDifference));
        
        for (Vertex vertex : graph.getVertices().values()) {
            priorityQueue.add(vertex);
        }

        System.out.println("Step 1 done");

        // Step 2: Rank the nodes based on edge difference heuristic
        while (!priorityQueue.isEmpty()) {
            System.out.println(priorityQueue.size());
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

        System.out.println("Step 2 done");

        long endTime = System.nanoTime();  // End time for preprocessing
        System.out.println("Preprocessing complete. Total vertices contracted: " + vertexOrder.size());
        System.out.println("Preprocessing time: " + (endTime - startTime) / 1_000_000 + " ms");
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
                    Edge shortcutEdge = new Edge(u.getId(), w.getId(), shortcutCost);
                    graph.addEdge(u.getId(), w.getId(), shortcutCost);  // Add shortcut edge
                    allEdges.add(shortcutEdge); // Store the shortcut edge
                    System.out.println("done");
                }
            }
        }

        // Step 3: Mark the vertex as contracted (lazy update approach)
        contractedVertices.add(v);
    }

    // Check if the shortest path between u, v, and w is unique
    private boolean isUniqueShortestPath(Vertex u, Vertex v, Vertex w) {
        // Run Dijkstra on the sub-paths (u -> v) and (v -> w)
        QueryResult uvResult = Dijkstra.dijkstra(graph, u.getId(), v.getId());
        QueryResult vwResult = Dijkstra.dijkstra(graph, v.getId(), w.getId());
        
        if (uvResult.getShortestPath() == -1 || vwResult.getShortestPath() == -1) {
            return false;  // No path exists between u and v, or v and w
        }
        
        // Calculate the expected total distance for the u -> v -> w path
        long expectedDistance = uvResult.getShortestPath() + vwResult.getShortestPath();
        
        // Now, check if there is any shorter path between u and w (directly or via any other vertex)
        QueryResult uwResult = Dijkstra.dijkstra(graph, u.getId(), w.getId());
        
        // If the distance from u -> w is strictly less than the combined u -> v -> w, then the path is not unique
        return uwResult.getShortestPath() >= expectedDistance;
    }

    // Update the priority queue after each contraction (lazy update)
    private void updatePriorityQueue(PriorityQueue<Vertex> priorityQueue) {
        // Re-calculate the priority queue based on the new state of the graph
        priorityQueue.clear(); 
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

    // Export the augmented graph to a text file
    public void exportAugmentedGraph(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write the number of vertices and edges (original + shortcut edges)
            writer.write(graph.getVertices().size() + " " + (graph.getEdges().size() + allEdges.size()));
            writer.newLine();

            // Write the vertices with their rank
            for (Vertex vertex : graph.getVertices().values()) {
                int rank = rankMap.getOrDefault(vertex, -1); // Get the rank of the vertex
                writer.write(vertex.getId() + " " + rank);
                writer.newLine();
            }

            // Write the edges (including shortcut edges)
            for (Edge edge : graph.getEdges()) {
                writer.write(edge.getFrom() + " " + edge.getTo() + " " + edge.getCost() + " -1");
                writer.newLine();
            }

            // Write shortcut edges
            for (Edge shortcutEdge : allEdges) {
                writer.write(shortcutEdge.getFrom() + " " + shortcutEdge.getTo() + " " + shortcutEdge.getCost() + " " + shortcutEdge.getFrom());
                writer.newLine();
            }

            System.out.println("Augmented graph exported successfully.");
        } catch (IOException e) {
            System.err.println("Error writing augmented graph to file: " + e.getMessage());
        }
    }

    // Run bidirectional Dijkstra to find the shortest path between source and target
    public QueryResult bidirectionalDijkstra(int source, int target) {
        
        return BidirectionalDijkstra.bidirectionalDijkstra(graph, source, target);
    }
}
