package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

@Extension
public class BuildGraphProjectActionFactory extends TransientProjectActionFactory{
    @Override
    public Collection<? extends Action> createFor(AbstractProject abstractProject) {
        return Collections.singleton(new BuildGraphProjectAction(abstractProject));
    }
}
