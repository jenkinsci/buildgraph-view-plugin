package org.jenkinsci.plugins.buildgraphview;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Compute the graph of related builds, based on {@link Cause.UpstreamCause}.
 */
public class BuildGraph implements Action {

    private transient DirectedGraph<BuildExecution, Edge> graph;

    private BuildExecution start;

    private transient int index = 0;

    public BuildGraph(AbstractBuild run) {
        this.start = new BuildExecution(run, 0);
    }

    public String getIconFileName() {
        return "/plugin/buildgraph-view/images/16x16/chain.png";
    }

    public String getDisplayName() {
        return "Build Graph";
    }

    public String getUrlName() {
        return "BuildGraph";
    }

    public BuildExecution getStart() {
        return start;
    }

    public DirectedGraph<BuildExecution, Edge> getGraph() {
        graph = new SimpleDirectedGraph<BuildExecution, Edge>(Edge.class);
        graph.addVertex(start);
        index = 0;
        computeGraphFrom(start);
        setupDisplayGrid();
        return graph;
    }

    private void computeGraphFrom(BuildExecution b) {
        List<Run> runs = getDownStream(b.getBuild());
        for (Run r : runs) {
            BuildExecution next = new BuildExecution(r, ++index);
            graph.addVertex(next);
            graph.addEdge(b, next, new Edge(b, next));
            computeGraphFrom(next);
        }
    }

    private List<Run> getDownStream(Run r) {
        Job parent = r.getParent();
        String name = parent.getName();
        List<Run> runs = new ArrayList<Run>();
        if (parent instanceof AbstractProject) {
            // I can't see any reason DependencyGraph require AbstractProject, not Run
            List<AbstractProject> jobs = Jenkins.getInstance().getDependencyGraph().getDownstream((AbstractProject) parent);
            for (Job job : jobs) {
                List<Run> builds = job.getBuilds();
                for (Run b : builds) {
                    Cause.UpstreamCause cause = (Cause.UpstreamCause) b.getCause(Cause.UpstreamCause.class);
                    if (cause != null && cause.getUpstreamProject().equals(name) && cause.getUpstreamBuild() == r.getNumber()) {
                        runs.add(b);
                    }
                }
            }
        }
        return runs;
    }

    /**
     * Assigns a unique row and column to each build in the graph
     */
    private void setupDisplayGrid() {
        List<List<BuildExecution>> allPaths = findAllPaths(start);
        // make the longer paths bubble up to the top
        Collections.sort(allPaths, new Comparator<List<BuildExecution>>() {
            public int compare(List<BuildExecution> runs1, List<BuildExecution> runs2) {
                return runs2.size() - runs1.size();
            }
        });
        // set the build row and column of each build
        // loop backwards through the rows so that the lowest path a job is on
        // will be assigned
        for (int row = allPaths.size() - 1; row >= 0; row--) {
            List<BuildExecution> path = allPaths.get(row);
            for (int column = 0; column < path.size(); column++) {
                BuildExecution job = path.get(column);
                job.setDisplayColumn(Math.max(job.getDisplayColumn(), column));
                job.setDisplayRow(row);
            }
        }
    }

    /**
     * Finds all paths that start at the given vertex
     * @param start the origin
     * @return a list of paths
     */
    private List<List<BuildExecution>> findAllPaths(BuildExecution start) {
        List<List<BuildExecution>> allPaths = new LinkedList<List<BuildExecution>>();
        if (graph.outDegreeOf(start) == 0) {
            // base case
            List<BuildExecution> singlePath = new LinkedList<BuildExecution>();
            singlePath.add(start);
            allPaths.add(singlePath);
        } else {
            for (Edge edge : graph.outgoingEdgesOf(start)) {
                List<List<BuildExecution>> allPathsFromTarget = findAllPaths(edge.getTarget());
                for (List<BuildExecution> path : allPathsFromTarget) {
                    path.add(0, start);
                }
                allPaths.addAll(allPathsFromTarget);
            }
        }
        return allPaths;
    }



    public static class Edge {

        private BuildExecution source;
        private BuildExecution target;

        public Edge(BuildExecution source, BuildExecution target) {
            this.source = source;
            this.target = target;
        }

        public BuildExecution getSource() {
            return source;
        }

        public BuildExecution getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return source.toString() + " -> " + target.toString();
        }
    }
}

