package org.jenkinsci.plugins.buildgraphview;

public class BuildGraphNodeModel {
    private String nodeId;
    private int row;
    private int column;
    private String title;
    private String color;
    private String buildUrl;
    private String description;
    private Boolean started;
    private Boolean running;
    private String status;
    private int progress;
    private String startTime;
    private String duration;
    private String rootUrl;
    private String clockpng;
    private String hourglasspng;
    private String terminalpng;
    private String timeStampString;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getClockpng() {
        return clockpng;
    }

    public void setClockpng(String clockpng) {
        this.clockpng = clockpng;
    }

    public String getHourglasspng() {
        return hourglasspng;
    }

    public void setHourglasspng(String hourglasspng) {
        this.hourglasspng = hourglasspng;
    }

    public String getTerminalpng() {
        return terminalpng;
    }

    public void setTerminalpng(String terminalpng) {
        this.terminalpng = terminalpng;
    }

    public String getTimeStampString() {
        return timeStampString;
    }

    public void setTimeStampString(String timeStampString) {
        this.timeStampString = timeStampString;
    }
}
