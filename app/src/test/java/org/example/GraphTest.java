package org.example;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.Main.BidirectionalDijkstra;
import org.Main.Dijkstra;
import org.Main.Graph;
import org.junit.Test;

public class GraphTest {
 @Test
    public void testGraphBuilding() throws Exception {
        String input = "4 4\n" +
                "0 0.0 0.0\n" +
                "1 1.0 1.0\n" +
                "2 2.0 2.0\n" +
                "3 3.0 3.0\n" +
                "0 1 10\n" +
                "1 2 10\n" +
                "2 3 10\n" +
                "3 0 10\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Graph graph = Graph.readGraphFromInput(in);

        assertEquals(4, graph.getVertices().size());
        assertEquals(4, graph.getAdjacencyList().size());
    }

    @Test
    public void testDijkstraEarlyStopping() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addEdge(0, 1, 5);

        long distance = Dijkstra.dijkstra(graph, 0, 1);
        assertEquals(5, distance);
    }

    @Test
    public void testBidirectionalDijkstra() {
        Graph graph = new Graph();
        graph.addVertex(0, 0, 0);
        graph.addVertex(1, 1, 1);
        graph.addEdge(0, 1, 5);

        long distance = BidirectionalDijkstra.bidirectionalDijkstra(graph, 0, 1);
        assertEquals(5, distance);
    }
}
