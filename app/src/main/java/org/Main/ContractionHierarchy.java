package org.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
    private List<Vertex> vertexOrder;
    private Set<Vertex> contractedVertices;
    private Map<Vertex, Integer> rankMap;
    private List<Edge> allEdges;
    private int lazyUpdateCount = 0;
    private int updateThreshold = 50;
    
    public ContractionHierarchy(Graph graph) {
        this.graph = graph;
        this.vertexOrder = new ArrayList<>();
        this.contractedVertices = new HashSet<>();
        this.rankMap = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }

    public void preprocess() {
        long startTime = System.nanoTime();

        // Priority Queue to decide which node to contract next based on updated priority formula
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(this::getNodePriority));

        // Add all vertices to the priority queue initially
        for (Vertex vertex : graph.getVertices().values()) {
            priorityQueue.add(vertex);
        }

        // Set to track dirty vertices for lazy updates
        Set<Vertex> dirtyVertices = new HashSet<>();

        while (!priorityQueue.isEmpty()) {
            System.out.println(priorityQueue.size());
            Vertex v = priorityQueue.poll();

            // Skip if vertex has been contracted
            if (contractedVertices.contains(v)) continue;

            // Lazy update check: reinsert dirty nodes
            if (dirtyVertices.contains(v)) {
                priorityQueue.add(v);
                dirtyVertices.remove(v);
                lazyUpdateCount++;
                continue;
            }

            // Contract the vertex
            vertexOrder.add(v);
            contractedVertices.add(v);
            rankMap.put(v, vertexOrder.size());

            // Simulate 1-hop contraction (neighbor contraction and shortcut addition)
            contractVertex(v);

            // Periodically reorder the priority queue after every 50 updates
            if (lazyUpdateCount >= updateThreshold) {
                updatePriorityQueue(priorityQueue);
                dirtyVertices.clear(); // Clear dirty list
                lazyUpdateCount = 0;
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Preprocessing complete. Total vertices contracted: " + vertexOrder.size());
        System.out.println("Preprocessing time: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    // Calculate priority based on edgeDiff and deletedNeighbors
    private int getNodePriority(Vertex v) {
        int edgeDiff = getEdgeDifference(v);
        int deletedNeighbors = getDeletedNeighbors(v);
        return (int) (edgeDiff + 0.75 * deletedNeighbors); // Prioritizing based on the new formula
    }

    // Calculate edge difference for a vertex: how many edges to contract
    private int getEdgeDifference(Vertex v) {
        Set<Edge> activeEdges = v.getActiveEdges();
        int contractedNeighbors = (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
        return activeEdges.size() + contractedNeighbors; // Edge difference based on remaining edges and contracted neighbors
    }

    // Calculate how many neighbors of v are contracted
    private int getDeletedNeighbors(Vertex v) {
        return (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
    }

    private void contractVertex(Vertex v) {
        Set<Edge> neighbors = v.getEdges();
        List<Vertex> neighborList = new ArrayList<>();

        // Collect all non-contracted neighbors for 1-hop contraction simulation
        for (Edge edge : neighbors) {
            Vertex u = graph.getVertexById(edge.getTo());
            if (!contractedVertices.contains(u)) {
                neighborList.add(u);
            }
        }

        // Simulate 1-hop contraction: Adding shortcut edges if necessary
        for (int i = 0; i < neighborList.size(); i++) {
            Vertex u = neighborList.get(i);
            for (int j = i + 1; j < neighborList.size(); j++) {
                Vertex w = neighborList.get(j);

                // If the path u-v-w is unique, we add a shortcut
                if (isUniqueShortestPath(u, v, w)) {
                    int shortcutCost = getCostBetween(u, v) + getCostBetween(v, w);
                    Edge shortcutEdge = new Edge(u.getId(), w.getId(), shortcutCost);
                    graph.addEdge(u.getId(), w.getId(), shortcutCost);
                    allEdges.add(shortcutEdge);
                }
            }
        }

        contractedVertices.add(v);
    }

    // Check if the u-v-w path is the unique shortest path
    private boolean isUniqueShortestPath(Vertex u, Vertex v, Vertex w) {
        QueryResult uvResult = Dijkstra.dijkstra(graph, u.getId(), v.getId());
        QueryResult vwResult = Dijkstra.dijkstra(graph, v.getId(), w.getId());

        if (uvResult.getShortestPath() == -1 || vwResult.getShortestPath() == -1) {
            return false;
        }

        long expectedDistance = uvResult.getShortestPath() + vwResult.getShortestPath();
        QueryResult uwResult = Dijkstra.dijkstra(graph, u.getId(), w.getId());

        return uwResult.getShortestPath() >= expectedDistance;
    }

    // Perform batch processing for Dijkstra's algorithm
    private void updatePriorityQueue(PriorityQueue<Vertex> priorityQueue) {
        priorityQueue.clear(); // Clear the current queue

        // Reinsert all uncontracted vertices into the priority queue
        for (Vertex vertex : graph.getVertices().values()) {
            if (!contractedVertices.contains(vertex)) {
                priorityQueue.add(vertex);
            }
        }
    }

    private int getCostBetween(Vertex u, Vertex v) {
        return u.getCostTo(v); // Placeholder logic for computing cost between two vertices
    }

    // Export the augmented graph with contracted vertices and shortcuts
    public void exportAugmentedGraph(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(graph.getVertices().size() + " " + (graph.getEdges().size() + allEdges.size()));
            writer.newLine();

            for (Vertex vertex : graph.getVertices().values()) {
                int rank = rankMap.getOrDefault(vertex, -1);
                writer.write(vertex.getId() + " " + rank);
                writer.newLine();
            }

            // Write original edges excluding contracted ones
            for (Edge edge : graph.getEdges()) {
                if (!contractedVertices.contains(graph.getVertexById(edge.getTo()))) {
                    writer.write(edge.getFrom() + " " + edge.getTo() + " " + edge.getCost() + " -1");
                    writer.newLine();
                }
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
}