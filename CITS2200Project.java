import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Deque;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.PriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Math;

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


    /* Tarjan's algorithm.

    Start with S = an empty array, and initialise all the indices of
    the nodes -1.

    Now, for each vertex:

     -> if it was not yet assigned an index, call strongconnect.

    Strong connect sets the depth index to v for the smallest unused index
    and increments that index and pushes it on to the stack S (keep it
    in a set Si so that we know that it is on the stack quickly).

    Look at each successor, if that vertex has no index, call strongconnect
    on it (depth first). Set the lowlink member of the vertex to the minimum
    of the current vertex and the successor.

    If the successor index was defined and it was already on the stack, then
    set the lowlink to the minimum of the current lowlink of the successor
    lowlink (in the strongly connected component)

    Now, once we're done with that, if this was a root node (eg, lowlink[v]
    == index[v], then build up a strongly connected component array
    using the vertices on the stack)
    */
    public static class StronglyConnectedDFS {
        private List<List<Integer>> components;
        private int[] indices;
        private int[] lowlink;
        private int currentIndex = 0;

        StronglyConnectedDFS(int n) {
            components = new LinkedList<List<Integer>>();
            indices = new int[n];
            lowlink = new int[n];
            for (int i = 0; i < n; ++i) {
                indices[i] = -1;
                lowlink[i] = -1;
            }
        }

        public void connect(Stack<Integer> traversal,
                            Set<Integer> onStack,
                            Map<Integer, List<Integer>> adjacencyList,
                            int vertex) {
            /* First, push this vertex on to the stack */
            traversal.push(new Integer(vertex));
            onStack.add(new Integer(vertex));

            /* Then assign its index and lowlink components to the current
             * available traversal index */
            indices[vertex] = currentIndex;
            lowlink[vertex] = currentIndex;

            currentIndex++;

            /* Now, look at all the siblings of this vertex. If no index
             * has been assigned to them yet, it means that we might be able
             * to add them to this component by calling strongconnect.
             * Afterwards, check to see if there are nodes with smaller
             * indices that are reachable */
            for (Integer sibling : adjacencyList.get(vertex)) {
                if (indices[sibling] == -1) {
                    connect(traversal, onStack, adjacencyList, sibling);
                    lowlink[vertex] = Math.min(lowlink[vertex], lowlink[sibling]);
                } else if (onStack.contains(sibling)) {
                    /* We've seen this sibling before somewhere - we want to
                     * assign the low-link of this vertex to the index of
                     * the sibling, since we'll eventually recurse back up to
                     * it. */
                    lowlink[vertex] = Math.min(lowlink[vertex], indices[sibling]);
                }
            }

            /* Now that we're done exploring siblings, are we the root-most
             * vertex? (Eg, the lowest-numbered vertex reachable is ourselves)
             *
             * If so, it means that we've found a new strongly connected
             * component. Pop everything off the stack and store in an
             * array to return later */
            if (lowlink[vertex] == indices[vertex] && traversal.size () > 0) {
                List<Integer> component = new LinkedList<Integer>();

                /* Create a new strongly connected component consisting
                 * only of elements from the top of the stack down to
                 * the current vertex */
                Integer topVertex = null;
                do {
                    topVertex = traversal.pop();
                    onStack.remove(topVertex);

                    /* Make sure to reset the lowlink value once we're done */
                    lowlink[topVertex] = Integer.MAX_VALUE;
                    component.add(topVertex);
                } while (topVertex != vertex);

                components.add(component);
            }
        }

        public List<List<Integer>> run(Map<Integer, List<Integer>> adjacencyList) {
            Stack<Integer> traversal = new Stack<Integer>();
            Set<Integer> onStack = new HashSet<Integer>();

            for (Integer vertex : adjacencyList.keySet()) {
                /* If we haven't yet explored this vertex, then start
                 * recursively calling connect on it and its children */
                if (indices[vertex] == -1) {
                    connect(traversal, onStack, adjacencyList, vertex);
                }
            }

            return components;
        }
    }

    public List<List<Integer>> getIntegerStronglyConnectedComponents() {
        StronglyConnectedDFS search = new StronglyConnectedDFS(adjacencyList.keySet().size());
        return search.run(adjacencyList);
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

        List<List<Integer>> integerComponents = getIntegerStronglyConnectedComponents();

        /* Convert back to an array of URL mappings */
        List<String[]> components = new ArrayList<String[]> () {{
            for (List<Integer> integerComponent : integerComponents) {
                List<String> component = new ArrayList<String>() {{
                    for (Integer vertex : integerComponent) {
                        add(urlMapping.get(vertex));
                    }
                }};

                add(component.toArray(new String[component.size()]));
            }
        }};

        return components.toArray(new String[components.size()][]);
    }

    public static class HamiltonianLongestPathBucket {
        public BlockingQueue<List<Integer>> queue;
        private int longest;

        public HamiltonianLongestPathBucket() {
            this.queue = new LinkedBlockingQueue<List<Integer>>();
            this.longest = 0;
        }

        public synchronized void consume(List<Integer> path) {
            if (path.size() > this.longest) {
                this.queue.add(path);
                this.longest = path.size();
            }
        }
    }

    public static class HamiltonianPathWorker implements Runnable {
        private HamiltonianPathScheduler scheduler;
        private Map<Integer, List<Integer>> adjacencyList;
        private int adjacencyListLength;

        public HamiltonianPathWorker(HamiltonianPathScheduler scheduler,
                                     Map<Integer, List<Integer>> adjacencyList) {
            this.scheduler = scheduler;
            this.adjacencyList = adjacencyList;
            this.adjacencyListLength = adjacencyList.size();
        }

        public void run() {
            /* Just keep on running the scheduler until it completes. The
             * scheduler will post work into the relevant bucket. */
            while (scheduler.run(adjacencyList, adjacencyListLength));
        }
    }

    public static class HamiltonianPathScheduler {
        public static class HamiltonianPathPrioritisedThread {
            public int length;
            public HamiltonianPathThread payload;

            public static class ThreadComparator implements Comparator<HamiltonianPathPrioritisedThread> {
                public int compare(HamiltonianPathPrioritisedThread o1,
                                   HamiltonianPathPrioritisedThread o2) {
                    return o2.length - o1.length;
                }
            }

            public HamiltonianPathPrioritisedThread(HamiltonianPathThread payload) {
                this.payload = payload;
                this.length = payload.length() + 1;
            }
        }

        private HamiltonianLongestPathBucket bucket;
        private Queue<HamiltonianPathPrioritisedThread> threads;
        private Queue<HamiltonianPathThread> pendingThreads;
        private final int gas;
        private final int concurrencyCap;

        public HamiltonianPathScheduler(int gas,
                                        int concurrencyCap,
                                        HamiltonianLongestPathBucket bucket) {
            this.gas = gas;
            this.concurrencyCap = concurrencyCap;
            this.threads = new PriorityQueue<HamiltonianPathPrioritisedThread>(
                11, new HamiltonianPathPrioritisedThread.ThreadComparator()
            );
            this.pendingThreads = new LinkedList<HamiltonianPathThread>();
            this.bucket = bucket;
        }

        public void add(HamiltonianPathThread thread) {
            pendingThreads.add(thread);
        }

        /* Returns true if there is more work to be done, otherwise returns
         * false */
        public boolean run(Map<Integer, List<Integer>> adjacencyList,
                           int adjacencyListLength) {
            /* Add all the pending threads to the thread set and tally up the
             * total running time. We cap the number of threads being executed
             * and pop that many off the queue. Then, the running time of
             * each thread is min(1, (length / totalLength) * gas). We keep
             * going until we either run out of gas or we don't have any more
             * threads to execute
             *
             * TODO: What we could also do here is keep track of the total
             * number of nodes left in the graph. We'll eventually reach
             * a point where it isn't possible to beat the longest path
             * and we should return early */

            /* Add all pending threads to the thread set */
            while (pendingThreads.size() > 0) {
                threads.add(new HamiltonianPathPrioritisedThread(pendingThreads.poll()));
            }

            /* Now, pop up to concurrencyCap threads */
            int nThreadsToPop = Math.min(concurrencyCap, threads.size());

            /* If we don't have any threads to run, then we're done. Return
             * false now */
            if (nThreadsToPop == 0) {
                return false;
            }

            int totalLength = 0;
            HamiltonianPathPrioritisedThread runningThreads[] = new HamiltonianPathPrioritisedThread[nThreadsToPop];

            for (int i = 0; i < nThreadsToPop; ++i) {
                HamiltonianPathPrioritisedThread prioritisedThread = threads.poll();
                totalLength += prioritisedThread.length;
                runningThreads[i] = prioritisedThread;
            }

            int threadIndex = 0;
            int remainingGas = this.gas;

            /* Keep going whilst we have some threads and gas */
            while (threadIndex < nThreadsToPop && remainingGas > 0) {
                HamiltonianPathPrioritisedThread thread = runningThreads[threadIndex];
                int threadRuntime = (int) Math.floor(Math.max(1, (thread.length / (float) totalLength) * this.gas));
                boolean completed = false;

                while (threadRuntime-- > 0 && remainingGas-- > 0) {
                    if (!thread.payload.step(adjacencyList, this)) {
                        List<Integer> path = thread.payload.generatePath();
                        this.bucket.consume(path);

                        /* Now, there is an early-return condition here. If we
                         * generated the longest possible hamiltonian path then
                         * we couldn't have generated any other paths that
                         * are interesting. Immediately return false */
                        if (path.size() == adjacencyListLength) {
                            return false;
                        }
                        completed = true;
                        break;
                    }
                }

                if (!completed) {
                    /* This thread should continue, add it to the
                     * priority queue */
                    add(thread.payload);
                }

                threadIndex++;
            }

            return true;
        }
    }

    public static class HamiltonianPathThread {
        private List<Integer> path;
        private Set<Integer> explored;

        /* Next here is the tail of the queue. We have a separate
         * member here so that we can avoid making unnecessary copies
         * of the underlying queue if we just have one child */
        private Integer next;

        private HamiltonianPathThread(List<Integer> path,
                                      Set<Integer> explored,
                                      Integer next) {
            this.path = new LinkedList<Integer>(path);
            this.explored = new HashSet<Integer>();
            this.explored.addAll(explored);
            this.next = next;
        }

        public HamiltonianPathThread(Integer start) {
            this.path = new LinkedList<Integer>();
            this.explored = new HashSet<Integer>();
            this.next = start;
        }

        public int length() {
            return path.size();
        }

        /* Returns true if the thread can continue, false if there are no
         * children and it needs to be cleaned up.
         *
         * This uses HamiltonianPathScheduler to add new threads to the
         * list when they are ready.
         */
        public boolean step(Map<Integer, List<Integer>> adjacencyList,
                            HamiltonianPathScheduler scheduler) {
            Integer node = next;
            List<Integer> children = adjacencyList.get(node);

            /* Push the next node on to the queue now and set its reference
             * to null. The path is guaranteed to be complete as soon as
             * this method returns false, which the outer algorithm
             * should keep running until that happens */
            this.path.add(next);
            this.explored.add(next);

            this.next = null;

            /* Count here refers to the number of nodes that we've explored.
             * Exploring a node either means adding it to our stack or creating
             * a copy of this thread and adding it to that thread's stack.
             *
             * We don't explore nodes we've already seen in this thread */
            int count = 0;

            if (children != null) {
                for (Integer child : children) {
                    if (this.explored.contains(child)) {
                        continue;
                    }

                    if (count == 0) {
                        /* We just set next on this thread and continue - means
                         * that we don't have to copy things unnecessarily */
                        this.next = child;
                    } else {
                        /* We have to fork this thread. Create a new thread from
                         * everything we currently have and set the next member
                         * appropriately */
                        scheduler.add(new HamiltonianPathThread(this.path,
                                                                this.explored,
                                                                child));
                    }

                    count++;
                }
            }

            return count > 0;
        }

        public List<Integer> generatePath() {
            return this.path;
        }
    }

    private static List<HamiltonianPathScheduler> threadPartitions(Map<Integer, List<Integer>> adjacencyList,
                                                                   HamiltonianLongestPathBucket bucket,
                                                                   int concurrencyCap,
                                                                   int parallelismCap,
                                                                   int gas) {
        List<HamiltonianPathScheduler> partitions = new LinkedList<HamiltonianPathScheduler>();
        HamiltonianPathScheduler current = new HamiltonianPathScheduler(gas, concurrencyCap, bucket);

        int partitionCounter = 0;
        int partitionSize = (int) Math.ceil(adjacencyList.keySet().size() / (float) parallelismCap);

        for (Integer start : adjacencyList.keySet()) {
            current.add(new HamiltonianPathThread(start));

            if (++partitionCounter == partitionSize) {
                partitions.add(current);
                current = new HamiltonianPathScheduler(gas, concurrencyCap, bucket);
                partitionCounter = 0;
            }
        }

        if (partitionCounter > 0) {
            partitions.add(current);
        }

        return partitions;
    }

    private String[] getHamiltonianPathUsingDFSFibres() {
        /* Basically, the only solution here is backtracking. However, we
         * are interested in the longest hamiltonian path. One heurestic
         * here is that if a path is long, we should probably spend more
         * time exploring it than we would exploring a short path. But we
         * also don't want to waste time on rabbit holes and ignore other
         * potential solutions.
         *
         * A clever way to do this to imitate the design of React Fibres.
         *
         * Essentially, we have a couple of "threads" going, where we
         * create a new "thread" every time there is a fork in the graph. Each
         * thread remembers its path and where it was in the depth-first
         * search process. Threads can be run for a single iteration. If
         * a thread is stuck in a situation where it has to backtrack, then it
         * adds that path and exits.
         *
         * We run a scheduler at the top of the loop which has a certain amount
         * of "gas". Threads with a longer path have a higher proportion of
         * the available gas and thus get to run for longer.
         *
         * This design is fairly easily parallelizable - you can allocate one
         * scheduler per thread and have them all share the same bucket, where
         * we push paths into the bucket as we go.
         */
        int gas = 20;
        int concurrencyCap = 4;
        int parallelismCap = 4;

        HamiltonianLongestPathBucket bucket = new HamiltonianLongestPathBucket();
        BlockingQueue<Runnable> threadPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

        /* Create a ThreadPoolExecutor with our tasks and then call shutdown()
         * on it. This will cause it to process all the tasks which we
         * submitted to it. We'll lazy fetch the results of each
         * task as they come streaming into the bucket */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(parallelismCap,
                                                             parallelismCap,
                                                             0,
                                                             TimeUnit.SECONDS,
                                                             new LinkedBlockingQueue<Runnable>());

        /* Go over all the start vertices and add them to
         * schedulers, which are then added to worker threads */
        for (HamiltonianPathScheduler scheduler: threadPartitions(adjacencyList,
                                                                  bucket,
                                                                  concurrencyCap,
                                                                  parallelismCap,
                                                                  gas)) {
            executor.execute(new HamiltonianPathWorker(scheduler, adjacencyList));
        }

        executor.shutdown();

        List<Integer> hamiltonianPath = new LinkedList<Integer>();

        /* Now that the executor is running, consume paths as they come
         * off the queue. Note that every path we receive will be strictly
         * longer than any other path, so we can just replace our existing
         * path with that one. Our strategy here is just to poll every
         * few milliseconds and then check if the executor is done
         * with its work */
        while (!executor.isTerminated() || !bucket.queue.isEmpty()) {
            try {
                List<Integer> candidate = bucket.queue.poll(250, TimeUnit.MILLISECONDS);
                if (candidate != null) {
                    hamiltonianPath = candidate;
                }
            } catch (InterruptedException e) {
                continue;
            }
        }

        /* Now that we're done, collect up the longest path and turn it
         * back into strings, since that is the most interesting */
        final List<Integer> longestHamiltonianPath = hamiltonianPath;
        return new ArrayList<String>() {{
            for (Integer node : longestHamiltonianPath) {
                add(urlMapping.get(node));
            }
        }}.toArray(new String[longestHamiltonianPath.size()]);
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
        return getHamiltonianPathUsingDFSFibres();
    }
}