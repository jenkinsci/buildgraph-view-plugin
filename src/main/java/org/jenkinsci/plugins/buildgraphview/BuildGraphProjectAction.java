package org.jenkinsci.plugins.buildgraphview;

import hudson.model.AbstractProject;
import hudson.model.Action;
import jenkins.model.Jenkins;

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
    public String getUrlName() {
        return Jenkins.getInstance().getRootUrl() + abstractProject.getLastBuild().getUrl() + "BuildGraph";
    }
}
