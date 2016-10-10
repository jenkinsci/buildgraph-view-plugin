package org.jenkinsci.plugins.buildgraphview;

import com.google.gson.Gson;
import hudson.model.*;

import java.io.IOException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

/**
 * Compute the graph of related builds, based on {@link Cause.UpstreamCause}.
 */
@ExportedBean
public class BuildGraph implements Action {

    private DirectedGraph<BuildExecution, Edge> graph;

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

    public String getBuildUrl()
    {
        return start.getBuildUrl();
    }

    public Api getApi() {
        return new BuildGraphApi(this);
    }

    private DirectedGraph<BuildExecution, Edge> getGraph() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException {
        if(graph == null) {
            graph = new SimpleDirectedGraph<BuildExecution, Edge>(Edge.class);
            graph.addVertex(start);
            index = 0;
            computeGraphFrom(start);
            setupDisplayGrid();
        }
        return graph;
    }

    private void computeGraphFrom(BuildExecution b) throws ExecutionException, InterruptedException, IOException {
        Run run = b.getBuild();
        for (DownStreamRunDeclarer declarer : DownStreamRunDeclarer.all()) {
            List<Run> runs = declarer.getDownStream(run);
            for (Run r : runs) {
                if(r != null) {
                    BuildExecution next = getExecution(r);
                    graph.addVertex(next);
                    graph.addEdge(b, next, new Edge(b, next));
                    computeGraphFrom(next);
                }
            }
        }
    }

    private BuildExecution getExecution(Run r) {
        for (BuildExecution buildExecution : graph.vertexSet()) {
            if (buildExecution.getBuild().equals(r)) return buildExecution;
        }
        return new BuildExecution(r, ++index);
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
                job.setDisplayRow(row + 1);
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

    @Exported
    public String getBuildGraph() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        DirectedGraph<BuildExecution, Edge> iGraph = this.getGraph();
        BuildGraphModel buildGraphModel = new BuildGraphModel();
        ArrayList<BuildGraphNodeModel> buildGraphNodeModelArrayList = new ArrayList();
        ArrayList<BuildGraphConnectorModel> buildGraphConnectorsModelArrayList = new ArrayList();
        for(BuildExecution item : iGraph.vertexSet()) {
            BuildGraphNodeModel buildGraphNodeModel = new BuildGraphNodeModel();
            buildGraphNodeModel.setNodeId(item.getId());
            buildGraphNodeModel.setBuildUrl(item.getBuildUrl());
            buildGraphNodeModel.setRow(item.getDisplayRow());
            buildGraphNodeModel.setColumn(item.getDisplayColumn());
            buildGraphNodeModel.setColor(item.getIconColor().getHtmlBaseColor());
            buildGraphNodeModel.setTitle(item.getFullDisplayName());
            buildGraphNodeModel.setDescription((item.getDescription() != null ? item.getDescription() : ""));
            buildGraphNodeModel.setStarted(item.isStarted());
            buildGraphNodeModel.setRunning(item.getBuild().isBuilding());
            int progress = 0;
            buildGraphNodeModel.setTimeStampString("");
            if(item.getBuild().isBuilding()) {
                progress = (int) round(100.0d * (currentTimeMillis() - item.getBuild().getTimestamp().getTimeInMillis())
                        / item.getBuild().getEstimatedDuration());
                if (progress > 100) {
                    progress = 99;
                }
                buildGraphNodeModel.setTimeStampString(item.getBuild().getTimestampString());
                buildGraphModel.setBuilding(true);
            }
            buildGraphNodeModel.setProgress(progress);
            buildGraphNodeModel.setStatus(item.getbuildSummaryStatusString());
            buildGraphNodeModel.setStartTime(item.getStartTime());
            buildGraphNodeModel.setDuration(item.getDurationString());
            buildGraphNodeModel.setRootUrl(JenkinsUtil.getInstance().getRootUrl());
            buildGraphNodeModel.setClockpng(JenkinsUtil.getInstance().getRootUrl() + "/images/16x16/clock.png");
            buildGraphNodeModel.setHourglasspng(JenkinsUtil.getInstance().getRootUrl() + "/images/16x16/hourglass.png");
            buildGraphNodeModel.setTerminalpng(JenkinsUtil.getInstance().getRootUrl() + "/images/16x16/terminal.png");

            buildGraphNodeModelArrayList.add(buildGraphNodeModel);
        }


        ArrayList<BuildGraphColumnsNodeModel> buildGraphColumnsNodeModelArrayList = new ArrayList<BuildGraphColumnsNodeModel>();
        for(BuildGraphNodeModel node : buildGraphNodeModelArrayList) {
            if(node.getColumn() >= buildGraphColumnsNodeModelArrayList.size()) {
                BuildGraphColumnsNodeModel buildGraphColumnsNodeModel = new BuildGraphColumnsNodeModel();
                ArrayList<BuildGraphNodeModel> buildGraphNodeModels = new ArrayList<BuildGraphNodeModel>();
                buildGraphNodeModels.add(node);
                buildGraphColumnsNodeModel.setNodes(buildGraphNodeModels);
                buildGraphColumnsNodeModelArrayList.add(buildGraphColumnsNodeModel);
            }
            else {
                BuildGraphColumnsNodeModel buildGraphColumnsNodeModel = buildGraphColumnsNodeModelArrayList.get(node.getColumn());
                buildGraphColumnsNodeModel.getNodes().add(node);
            }
        }

        for(Edge edge : iGraph.edgeSet()) {
            BuildGraphConnectorModel buildGraphConnectorModel = new BuildGraphConnectorModel();
            buildGraphConnectorModel.setSource(edge.getSource().getId());
            buildGraphConnectorModel.setTarget(edge.getTarget().getId());
            buildGraphConnectorsModelArrayList.add(buildGraphConnectorModel);
        }
        buildGraphModel.setNodesSize(buildGraphNodeModelArrayList.size());
        buildGraphModel.setNodes(buildGraphColumnsNodeModelArrayList);
        buildGraphModel.setConnectors(buildGraphConnectorsModelArrayList);
        Gson gson = new Gson();
        return gson.toJson(buildGraphModel);
    }

    public static class Edge implements Serializable {

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

