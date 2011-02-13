//
// LocationTest.java
//

/*
LOCI Common package: utilities for I/O, reflection and miscellaneous tasks.
Copyright (C) 2005-@year@ Melissa Linkert and Curtis Rueden.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.common.utests;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import loci.common.Location;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the loci.common.Location class.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/test/loci/common/utests/LocationTest.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/test/loci/common/utests/LocationTest.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @see loci.common.Location
 */
public class LocationTest {

  // -- Fields --

  private Location[] files;
  private boolean[] exists;
  private boolean[] isDirectory;
  private boolean[] isHidden;
  private String[] mode;

  // -- Setup methods --

  @BeforeMethod
  public void setup() throws IOException {
    File tmpDirectory = new File(System.getProperty("java.io.tmpdir"),
      System.currentTimeMillis() + "-location-test");
    boolean success = tmpDirectory.mkdirs();
    tmpDirectory.deleteOnExit();

    File hiddenFile = File.createTempFile(".hiddenTest", null, tmpDirectory);
    hiddenFile.deleteOnExit();

    File invalidFile = File.createTempFile("invalidTest", null, tmpDirectory);
    String invalidPath = invalidFile.getAbsolutePath();
    invalidFile.delete();

    File validFile = File.createTempFile("validTest", null, tmpDirectory);
    validFile.deleteOnExit();

    files = new Location[] {
      new Location(validFile.getAbsolutePath()),
      new Location(invalidPath),
      new Location(tmpDirectory),
      new Location("http://loci.wisc.edu/software/bio-formats"),
      new Location("http://openmicroscopy.org/software/bio-formats"),
      new Location(hiddenFile)
    };

    exists = new boolean[] {
      true, false, true, true, false, true
    };

    isDirectory = new boolean[] {
      false, false, true, false, false, false
    };

    isHidden = new boolean[] {
      false, false, false, false, false, true
    };

    mode = new String[] {
      "rw", "", "rw", "r", "", "rw"
    };

  }

  // -- Tests --

  @Test
  public void testReadWriteMode() {
    for (int i=0; i<files.length; i++) {
      String msg = files[i].getName();
      assertEquals(msg, files[i].canRead(), mode[i].indexOf("r") != -1);
      assertEquals(msg, files[i].canWrite(), mode[i].indexOf("w") != -1);
    }
  }

  @Test
  public void testAbsolute() {
    for (Location file : files) {
      assertEquals(file.getName(), file.getAbsolutePath(),
        file.getAbsoluteFile().getAbsolutePath());
    }
  }

  @Test
  public void testExists() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].exists(), exists[i]);
    }
  }

  @Test
  public void testCanonical() throws IOException {
    for (Location file : files) {
      assertEquals(file.getName(), file.getCanonicalPath(),
        file.getCanonicalFile().getAbsolutePath());
    }
  }

  @Test
  public void testParent() {
    for (Location file : files) {
      assertEquals(file.getName(), file.getParent(),
        file.getParentFile().getAbsolutePath());
    }
  }

  @Test
  public void testIsDirectory() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].isDirectory(), isDirectory[i]);
    }
  }

  @Test
  public void testIsFile() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].isFile(),
        !isDirectory[i] && exists[i]);
    }
  }

  @Test
  public void testIsHidden() {
    for (int i=0; i<files.length; i++) {
      assertEquals(files[i].getName(), files[i].isHidden(), isHidden[i]);
    }
  }

  @Test
  public void testListFiles() {
    for (int i=0; i<files.length; i++) {
      String[] completeList = files[i].list();
      String[] unhiddenList = files[i].list(true);
      Location[] fileList = files[i].listFiles();

      if (!files[i].isDirectory()) {
        assertEquals(files[i].getName(), completeList, null);
        assertEquals(files[i].getName(), unhiddenList, null);
        assertEquals(files[i].getName(), fileList, null);
        continue;
      }

      assertEquals(files[i].getName(), completeList.length, fileList.length);

      List<String> complete = Arrays.asList(completeList);
      for (String child : unhiddenList) {
        assertEquals(files[i].getName(), complete.contains(child), true);
        assertEquals(files[i].getName(),
          new Location(files[i], child).isHidden(), false);
      }

      for (int f=0; f<fileList.length; f++) {
        assertEquals(files[i].getName(),
          fileList[f].getName(), completeList[f]);
      }
    }
  }

  @Test
  public void testToURL() throws IOException {
    for (Location file : files) {
      String path = file.getAbsolutePath();
      if (path.indexOf("://") == -1) {
        path = "file://" + path;
      }
      if (file.isDirectory() && !path.endsWith(File.separator)) {
        path += File.separator;
      }
      assertEquals(file.getName(), file.toURL(), new URL(path));
    }
  }

  @Test
  public void testToString() {
    for (Location file : files) {
      assertEquals(file.getName(), file.toString(), file.getAbsolutePath());
    }
  }

}
