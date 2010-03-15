//
// ReadByteSubArrayTest.java
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

import java.io.IOException;

import loci.common.IRandomAccess;
import loci.common.utests.providers.IRandomAccessProvider;
import loci.common.utests.providers.IRandomAccessProviderFactory;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests for reading bytes from a loci.common.IRandomAccess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/common/test/loci/common/utests/ReadByteSubArrayTest.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/common/test/loci/common/utests/ReadByteSubArrayTest.java">SVN</a></dd></dl>
 *
 * @see loci.common.IRandomAccess
 */
@Test(groups="readTests")
public class ReadByteSubArrayTest {

  private static final byte[] PAGE = new byte[] {
    (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
    (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
    (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C,
    (byte) 0x0D, (byte) 0x0E, (byte) 0xFF, (byte) 0xFE
  };

  private static final String MODE = "r";

  private static final int BUFFER_SIZE = 2;

  private IRandomAccess fileHandle;

  @Parameters({"provider"})
  @BeforeMethod
  public void setUp(String provider) throws IOException {
    IRandomAccessProviderFactory factory = new IRandomAccessProviderFactory();
    IRandomAccessProvider instance = factory.getInstance(provider);
    fileHandle = instance.createMock(PAGE, MODE, BUFFER_SIZE);
  }

  @Test
  public void testLength() throws IOException {
    assertEquals(16, fileHandle.length());
  }

  @Test
  public void testSequentialReadByte() throws IOException {
    byte[] b = new byte[16];
    int length = fileHandle.read(b,0,16);
    assertEquals(16, fileHandle.getFilePointer());
    assertEquals(16, length);
    assertEquals(0x01, b[0]);
    assertEquals(0x02, b[1]);
    assertEquals(0x03, b[2]);
    assertEquals(0x04, b[3]);
    assertEquals(0x05, b[4]);
    assertEquals(0x06, b[5]);
    assertEquals(0x07, b[6]);
    assertEquals(0x08, b[7]);
    assertEquals(0x09, b[8]);
    assertEquals(0x0A, b[9]);
    assertEquals(0x0B, b[10]);
    assertEquals(0x0C, b[11]);
    assertEquals(0x0D, b[12]);
    assertEquals(0x0E, b[13]);
    assertEquals((byte) 0xFF, b[14]);
    assertEquals((byte) 0xFE, b[15]);
  }

  @Test
  public void testSeekForwardReadByte() throws IOException {
    fileHandle.seek(7);
    byte[] b = new byte[4];
    int length = fileHandle.read(b,1,2);
    assertEquals(9, fileHandle.getFilePointer());
    assertEquals(2, length);
    assertEquals(0x00, b[0]);
    assertEquals(0x08, b[1]);
    assertEquals(0x09, b[2]);
    assertEquals(0x00, b[3]);
  }

  @Test
  public void testResetReadByte() throws IOException {
    byte[] b = new byte[4];
    int length = fileHandle.read(b,1,2);
    assertEquals(2, fileHandle.getFilePointer());
    assertEquals(0x02, length);
    assertEquals(0x00, b[0]);
    assertEquals(0x01, b[1]);
    assertEquals(0x02, b[2]);
    assertEquals(0x00, b[3]);
    fileHandle.seek(0);
    b = new byte[4];
    length = fileHandle.read(b,1,2);
    assertEquals(0x02, length);
    assertEquals(0x00, b[0]);
    assertEquals(0x01, b[1]);
    assertEquals(0x02, b[2]);
    assertEquals(0x00, b[3]);
  }

  @Test
  public void testSeekBackReadByte() throws IOException {
    fileHandle.seek(15);
    fileHandle.seek(7);
    byte[] b = new byte[4];
    int length = fileHandle.read(b,1,2);
    assertEquals(9, fileHandle.getFilePointer());
    assertEquals(2, length);
    assertEquals(0x00, b[0]);
    assertEquals(0x08, b[1]);
    assertEquals(0x09, b[2]);
    assertEquals(0x00, b[3]);
  }

  @Test
  public void testRandomAccessReadByte() throws IOException {
    testSeekForwardReadByte();
    testSeekBackReadByte();
    // The test relies on a "new" file or reset file pointer
    fileHandle.seek(0);
    testResetReadByte();
  }

}
