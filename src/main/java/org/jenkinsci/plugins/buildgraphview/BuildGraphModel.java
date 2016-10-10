package org.jenkinsci.plugins.buildgraphview;

import java.util.ArrayList;

public class BuildGraphModel {

    private ArrayList<BuildGraphColumnsNodeModel> nodes;
    private ArrayList<BuildGraphConnectorModel> connectors;
    private Boolean isBuilding = false;
    private Integer nodesSize;

    public ArrayList<BuildGraphColumnsNodeModel> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<BuildGraphColumnsNodeModel> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<BuildGraphConnectorModel> getConnectors() {
        return connectors;
    }

    public void setConnectors(ArrayList<BuildGraphConnectorModel> connectors) {
        this.connectors = connectors;
    }

    public Boolean getBuilding() {
        return isBuilding;
    }

    public void setBuilding(Boolean building) {
        isBuilding = building;
    }

    public Integer getNodesSize() {
        return nodesSize;
    }

    public void setNodesSize(Integer nodesSize) {
        this.nodesSize = nodesSize;
    }
}
