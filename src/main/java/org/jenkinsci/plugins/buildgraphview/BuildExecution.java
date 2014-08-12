package org.jenkinsci.plugins.buildgraphview;

import hudson.model.Run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;

/**
 * A wrapper on a AbstractBuild that maintains additional layout information, used during graphical rendering.
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BuildExecution {

    private transient Run build;

    // A unique number that identifies when in the FlowAbstractBuild this job was started
    private final int buildIndex;

    private int displayColumn;
    private String promoString = "";
    private int displayRow;
    public boolean causedByPromotion = false;
    public BuildExecution(Run build, int buildIndex) {
        this.build = build;
        this.buildIndex = buildIndex;
    }

    public String getId() {
        return "build-" + buildIndex;
    }

    public String getBuildUrl() {
        return this.build != null ? this.build.getAbsoluteUrl() : null;
    }

    public String getStartTime() {
        if (isStarted()) {
        	String dateAndPromotion = "";
        	dateAndPromotion+= DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(build.getTime());
        	dateAndPromotion+=this.promoString;
            return dateAndPromotion;
        }
        return "";
    }

    public boolean isStarted() {
        return build.getTime() != null;
    }
    
    public void setPromoString(){
    	
    	String url = this.build.getAbsoluteUrl()+"/injectedEnvVars/export";
		try{
			URL buildUrl = new URL(url);
			InputStreamReader isr = new InputStreamReader(buildUrl.openStream());
			BufferedReader br = new BufferedReader(isr);
			String line;
			String jobName = "";
			String jobNum = "";
			while((line = br.readLine()) != null){
				int colonIndex = line.indexOf('=');
				String key = line.substring(0,colonIndex);
				String val = line.substring(colonIndex+1,line.length());
				if(key.contains("BUILDGRAPH_PARENT_NAME")){
					jobName = val;
				}else if(key.contains("BUILDGRAPH_PARENT_NUMBER")){
					jobNum = val;
				}
			}
			if((jobName != "") && (jobNum != "")){
				this.causedByPromotion = true;
				this.promoString += " : due to promotion on " + jobName + " #" + jobNum;
			}
			br.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
    	
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
        return (build != null ? build.getParent().getName() + " #" + build.number : "");
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
        return build.hashCode();
    }
}
