package org.jenkinsci.plugins.buildgraphview;

import hudson.model.BallColor;
import hudson.model.Run;

import java.io.Serializable;
import java.text.DateFormat;

/**
 * A wrapper on a AbstractBuild that maintains additional layout information, used during graphical rendering.
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BuildExecution implements Serializable {

    private transient Run build;

    // A unique number that identifies when in the FlowAbstractBuild this job was started
    private final int buildIndex;

    private String id;

    private String buildUrl;

    private String startTime;

    private String buildName;

    private String buildNumber;

    private BallColor iconColor;

    private String fullDisplayName;

    private String description;

    private boolean building;

    private String durationString;

    private String buildSummaryStatusString;

    private int displayColumn;

    private int displayRow;

    public BuildExecution(Run build, int buildIndex) {
        this.build = build;
        this.buildIndex = buildIndex;
        this.id = "build-" + buildIndex;
        this.buildUrl = this.build != null ? this.build.getAbsoluteUrl() : null;
        this.startTime = isStarted() ? DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT).format(build.getTime())
                : "";
        this.buildName = build.getParent().getName();
        this.buildNumber = "" + build.number;
        this.iconColor = build.getIconColor();
        this.fullDisplayName = build.getFullDisplayName();
        this.description = build.getDescription();
        this.building = build.isBuilding();
        this.durationString = build.getDurationString();
        this.buildSummaryStatusString = build.getBuildStatusSummary().message;
    }

    public BuildExecution(int buildIndex) {
        this.buildIndex = buildIndex;
    }

    public BallColor getIconColor() {
        return iconColor;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean getBuilding() {
        return building;
    }

    public String getDurationString() {
        return durationString;
    }

    public String getbuildSummaryStatusString() {
        return buildSummaryStatusString;
    }

    public String getId() {
        return id;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getStartTime() {
        if(build == null) {
            return startTime;
        }
        if (isStarted()) {
            return startTime = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT)
                    .format(build.getTime());
        }
        return "";
    }

    public boolean isStarted() {
        return build != null ? build.getTime() != null : true;
    }

    public Run<?,?> getBuild() {
        return build;
    }

    public int getBuildIndex() {
        return buildIndex;
    }

    public int getDisplayColumn() {
        return displayColumn;
    }

    public int getDisplayRow() {
        return displayRow;
    }

    public void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }

    public void setDisplayRow(int displayRow) {
        this.displayRow = displayRow;
    }

    public String toString() {
        return (buildName != null && buildNumber != null ? buildName + " #" + buildNumber : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuildExecution) {
            return build.equals(((BuildExecution) obj).build);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (build != null) {
            return build.hashCode();
        }
        return super.hashCode();
    }
}
