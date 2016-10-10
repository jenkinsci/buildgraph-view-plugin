package org.jenkinsci.plugins.buildgraphview;

import java.util.ArrayList;

public class BuildGraphColumnsNodeModel {
    private ArrayList<BuildGraphNodeModel> nodes;

    public ArrayList<BuildGraphNodeModel> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<BuildGraphNodeModel> nodes) {
        this.nodes = nodes;
    }
}
