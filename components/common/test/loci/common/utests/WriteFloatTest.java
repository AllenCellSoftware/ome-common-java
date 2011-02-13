//
// WriteFloatTest.java
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

import java.io.IOException;

import loci.common.IRandomAccess;
import loci.common.utests.providers.IRandomAccessProvider;
import loci.common.utests.providers.IRandomAccessProviderFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests for reading floats from a loci.common.IRandomAccess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/test/loci/common/utests/WriteFloatTest.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/test/loci/common/utests/WriteFloatTest.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @see loci.common.IRandomAccess
 */
@Test(groups="writeTests")
public class WriteFloatTest {

  private static final byte[] PAGE = new byte[] {
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
  };

  private static final String MODE = "rw";

  private static final int BUFFER_SIZE = 1024;

  private IRandomAccess fileHandle;

  private boolean checkGrowth;

  @Parameters({"provider", "checkGrowth"})
  @BeforeMethod
  public void setUp(String provider, @Optional("false") String checkGrowth)
    throws IOException {
    this.checkGrowth = Boolean.parseBoolean(checkGrowth);
    IRandomAccessProviderFactory factory = new IRandomAccessProviderFactory();
    IRandomAccessProvider instance = factory.getInstance(provider);
    fileHandle = instance.createMock(PAGE, MODE, BUFFER_SIZE);
  }

  @Test(groups="initialLengthTest")
  public void testLength() throws IOException {
    assertEquals(28, fileHandle.length());
  }

  @Test
  public void testSequential() throws IOException {
    fileHandle.writeFloat(0.0f);
    if (checkGrowth) {
      assertEquals(4, fileHandle.length());
    }
    fileHandle.writeFloat(1.0f);
    if (checkGrowth) {
      assertEquals(8, fileHandle.length());
    }
    fileHandle.writeFloat(-1.0f);
    if (checkGrowth) {
      assertEquals(12, fileHandle.length());
    }
    fileHandle.writeFloat(3.1415927f);
    if (checkGrowth) {
      assertEquals(16, fileHandle.length());
    }
    fileHandle.writeFloat(Float.MAX_VALUE);
    if (checkGrowth) {
      assertEquals(20, fileHandle.length());
    }
    fileHandle.writeFloat(Float.NEGATIVE_INFINITY);
    if (checkGrowth) {
      assertEquals(24, fileHandle.length());
    }
    fileHandle.writeFloat(Float.NaN);
    if (checkGrowth) {
      assertEquals(28, fileHandle.length());
    }
    fileHandle.seek(0);
    assertEquals(0.0f, fileHandle.readFloat());
    assertEquals(1.0f, fileHandle.readFloat());
    assertEquals(-1.0f, fileHandle.readFloat());
    assertEquals(3.1415927f, fileHandle.readFloat());
    assertEquals(Float.MAX_VALUE, fileHandle.readFloat());
    assertEquals(Float.NEGATIVE_INFINITY, fileHandle.readFloat());
    assertEquals(Float.NaN, fileHandle.readFloat());
  }

  @Test
  public void testSeekForward() throws IOException {
    fileHandle.seek(8);
    fileHandle.writeFloat(-1.0f);
    if (checkGrowth) {
      assertEquals(12, fileHandle.length());
    }
    fileHandle.writeFloat(3.1415927f);
    if (checkGrowth) {
      assertEquals(16, fileHandle.length());
    }
    fileHandle.seek(8);
    assertEquals(-1.0f, fileHandle.readFloat());
    assertEquals(3.1415927f, fileHandle.readFloat());
  }

  @Test
  public void testReset() throws IOException {
    fileHandle.writeFloat(0.0f);
    if (checkGrowth) {
      assertEquals(4, fileHandle.length());
    }
    fileHandle.writeFloat(1.0f);
    if (checkGrowth) {
      assertEquals(8, fileHandle.length());
    }
    fileHandle.seek(0);
    assertEquals(0.0f, fileHandle.readFloat());
    assertEquals(1.0f, fileHandle.readFloat());
    fileHandle.seek(0);
    fileHandle.writeFloat(-1.0f);
    fileHandle.writeFloat(3.1415927f);
    fileHandle.seek(0);
    assertEquals(-1.0f, fileHandle.readFloat());
    assertEquals(3.1415927f, fileHandle.readFloat());
  }

}
