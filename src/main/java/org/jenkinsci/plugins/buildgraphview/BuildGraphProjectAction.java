package org.jenkinsci.plugins.buildgraphview;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractProject;
import hudson.model.Action;

public class BuildGraphProjectAction implements Action {

    private AbstractProject abstractProject;

    public BuildGraphProjectAction(AbstractProject abstractProject) {
        this.abstractProject = abstractProject;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/buildgraph-view/images/16x16/chain.png";
    }

    @Override
    public String getDisplayName() {
        return "Build Graph";
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String getUrlName() {
        if (abstractProject.getLastBuild() == null) {
            return JenkinsUtil.getInstance().getRootUrl() + abstractProject.getUrl();
        }
        return JenkinsUtil.getInstance().getRootUrl() + abstractProject.getLastBuild().getUrl() + "BuildGraph";
    }
}
