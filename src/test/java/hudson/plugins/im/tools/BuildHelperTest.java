package hudson.plugins.im.tools;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.ResultTrend;
import hudson.plugins.im.tools.BuildHelper.ExtResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildHelperTest {

    @Test
    public void testIsFix() throws Exception {
        {
            FreeStyleBuild previousBuild = mock(FreeStyleBuild.class);
            when(previousBuild.getResult()).thenReturn(Result.FAILURE);

            FreeStyleBuild build = mock(FreeStyleBuild.class);
            when(build.getPreviousBuild()).thenReturn(previousBuild);

            // non non-successful build can ever be a fix:
            when(build.getResult()).thenReturn(Result.ABORTED);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));

            when(build.getResult()).thenReturn(Result.NOT_BUILT);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));

            when(build.getResult()).thenReturn(Result.UNSTABLE);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));

            when(build.getResult()).thenReturn(Result.FAILURE);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));

            // only a success can be a fix
            when(build.getResult()).thenReturn(Result.SUCCESS);
            assertTrue(ResultTrend.FIXED == ResultTrend.getResultTrend(build));
        }

        {
            // a success without a previous failure cannot be a fix:
            FreeStyleBuild build = mock(FreeStyleBuild.class);
            when(build.getResult()).thenReturn(Result.SUCCESS);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));
        }

        {
            // ABORTED doesn't count as failure
            FreeStyleBuild build = mock(FreeStyleBuild.class);
            when(build.getResult()).thenReturn(Result.ABORTED);
            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(build));

            FreeStyleBuild nextBuild = mock(FreeStyleBuild.class);
            when(nextBuild.getResult()).thenReturn(Result.SUCCESS);
            when(nextBuild.getPreviousBuild()).thenReturn(build);

            assertFalse(ResultTrend.FIXED == ResultTrend.getResultTrend(nextBuild));

            // but if there was a unstable/failing build somewhere before,
            // it is a fix again
            FreeStyleBuild anotherAborted = mock(FreeStyleBuild.class);
            when(build.getResult()).thenReturn(Result.ABORTED);

            FreeStyleBuild anUnstableBuild = mock(FreeStyleBuild.class);
            when(build.getResult()).thenReturn(Result.UNSTABLE);

            assertTrue(ResultTrend.FIXED == ResultTrend.getResultTrend(nextBuild));
        }
    }

    @SuppressWarnings("deprecation")
    public void testGetResultDescription() throws Exception {
        FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getResult()).thenReturn(Result.SUCCESS);
        assertEquals("SUCCESS", BuildHelper.getResultDescription(build));

        when(build.getResult()).thenReturn(Result.FAILURE);
        assertEquals("FAILURE", BuildHelper.getResultDescription(build));

        FreeStyleBuild previousBuild = mock(FreeStyleBuild.class);
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);
        when(build.getPreviousBuild()).thenReturn(previousBuild);
        assertEquals("STILL FAILING", BuildHelper.getResultDescription(build));

        when(build.getResult()).thenReturn(Result.SUCCESS);
        assertEquals("FIXED", BuildHelper.getResultDescription(build));

        when(build.getResult()).thenReturn(Result.UNSTABLE);
        assertEquals("UNSTABLE", BuildHelper.getResultDescription(build));

        when(previousBuild.getResult()).thenReturn(Result.UNSTABLE);
        assertEquals("STILL UNSTABLE", BuildHelper.getResultDescription(build));

        when(build.getResult()).thenReturn(Result.SUCCESS);
        assertEquals("FIXED", BuildHelper.getResultDescription(build));

        when(previousBuild.getResult()).thenReturn(Result.ABORTED);
        assertEquals("ABORTED", BuildHelper.getResultDescription(previousBuild));

        when(build.getResult()).thenReturn(Result.SUCCESS);
        // don't mark it as fixed:
        assertEquals("SUCCESS", BuildHelper.getResultDescription(build));

        // NOW UNSTABLE
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        assertEquals(ExtResult.NOW_UNSTABLE.toString(), BuildHelper.getResultDescription(build));
    }
}
