package org.jenkinsci.plugins.buildgraphview;

import hudson.Extension;
import hudson.model.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BuildGraphActionFactory extends TransientBuildActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractBuild run) {
        return Collections.singleton(new BuildGraph(run));
    }
}
