package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BuildGraphActionFactory extends TransientBuildActionFactory {

    /**
     * Create a BuildGraph action for all builds, and also populate a {@link DownStream} for all upstream builds so
     * that we get an upstream -> downstream one-to-many link.
     */
    @Override
    public Collection<? extends Action> createFor(Run run) {
        Cause.UpstreamCause cause = (Cause.UpstreamCause) run.getCause(Cause.UpstreamCause.class);
        if (cause != null) {
            Job up = Jenkins.getInstance().getItemByFullName(
                    cause.getUpstreamProject(), Job.class);
            if (up != null) {
                Run r = up.getBuildByNumber(cause.getUpstreamBuild());
                if (r != null) {
                    DownStream.forBuild(r).addDownStream(run);
                }
            }
        }

        return Collections.singleton(new BuildGraph(run));
    }
}
