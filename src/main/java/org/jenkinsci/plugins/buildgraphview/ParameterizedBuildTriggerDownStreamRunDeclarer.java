package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.parameterizedtrigger.BuildInfoExporterAction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension(optional = true)
public class ParameterizedBuildTriggerDownStreamRunDeclarer extends DownStreamRunDeclarer {

    @Override
    public List<Run> getDownStream(Run r) {
        List<Run> runs = new ArrayList<Run>();
        for (BuildInfoExporterAction action : r.getActions(BuildInfoExporterAction.class)) {
            runs.addAll(action.getTriggeredBuilds());
        }
        return runs;
    }
}
