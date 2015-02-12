package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Add all combination-builds of a Matrix-build to downstream-builds.
 *
 * Created by christianlangmann on 26/01/15.
 */
@Extension
public class MatrixBuildDownstreamDeclarer extends DownStreamRunDeclarer {
    @Override
    public List<Run> getDownStream(Run r) throws ExecutionException, InterruptedException {

        List<Run> runs = new ArrayList<Run>();
        if (r instanceof MatrixBuild) {
            final MatrixBuild matrixRun = (MatrixBuild)r;
            for (Run downstreamRun : matrixRun.getExactRuns()) {
                runs.add(downstreamRun);
            }
        }
        return runs;
    }
}
