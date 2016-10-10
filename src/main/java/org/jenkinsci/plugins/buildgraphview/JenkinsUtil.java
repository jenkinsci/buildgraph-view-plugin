package org.jenkinsci.plugins.buildgraphview;


import jenkins.model.Jenkins;

public final class JenkinsUtil {

    private JenkinsUtil() {
    }

    public static Jenkins getInstance() {
        Jenkins instance = Jenkins.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Jenkins has not been started, or was already shut down");
        }
        return instance;
    }
}
