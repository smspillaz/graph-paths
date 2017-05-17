import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;

import java.util.Comparator;

public class CITS2200Project {
    /* The invariant here is that the size of these three maps is always
     * equal. If a node exists, it will have mapping from an int to a string,
     * a string to an int and an int to a sibling list */
    public Map<String, Integer> nodeMapping = new HashMap<String, Integer>();
    public Map<Integer, String> urlMapping = new HashMap<Integer, String>();
    public Map<Integer, List<Integer>> adjacencyList = new HashMap<Integer, List<Integer>>();
    int nextNode = 0;

    private void ensureMappingForString(String str) {
        if (!nodeMapping.containsKey(str)) {
            nodeMapping.put(str, nextNode);
            urlMapping.put(new Integer(nextNode), str);
            adjacencyList.put(new Integer(nextNode),
                              new LinkedList<Integer>());
            nextNode++;
        }
    }

    /**
     * Adds an edge to the Wikipedia page graph. If the pages do not
     * already exist in the graph, they will be added to the graph.
     * 
     * @param urlFrom the URL which.containsKey a link to urlTo.
     * @param urlTo the URL which urlFrom.containsKey a link to.
     */
    public void addEdge(String urlFrom, String urlTo) {
        ensureMappingForString(urlFrom);
        ensureMappingForString(urlTo);

        int from = nodeMapping.get(urlFrom);
        int to = nodeMapping.get(urlTo);

        /* Add the edge to the adjacency list */
        List<Integer> adjacent = adjacencyList.get(from);
        adjacent.add(new Integer(to));
    }

    public class PQComparator implements Comparator<VertexWeight> {
        public int compare(VertexWeight o1, VertexWeight o2) {
            return o1.weight - o2.weight;
        }
    }

    public class VertexWeight {
        public int vertex;
        public int weight;

        public VertexWeight(int vertex, int weight) {
            this.vertex = vertex;
            this.weight = weight;
        }
    }

    public int[] getShortestPaths(int from) {
        Queue<VertexWeight> queue = new PriorityQueue<VertexWeight>(11, new PQComparator());
        int distances[] = new int[adjacencyList.keySet().size() ];

        /* Initialize all distances to -1 */
        for (int i = 0; i < adjacencyList.keySet().size() ; ++i) {
            distances[i] = -1;
        }

        /* Add with a distance of zero */
        queue.add(new VertexWeight(from, 0));

        while (queue.size() != 0) {
            VertexWeight node = queue.poll();

            /* Already seen, we're not going to do any better than this */
            if (distances[node.vertex] != -1) {
                continue;
            }

            distances[node.vertex] = node.weight;

            /* Explore all neighbours and update the distance
             * and parent list as we go. We're picking the cheapest
             * vertex first here. If we see something cheaper, we need
             * to update the priority queue */
            List<Integer> siblings = adjacencyList.get(node.vertex);
            if (siblings != null) {
                for (Integer sibling : siblings) {
                    int total = distances[node.vertex] + 1;

                    if (distances[sibling] == -1) {
                        queue.add(new VertexWeight(sibling, total));
                    }
                }
            }
        }

        return distances;
    }

    /**
     * Finds the shorest path in number of links between two pages.
     * If there is no path, returns -1.
     * 
     * @param urlFrom the URL where the path should start.
     * @param urlTo the URL where the path should end.
     * @return the legnth of the shorest path in number of links followed.
     */
    public int getShortestPath(String urlFrom, String urlTo) {
        /* Fairly straightforward - look up the nodes and use Dijkstra's
         * algorithm to get the shortest path in O(E log V) time */
        int from = nodeMapping.get(urlFrom);
        int to = nodeMapping.get(urlTo);

        /* Now that we're done, we can just get the distance to the target
         * vertex in the array */
        return getShortestPaths(from)[to];
    }

    public static int getMaxInArray(int[] array) {
        int highest = Integer.MIN_VALUE;

        for (int i = 0; i < array.length; ++i) {
            if (array[i] > highest) {
                highest = array[i];
            }
        }

        return highest;
    }

    /**
     * Finds all the centers of the page graph. The order of pages
     * in the output does not matter. Any order is correct as long as
     * all the centers are in the array, and no pages that aren't centers
     * are in the array.
     * 
     * @return an array containing all the URLs that correspond to pages that are centers.
     */
    public String[] getCenters() {
        /* Essentially here we need to get the longest "shortest path" of each
         * vertex and store that along with the vertex in an array. Then pick
         * the minima of the <longest "shortest path", vertex> and return the
         * set of all strings in that have that length.
         *
         * The cost here is that we need to re-heapify the queue every time
         * we add a new path, which makes it a V log V operation. */
        Queue<VertexWeight> queue = new PriorityQueue<VertexWeight>(11, new PQComparator());

        for (Integer vertex : adjacencyList.keySet()) {
            int longestShortestPathInArray = getMaxInArray(getShortestPaths(vertex));

            /* Note that it only makes sense to add a vertex here if
             * this value is at least greater than one. Otherwise, we would
             * prefer self-connected vertices or vertices with no connections */
            if (longestShortestPathInArray > 0) {
                queue.add(new VertexWeight(vertex, longestShortestPathInArray));
            }
        }

        /* Now that we have explored the connection of every vertex to every
         * other vertex, we can just take the minima of the priority queue */
        List<String> centers = new ArrayList<String>();
        VertexWeight node = queue.poll();
        int minima = node.weight;
        centers.add(urlMapping.get(node.vertex));

        while (queue.size() != 0) {
            node = queue.poll();
            if (node.weight > minima) {
                break;
            }

            centers.add(urlMapping.get(node.vertex));
        }

        return centers.toArray(new String[centers.size()]);
    }

    /**
     * Finds all the strongly connected components of the page graph.
     * Every strongly connected component can be represented as an array 
     * containing the page URLs in the component. The return value is thus an array
     * of strongly connected components. The order of elements in these arrays
     * does not matter. Any output that contains all the strongly connected
     * components is considered correct.
     * 
     * @return an array containing every strongly connected component.
     */
    public String[][] getStronglyConnectedComponents() {
        return null;
    }

    /**
     * Finds a Hamiltonian path in the page graph. There may be many
     * possible Hamiltonian paths. Any of these paths is a correct output.
     * This method should never be called on a graph with more than 20
     * vertices. If there is no Hamiltonian path, this method will
     * return an empty array. The output array should contain the URLs of pages
     * in a Hamiltonian path. The order matters, as the elements of the
     * array represent this path in sequence. So the element [0] is the start
     * of the path, and [1] is the next page, and so on.
     * 
     * @return a Hamiltonian path of the page graph.
     */
    public String[] getHamiltonianPath() {
        return null;
    }
}