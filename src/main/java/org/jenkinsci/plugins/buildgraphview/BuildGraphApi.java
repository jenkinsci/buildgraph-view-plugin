package org.jenkinsci.plugins.buildgraphview;

import hudson.model.Api;

/**
 * Created by suresh on 4/26/2016.
 */
public class BuildGraphApi extends Api {
    public BuildGraphApi(BuildGraph buildGraph) {
        super(buildGraph);
    }
}
