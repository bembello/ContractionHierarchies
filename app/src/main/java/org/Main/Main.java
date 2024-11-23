package org.Main;

import java.io.InputStream;
import java.util.List;

public class Main {

    // Implement the contraction phase using the ContractionHierarchy class
    private static ContractionHierarchy contractionPhase(Graph graph) {
        // Initialize the ContractionHierarchy with the given graph
        ContractionHierarchy contractionHierarchy = new ContractionHierarchy(graph);

        // Perform the preprocessing phase (i.e., contraction)
        long start = System.nanoTime();
        contractionHierarchy.preprocess();  // This method will contract nodes and add shortcut edges
        long end = System.nanoTime();
        double contractionTimeInSeconds = (end - start) / 1_000_000_000.0; // Convert to seconds
        System.out.println("Contraction time (s): " + contractionTimeInSeconds);

        // Export the augmented graph with shortcut edges
        contractionHierarchy.exportAugmentedGraph("augmented_denmark.graph");
        
        return contractionHierarchy; // Return the ContractionHierarchy for further usage
    }

    public static void main(String[] args) {
        try {
            // Load the graph from resources
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("denmark.graph");
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources: denmark.graph");
            }

            // Read the graph from the input stream
            Graph graph = Graph.readGraphFromInput(inputStream);

            // Generate random pairs (for queries) based on the number of vertices
            int numVertices = graph.getVertices().size();
            List<int[]> pairs = RandomPairs.generateRandomPairs(1000, numVertices, 314159);

            // Perform the contraction phase (preprocessing)
            ContractionHierarchy contractionHierarchy = contractionPhase(graph);

            // Use the augmented graph (with shortcuts) for bidirectional Dijkstra
            Graph augmentedGraph = contractionHierarchy.getAugmentedGraph();

            // Metrics for the query phase (for benchmarking purposes)
            long totalQueryTime = 0;
            long totalRelaxedEdges = 0;
            int queryCount = pairs.size();
            int i = 0;

            // Run the bidirectional Dijkstra algorithm for each random pair on the augmented graph
            for (int[] pair : pairs) {
                System.out.println("Processing pair " + i++);

                int source = pair[0];
                int target = pair[1];

                // Bidirectional Dijkstra - timing and relaxed edges tracking on the contracted graph
                long start = System.nanoTime();
                QueryResult bidirectionalResult = BidirectionalDijkstraCH.bidirectionalDijkstra(augmentedGraph, source, target);
                long end = System.nanoTime();
                totalQueryTime += (end - start);
                totalRelaxedEdges += bidirectionalResult.getRelaxedEdges();
            }

            // Compute and report average times and relaxed edges per query
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
