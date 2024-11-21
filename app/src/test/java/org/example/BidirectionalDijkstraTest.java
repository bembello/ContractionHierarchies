package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.Main.BidirectionalDijkstra;
import org.Main.Graph;
import org.Main.QueryResult;
import org.junit.Test;


public class BidirectionalDijkstraTest {

    @Test
    public void testSimpleGraph() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addVertex(2, 2, 2);
        graph.addEdge(0, 1, 1); // Edge 1: 0 -> 1
        graph.addEdge(1, 2, 1); // Edge 2: 1 -> 2

        QueryResult result = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 2);
        assertEquals(2, result.getShortestPath()); // Shortest path: 0 -> 1 -> 2
        assertTrue(result.getRelaxedEdges() > 0); // Some relaxed edges should have been counted
    }

    @Test
    public void testNoDirectPath() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addVertex(2, 2, 2);
        graph.addEdge(0, 1, 5); // Edge 1: 0 -> 1
        graph.addEdge(1, 2, 5); // Edge 2: 1 -> 2
    
        // Path exists via vertex 1 (0 -> 1 -> 2)
        QueryResult result = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 2);
        assertEquals(10, result.getShortestPath()); // Path cost is 10
        assertTrue(result.getRelaxedEdges() > 0); // Some edges were relaxed
    }
    

    @Test
    public void testMultiplePaths() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addVertex(2, 2, 2);
        graph.addEdge(0, 1, 3); // Edge 1: 0 -> 1
        graph.addEdge(1, 2, 4); // Edge 2: 1 -> 2
        graph.addEdge(0, 2, 5); // Edge 3: 0 -> 2 (direct)

        QueryResult result = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 2);
        assertEquals(5, result.getShortestPath()); // Shortest path: 0 -> 2
        assertTrue(result.getRelaxedEdges() > 0); // Some relaxed edges should have been counted
    }

    @Test
    public void testNoEdges() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);

        // No edges, so no path should exist
        QueryResult result = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 1);
        assertEquals(-1, result.getShortestPath()); // No path exists
        assertEquals(0, result.getRelaxedEdges()); // No relaxed edges
    }

    @Test
    public void testSourceEqualsTarget() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0); // Single vertex graph
    
        QueryResult result = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 0);
    
        // Fix assertion to expect shortest path 0 and 0 relaxed edges
        assertEquals(0, result.getShortestPath());
        assertEquals(0, result.getRelaxedEdges());
    }
}
