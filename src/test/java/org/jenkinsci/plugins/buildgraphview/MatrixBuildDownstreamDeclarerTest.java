package org.jenkinsci.plugins.buildgraphview;

import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Build;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatrixBuildDownstreamDeclarerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @WithoutJenkins
    @Test
    public void testGetDownStream() throws Exception {
        final MatrixRun runs[] = { mock(MatrixRun.class), mock(MatrixRun.class) };
        final List<MatrixRun> expectedRuns = Arrays.asList(runs);
        final MatrixBuild matrix = mock(MatrixBuild.class);
        when(matrix.getExactRuns()).thenReturn(expectedRuns);
        final MatrixBuildDownstreamDeclarer declarer = new MatrixBuildDownstreamDeclarer();
        List<Run> result = declarer.getDownStream(matrix);
        assertThat(result).containsOnlyElementsOf(expectedRuns);
    }

    @WithoutJenkins
    @Test
    public void testGetDownStreamForNonMatrixProject() throws Exception {
        final Build matrix = mock(Build.class);
        final MatrixBuildDownstreamDeclarer declarer = new MatrixBuildDownstreamDeclarer();
        List<Run> result = declarer.getDownStream(matrix);
        assertThat(result).isEmpty();
    }

    @WithoutJenkins
    @Test
    public void testGetDownStreamForEmptyMatrixProject() throws Exception {
        final MatrixBuild matrix = mock(MatrixBuild.class);
        final MatrixBuildDownstreamDeclarer declarer = new MatrixBuildDownstreamDeclarer();
        List<Run> result = declarer.getDownStream(matrix);
        assertThat(result).isEmpty();
    }

    @Test
    public void testIsExtension() throws Exception {
        final List<DownStreamRunDeclarer> all = DownStreamRunDeclarer.all();
        assertThat(all).usingElementComparator(new Comparator<DownStreamRunDeclarer>() {
            public int compare(DownStreamRunDeclarer o1, DownStreamRunDeclarer o2) {
                return o1.getClass().toString().compareTo(o2.getClass().toString());
            }
        }).contains(new MatrixBuildDownstreamDeclarer());
    }
}
