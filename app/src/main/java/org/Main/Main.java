package org.Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {

    // Implement the contraction phase using the ContractionHierarchy class
    private static void contractionPhase(Graph graph) {
        // Initialize the ContractionHierarchy with the given graph
        ContractionHierarchy contractionHierarchy = new ContractionHierarchy(graph);

        // Perform the preprocessing phase (i.e., contraction)
        long start = System.nanoTime();
        contractionHierarchy.preprocess();  // This method will contract nodes and add shortcut edges
        long end = System.nanoTime();
        double contractionTimeInSeconds = (end - start) / 1_000_000_000.0; // Convert to seconds
        System.out.println("Contraction time (s): " + contractionTimeInSeconds);

        // Optionally, save the augmented graph here (after preprocessing)
        // graph.saveAugmentedGraph("augmented_denmark.graph");
    }

    public static void main(String[] args) {
        try {
            // Load the graph from resources
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("denmark.graph");
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources: denmark.graph");
            }

            Graph graph = Graph.readGraphFromInput(inputStream);

            // Step 1: Perform the contraction phase (preprocessing)
            contractionPhase(graph);

            // Step 2: Generate 1,000 random source-target pairs
            int numVertices = graph.getVertices().size();
            List<int[]> pairs = RandomPairs.generateRandomPairs(1000, numVertices, 314159);

            // Metrics for the query phase
            long totalQueryTime = 0;
            long totalRelaxedEdges = 0;
            int queryCount = pairs.size();

            // Step 3: Run the bidirectional Dijkstra algorithm for each pair
            for (int[] pair : pairs) {
                int source = pair[0];
                int target = pair[1];

                // Bidirectional Dijkstra - timing and relaxed edges tracking
                long start = System.nanoTime();
                QueryResult bidirectionalResult = BidirectionalDijkstra.bidirectionalDijkstra(graph, source, target);
                long end = System.nanoTime();
                totalQueryTime += (end - start);
                totalRelaxedEdges += bidirectionalResult.getRelaxedEdges();

                // Optionally, validate results by comparing with Dijkstra results
                // long dijkstraResult = Dijkstra.dijkstra(graph, source, target);
                // if (dijkstraResult != bidirectionalResult.getShortestPath()) {
                //     System.err.println("Mismatch in results for pair: " + source + ", " + target);
                // }
            }

            // Step 4: Compute and report average times and relaxed edges
            double avgQueryTime = totalQueryTime / 1_000_000.0 / queryCount; // Convert to ms
            double avgRelaxedEdges = totalRelaxedEdges / (double) queryCount;

            // Report results
            System.out.println("Average query time (ms): " + avgQueryTime);
            System.out.println("Average number of relaxed edges per query: " + avgRelaxedEdges);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}