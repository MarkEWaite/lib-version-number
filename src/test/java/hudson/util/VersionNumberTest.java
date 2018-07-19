/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Xavier Le Vourch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.util;

import java.util.Arrays;
import org.apache.maven.artifact.versioning.ComparableVersion;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

public class VersionNumberTest {

    @Test
    public void isNewerThan() {
       assertTrue(new VersionNumber("2.0.*").isNewerThan(new VersionNumber("2.0")));
       assertTrue(new VersionNumber("2.1-SNAPSHOT").isNewerThan(new VersionNumber("2.0.*")));
       assertTrue(new VersionNumber("2.1").isNewerThan(new VersionNumber("2.1-SNAPSHOT")));
       assertTrue(new VersionNumber("2.0.*").isNewerThan(new VersionNumber("2.0.1")));
       assertTrue(new VersionNumber("2.0.1").isNewerThan(new VersionNumber("2.0.1-SNAPSHOT")));
       assertTrue(new VersionNumber("2.0.1-SNAPSHOT").isNewerThan(new VersionNumber("2.0.0.99")));
       assertTrue(new VersionNumber("2.0.0.99").isNewerThan(new VersionNumber("2.0.0")));
       assertTrue(new VersionNumber("2.0.0").isNewerThan(new VersionNumber("2.0.ea")));
       assertTrue(new VersionNumber("2.0").isNewerThan(new VersionNumber("2.0.ea")));
       // the inversion of the previous test case from the old behaviour is explained by
       // which makes more sense than before
       assertTrue(new VersionNumber("2.0.0").equals(new VersionNumber("2.0")));
    }

    @Test
    public void alpha() {
       assertTrue(new VersionNumber("2.0").isNewerThan(new VersionNumber("2.0-alpha-1")));
       assertTrue(new VersionNumber("2.0-alpha-1").isNewerThan(new VersionNumber("2.0-alpha-1-rc9999.abc123def456")));
    }
    
    @Test
    public void earlyAccess() {
       assertTrue(new VersionNumber("2.0.ea2").isNewerThan(new VersionNumber("2.0.ea1")));
       assertTrue(new VersionNumber("2.0.ea1").isNewerThan(new VersionNumber("2.0.ea")));
       assertEquals(new VersionNumber("2.0.ea"), new VersionNumber("2.0.ea0"));
    }
    
    @Test
    public void snapshots() {
        assertTrue(new VersionNumber("1.12").isNewerThan(new VersionNumber("1.12-SNAPSHOT (private-08/24/2008 12:13-hudson)")));
        assertTrue(new VersionNumber("1.12-SNAPSHOT (private-08/24/2008 12:13-hudson)").isNewerThan(new VersionNumber("1.11")));
        assertTrue(new VersionNumber("1.12-SNAPSHOT (private-08/24/2008 12:13-hudson)").equals(new VersionNumber("1.12-SNAPSHOT")));
        // This is changed from the old impl because snapshots are no longer a "magic" number
        assertFalse(new VersionNumber("1.12-SNAPSHOT").equals(new VersionNumber("1.11.*")));
        assertTrue(new VersionNumber("1.11.*").isNewerThan(new VersionNumber("1.11.9")));
        assertTrue(new VersionNumber("1.12-SNAPSHOT").isNewerThan(new VersionNumber("1.12-rc9999.abc123def456")));
    }

    @Test
    public void timestamps() {
        assertTrue(new VersionNumber("2.0.3-20170207.105042-1").isNewerThan(new VersionNumber("2.0.2")));
        assertTrue(new VersionNumber("2.0.3").isNewerThan(new VersionNumber("2.0.3-20170207.105042-1")));
        assertTrue(new VersionNumber("2.0.3-20170207.105042-1").equals(new VersionNumber("2.0.3-SNAPSHOT")));
        assertTrue(new VersionNumber("2.0.3-20170207.105042-1").equals(new VersionNumber("2.0.3-SNAPSHOT (private-08/24/2008 12:13-hudson)")));
        assertTrue(new VersionNumber("2.0.3-20170207.105043-2").isNewerThan(new VersionNumber("2.0.3-20170207.105042-1")));
        assertTrue(new VersionNumber("2.0.3-20170207.105042-2").isNewerThan(new VersionNumber("2.0.3-20170207.105042-1")));
        assertTrue(new VersionNumber("2.0.3-20170207.105042-13").isNewerThan(new VersionNumber("2.0.3-20170207.105042-2")));
        assertFalse(new VersionNumber("2.0.3-20170207.105042-1").isNewerThan(new VersionNumber("2.0.3-SNAPSHOT")));
        assertFalse(new VersionNumber("2.0.3-20170207.105042-1").isOlderThan(new VersionNumber("2.0.3-SNAPSHOT")));
    }

    @Test
    public void digit() {
        assertEquals(32, new VersionNumber("2.32.3.1-SNAPSHOT").getDigitAt(1));
        assertEquals(3, new VersionNumber("2.32.3.1-SNAPSHOT").getDigitAt(2));
        assertEquals(1, new VersionNumber("2.32.3.1-SNAPSHOT").getDigitAt(3));
        assertEquals(-1, new VersionNumber("2.32.3.1-SNAPSHOT").getDigitAt(4));
        assertEquals(2, new VersionNumber("2.7.22.0.2").getDigitAt(4));
        assertEquals(3, new VersionNumber("2.7.22.0.3-SNAPSHOT").getDigitAt(4));
        assertEquals(-1, new VersionNumber("2.0.3-20170207.105042-1").getDigitAt(4));
        assertEquals(-1, new VersionNumber("2.0.3").getDigitAt(5));
        assertEquals(2, new VersionNumber("2.0.3").getDigitAt(0));
        assertEquals(-1, new VersionNumber("2.0.3").getDigitAt(-1));
        assertEquals(-1, new VersionNumber("1.0.0.GA.2-3").getDigitAt(3));
        assertEquals(-1, new VersionNumber("").getDigitAt(-1));
        assertEquals(-1, new VersionNumber("").getDigitAt(0));
    }

    @Ignore("TODO still pretty divergent: expected:<[2.0.0, 2.0, 2.0.*, 2.0.ea, 2.0.0.99, 2.0.1-alpha-1-rc9999.abc123def456, 2.0.1-alpha-1, 2.0.1-rc9999.abc123def456, 2.0.1-SNAPSHOT, 2.0.1]> but was:<[2.0.ea, 2.0.0, 2.0, 2.0.1-alpha-1-rc9999.abc123def456, 2.0.1-alpha-1, 2.0.1-rc9999.abc123def456, 2.0.1-SNAPSHOT, 2.0.*, 2.0.0.99, 2.0.1]>")
    @Issue("JENKINS-51594")
    @Test
    public void mavenComparison() {
        String[] versions = {"2.0.*", "2.0.1", "2.0.1-SNAPSHOT", "2.0.0.99", "2.0.0", "2.0.ea", "2.0", "2.0.1-rc9999.abc123def456", "2.0.1-alpha-1-rc9999.abc123def456", "2.0.1-alpha-1"};
        // Much more easily expressed in java.level=8:
        ComparableVersion[] control = new ComparableVersion[versions.length];
        for (int i = 0; i < versions.length; i++) {
            control[i] = new ComparableVersion(versions[i]);
        }
        Arrays.sort(control);
        String[] expected = new String[versions.length];
        for (int i = 0; i < versions.length; i++) {
            expected[i] = control[i].toString();
        }
        VersionNumber[] test = new VersionNumber[versions.length];
        for (int i = 0; i < versions.length; i++) {
            test[i] = new VersionNumber(versions[i]);
        }
        Arrays.sort(test);
        String[] actual = new String[versions.length];
        for (int i = 0; i < versions.length; i++) {
            actual[i] = test[i].toString();
        }
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }

}
