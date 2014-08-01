package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

import com.cloudbees.plugins.flow.DownStreamRunDeclarer;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class UpstreamCauseDonwStreamRunDeclarer extends DownStreamRunDeclarer {

    @Override
    public List<Run> getDownStream(Run r) {
        Job parent = r.getParent();
        String name = parent.getFullName();
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
}
