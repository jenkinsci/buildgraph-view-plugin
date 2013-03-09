package org.jenkinsci.plugins.buildgraphview;

import hudson.model.Cause;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class DownStream extends InvisibleAction {

    private transient List<Run> downStream = new ArrayList<Run>();

    public void addDownStream(Run r) {
        downStream.add(r);
    }

    public List<Run> getDownStream() {
        return downStream;
    }

    /**
     * Retrieve or create on-demand a DownStream action for this run
     */
    public static DownStream forBuild(Run run) {
        DownStream action = run.getAction(DownStream.class);
        if (action == null) {
            action = new DownStream();
            run.addAction(action);
        }
        return action;
    }

}
