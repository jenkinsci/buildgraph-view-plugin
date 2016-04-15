package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.promoted_builds.*;

import java.util.ArrayList;
import java.util.List;
import hudson.tasks.BuildStep;
import jenkins.model.DependencyDeclarer;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;

/**
 * Getting Abstract Projects from Promoted Builds configuration taken from DPP
 */
@Extension(optional = true)
public class PromotedBuildTriggerDownStreamRunDeclarer extends DownStreamRunDeclarer {
    // Force a classloading error if promoted-builds plugin isn't available
    public static final Class clazz = PromotedBuildAction.class;

    @Override
    public List<Run> getDownStream(Run r) {
        Job parent = r.getParent();
        String name = parent.getFullName();
        List<Run> runs = new ArrayList<Run>();
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        DependencyGraph graph = new DependencyGraph();
        AbstractProject project = ((AbstractBuild<?,?>)r).getProject();
        JobPropertyImpl property = ((AbstractBuild<?,?>)r).getProject().getProperty(JobPropertyImpl.class);
        if (property != null) {
            List<PromotionProcess> promotionProcesses = property.getActiveItems();
            for (PromotionProcess promotionProcess : promotionProcesses) {

                List<BuildStep> buildSteps = promotionProcess.getBuildSteps();

                for (BuildStep buildStep : buildSteps) {
                    if (buildStep instanceof DependencyDeclarer) {
                        ((DependencyDeclarer) buildStep).buildDependencyGraph(promotionProcess, graph);
                    }
                }
                result.addAll(graph.getDownstream(promotionProcess));
                for (BuildStep buildStep : buildSteps) {
                    if (buildStep instanceof BuildTrigger) {
                        BuildTrigger buildTrigger = (BuildTrigger) buildStep;
                        List<BuildTriggerConfig> configs = buildTrigger.getConfigs();
                        for (BuildTriggerConfig config : configs) {
                            result.addAll(config.getProjectList(project.getParent(), null));
                        }
                    }

                    if (buildStep instanceof TriggerBuilder) {
                        TriggerBuilder triggerBuilder = (TriggerBuilder) buildStep;
                        List<BlockableBuildTriggerConfig> configs = triggerBuilder.getConfigs();
                        for (BlockableBuildTriggerConfig config : configs) {
                            result.addAll(config.getProjectList(project.getParent(), null));

                        }
                    }
                }
            }
        }
        for (Job job : result) {
            List<Run> builds = job.getBuilds();
            for (Run b : builds) {
                Cause.UpstreamCause cause = (Cause.UpstreamCause) b.getCause(Cause.UpstreamCause.class);
                if (cause != null && cause.getUpstreamBuild() == r.getNumber()) {
                    runs.add(b);
                }
            }
        }
        return runs;
    }
}
