package org.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ContractionHierarchy {
    private Graph graph;
    private List<Vertex> vertexOrder;
    private Set<Vertex> contractedVertices;
    private Map<Vertex, Integer> rankMap;
    private List<Edge> allEdges;
    private int updateThreshold = 50;
    private int totalShortcutsAdded = 0;

    public ContractionHierarchy(Graph graph) {
        this.graph = graph;
        this.vertexOrder = new ArrayList<>();
        this.contractedVertices = new HashSet<>();
        this.rankMap = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }

    public void preprocess() {
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(this::getNodePriority));
        Set<Vertex> dirtyVertices = new HashSet<>();
    
        // Initialize priority queue with non-contracted vertices
        for (Vertex v : graph.getVertices().values()) {
            if (!contractedVertices.contains(v)) {
                priorityQueue.add(v);
            }
        }
    
        Map<Vertex, Integer> priorityCache = new HashMap<>();
        for (Vertex v : graph.getVertices().values()) {
            priorityCache.put(v, getNodePriority(v));
        }
    
        while (!priorityQueue.isEmpty() && contractedVertices.size() < graph.getVertices().size()) {
            Vertex v = priorityQueue.poll();
            if (contractedVertices.contains(v)) continue;
    
            vertexOrder.add(v);
            contractedVertices.add(v);
            rankMap.put(v, vertexOrder.size());
            System.out.println("Contracted vertices: " + contractedVertices.size());
    
            int shortcutsAdded = contractVertex(v);
            System.out.println("Shortcuts added for vertex " + v.getId() + ": " + shortcutsAdded);
    
            totalShortcutsAdded += shortcutsAdded;
    
            for (Edge e : v.getEdges()) {
                Vertex neighbor = graph.getVertexById(e.getTo());
                if (!contractedVertices.contains(neighbor)) {
                    dirtyVertices.add(neighbor);
                }
            }
    
            if (dirtyVertices.size() > updateThreshold) {
                for (Vertex vertex : dirtyVertices) {
                    if (!contractedVertices.contains(vertex)) {
                        priorityQueue.add(vertex);
                        priorityCache.put(vertex, getNodePriority(vertex));
                    }
                }
                dirtyVertices.clear();
            }
        }
    }
    

    public int getNodePriority(Vertex v) {
        int edgeDiff = getEdgeDifference(v);
        int deletedNeighbors = getDeletedNeighbors(v);
        return (int) (edgeDiff + 0.75 * deletedNeighbors);
    }

    public List<Vertex> getVertexOrder() {
        return vertexOrder;
    }

    public int getTotalShortcutsAdded() {
        return totalShortcutsAdded;
    }

    private int getEdgeDifference(Vertex v) {
        Set<Edge> activeEdges = v.getActiveEdges();
        int contractedNeighbors = (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
        return activeEdges.size() + contractedNeighbors;
    }

    private int getDeletedNeighbors(Vertex v) {
        return (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
    }

    public Graph getAugmentedGraph() {
        Graph augmentedGraph = new Graph();

        // Add all vertices from the original graph
        for (Vertex vertex : graph.getVertices().values()) {
            augmentedGraph.addVertex(vertex);
        }

        // Add edges from the original graph that are not contracted
        for (Edge edge : graph.getEdges()) {
            if (!contractedVertices.contains(graph.getVertexById(edge.getTo()))) {
                augmentedGraph.addEdge(edge.getFrom(), edge.getTo(), edge.getCost());
            }
        }

        // Add shortcut edges
        for (Edge shortcutEdge : allEdges) {
            augmentedGraph.addEdge(shortcutEdge.getFrom(), shortcutEdge.getTo(), shortcutEdge.getCost());
        }

        return augmentedGraph;
    }

    public int contractVertex(Vertex v) {
        int shortcutsAdded = 0;
    
        Map<Vertex, Integer> neighbors = new HashMap<>();
        for (Edge edge : v.getEdges()) {
            Vertex neighbor = graph.getVertexById(edge.getTo());
            if (!contractedVertices.contains(neighbor)) {
                neighbors.put(neighbor, edge.getCost());
            }
        }
    
        // Attempt to add shortcuts between neighbors
        for (Vertex u : neighbors.keySet()) {

            for (Vertex w : neighbors.keySet()) {

                if (!u.equals(w)) {
                    int shortcutCost = neighbors.get(u) + neighbors.get(w);
    
                    // Debug log for checking edges and costs
                    System.out.println("Checking shortcut between " + u.getId() + " and " + w.getId());
                    boolean hasEdge = graph.hasEdge(u.getId(), w.getId());
                    System.out.println("Has edge: " + hasEdge);
    
                    int existingEdgeCost = hasEdge ? graph.getEdgeCost(u.getId(), w.getId()) : -1;
                    System.out.println("Existing edge cost: " + existingEdgeCost + ", shortcut cost: " + shortcutCost);
    
                    // Check if a shortcut can be added
                    if (!hasEdge || existingEdgeCost > shortcutCost) {
                        System.out.println("No edge or shortcut cost is better, adding shortcut.");
                        graph.addEdge(u.getId(), w.getId(), shortcutCost);
                        allEdges.add(new Edge(u.getId(), w.getId(), shortcutCost));
                        shortcutsAdded++;
                    } else {
                        System.out.println("Existing edge is cheaper, not adding shortcut.");
                    }
                }
            }
        }
    
        return shortcutsAdded;
    }
    

    public boolean isUniqueShortestPath(Vertex u, Vertex v, Vertex w) {
        // Simplified uniqueness check for now, disable the Dijkstra call temporarily for debugging
        return true;
        // If you want to keep the Dijkstra check, ensure Dijkstra runs correctly and returns valid shortest path values
    }

    public void exportAugmentedGraph(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(graph.getVertices().size() + " " + (graph.getEdges().size() + allEdges.size()));
            writer.newLine();

            // Write vertices and their ranks
            for (Vertex vertex : graph.getVertices().values()) {
                int rank = rankMap.getOrDefault(vertex, -1);
                writer.write(vertex.getId() + " " + rank);
                writer.newLine();
            }

            // Write original edges not contracted
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
        } catch (IOException e) {
            throw new RuntimeException("Error writing augmented graph to file", e);
        }
    }
}
