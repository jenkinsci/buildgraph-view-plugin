package org.jenkinsci.plugins.buildgraphview;

import com.cloudbees.plugins.flow.FlowCause;
import com.cloudbees.plugins.flow.FlowRun;
import com.cloudbees.plugins.flow.JobInvocation;
import hudson.Extension;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */

public class FlowDownStreamRunDeclarer extends DownStreamRunDeclarer {

    @Override
    public List<Run> getDownStream(Run r) throws ExecutionException, InterruptedException {

        if (r instanceof FlowRun) {
            FlowRun f = (FlowRun) r;
            return getOutgoingEdgeRuns(f, f.getStartJob());
        }

        List<Run> runs = Collections.emptyList();
        FlowCause cause = (FlowCause) r.getCause(FlowCause.class);
        FlowRun f;
        while (runs.isEmpty() && cause != null) {
            f = cause.getFlowRun();
            runs = getOutgoingEdgeRuns(f, cause.getAssociatedJob());
            cause = (FlowCause) cause.getFlowRun().getCause(FlowCause.class);
        }

        return runs;
    }

    private List<Run> getOutgoingEdgeRuns(FlowRun f, JobInvocation start) throws ExecutionException, InterruptedException {
        Set<FlowRun.JobEdge> edges = f.getJobsGraph().outgoingEdgesOf(start);
        List<Run> runs = new ArrayList<Run>(edges.size());
        for (FlowRun.JobEdge edge : edges) {
            JobInvocation targetJobEdge = edge.getTarget();
            if (targetJobEdge.isStarted()) {
                Run run = targetJobEdge.getBuild();
                if (run != null) {
                    runs.add(run);
                }
            }
        }
        return runs;
    }
}
