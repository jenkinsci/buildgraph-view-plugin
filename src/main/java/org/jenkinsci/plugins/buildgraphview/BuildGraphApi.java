package org.jenkinsci.plugins.buildgraphview;

import hudson.model.Api;

public class BuildGraphApi extends Api {
    public BuildGraphApi(BuildGraph buildGraph) {
        super(buildGraph);
    }
}
