package org.jenkinsci.plugins.buildgraphview;

import hudson.model.*;

import java.io.IOException;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jenkins.model.Jenkins;
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

    private boolean isBuildInProgress = false;

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

    @Exported
    public boolean getIsBuildInProgress() { return isBuildInProgress; }

    public DirectedGraph<BuildExecution, Edge> getGraph() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException {
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
    public String getBuildSteps() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException
    {
        DirectedGraph<BuildExecution, Edge> iGraph = this.getGraph();
        StringBuilder sb = new StringBuilder();
        for(BuildExecution item : iGraph.vertexSet())
        {
            sb.append("<div class=\"build\" id=\"" + item.getId() + "\" data-column=\"" + item.getDisplayColumn() + "\" data-row=\"" + item.getDisplayRow() + "\" >");
                sb.append("<div ");
                sb.append("class=\"title\" ");
                sb.append("style=\"background-color: " + item.getIconColor().getHtmlBaseColor() + "; ");
                sb.append("background-image:linear-gradient(" + item.getIconColor().getHtmlBaseColor() + ", white);\">");
                    sb.append("<a href=\"" + item.getBuildUrl() + "\">" + item.getFullDisplayName() + "</a>");
                sb.append("</div>");
                sb.append("<div class=\"details\">");
                if(item.getDescription() != null)
                {
                    sb.append(item.getDescription());
                    sb.append("<br/>");
                }
                if(item.isStarted())
                {
                    if(item.getBuilding())
                    {
                        isBuildInProgress = true;
                        sb.append("<img title=\"Started\" alt=\"Started\" src=\"" + Jenkins.getInstance().getRootUrl() +"/images/16x16/clock.png\"/>" + item.getBuild().getTimestampString() + " ago<br/>");
                        if(item.getBuild().isBuilding()) {
                            int progress = (int) round(100.0d * (currentTimeMillis() - item.getBuild().getTimestamp().getTimeInMillis())
                                    / item.getBuild().getEstimatedDuration());
                            if (progress > 100) {
                                progress = 99;
                            }
                            sb.append("<br /><br /><table class=\"progress-bar\">");
                            sb.append("<tbody><tr>");
                            sb.append("<td class=\"progress-bar-done\" style=\"width:"+ progress +"%;\"/>");
                            sb.append("<td class=\"progress-bar-left\" style=\"width:" + (100-progress) +"%\"/>");
                            sb.append("</tr></tbody>");
                            sb.append("</table>");
                        }
                    }
                    else
                    {
                        sb.append("Status: " + item.getbuildSummaryStatusString() + "<br/>");
                        sb.append("<img title=\"Started\" alt=\"Started\" src=\"" + Jenkins.getInstance().getRootUrl() + "/images/16x16/clock.png\"/> " + item.getStartTime() + "<br/>");
                        sb.append("<img title=\"Duration\" alt=\"Duration\" src=\"" + Jenkins.getInstance().getRootUrl() + "/images/16x16/hourglass.png\"/> " + item.getDurationString() + "<br/>");
                    }
                    sb.append("<br/>");
                    sb.append("<a href=\"" + item.getBuildUrl() + "console\"><img title=\"view console output\" alt=\"console\" src=\"" + Jenkins.getInstance().getRootUrl() + "/images/16x16/terminal.png\"/></a>");
                }
                else
                {
                    sb.append("Scheduled");
                }
                sb.append("</div>");
            sb.append("</div>");
        }
        return sb.toString();
    }

    @Exported
    public  String getEndPoints()  throws ExecutionException, InterruptedException, ClassNotFoundException, IOException
    {
        StringBuilder sb = new StringBuilder();
        DirectedGraph<BuildExecution, Edge> iGraph = this.getGraph();
        String sep = "";
        for(BuildExecution item : iGraph.vertexSet())
        {
            sb.append(sep);
            sb.append("'");
            sb.append(item.getId());
            sb.append("'");
            sep = ",";
        }
        return sb.toString();
    }

    @Exported
    public String getConnectors() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException
    {
        StringBuilder sb = new StringBuilder();
        DirectedGraph<BuildExecution, Edge> iGraph = this.getGraph();
        String sep = "";
        for(Edge edge : iGraph.edgeSet())
        {
            sb.append(sep);
            sb.append("[");
            sb.append("'" + edge.getSource().getId() + "'");
            sb.append(",");
            sb.append("'" + edge.getTarget().getId() + "'");
            sb.append(",");
            sb.append(edge.getSource().getDisplayColumn());
            sb.append(",");
            sb.append(edge.getSource().getDisplayRow());
            sb.append(",");
            sb.append(edge.getTarget().getDisplayColumn());
            sb.append(",");
            sb.append(edge.getTarget().getDisplayRow());
            sb.append("]");
            sep = ":";
        }
        return sb.toString();
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

