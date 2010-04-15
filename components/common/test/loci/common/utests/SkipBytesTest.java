//
// SkipBytesTest.java
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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Tests for reading bytes from a loci.common.IRandomAccess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/common/test/loci/common/utests/SkipBytesTest.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/common/test/loci/common/utests/SkipBytesTest.java">SVN</a></dd></dl>
 *
 * @see loci.common.IRandomAccess
 */
@Test(groups="readTests")
public class SkipBytesTest {

  private static final byte[] PAGE = new byte[] {
    // 64-byte page
    (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
    (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
    (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C,
    (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10,
    (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
    (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18,
    (byte) 0x19, (byte) 0x1A, (byte) 0x1B, (byte) 0x1C,
    (byte) 0x1D, (byte) 0x1E, (byte) 0x1F, (byte) 0x20,
    (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24,
    (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28,
    (byte) 0x29, (byte) 0x2A, (byte) 0x2B, (byte) 0x2C,
    (byte) 0x2D, (byte) 0x2E, (byte) 0x2F, (byte) 0x30,
    (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34,
    (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38,
    (byte) 0x39, (byte) 0x3A, (byte) 0x3B, (byte) 0x3C,
    (byte) 0x3D, (byte) 0x3E, (byte) 0x3F, (byte) 0x40,
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
    assertEquals(64, fileHandle.length());
  }

  @Test
  public void testSkip4Bytes() throws IOException {
    int skipLength = fileHandle.skipBytes(4);
    assertEquals(4, skipLength);
    assertEquals(5, fileHandle.readByte());
    assertEquals(6, fileHandle.readByte());
    assertEquals(7, fileHandle.readByte());
    assertEquals(8, fileHandle.readByte());
  }

  @Test
  public void testSkipOffEnd() throws IOException {
    int skipLength = fileHandle.skipBytes(65);
    assertEquals(64, skipLength);
  }

  @Test
  public void testSkipNegativeBytes() throws IOException {
    assertEquals(0, fileHandle.skipBytes(-1));
  }

  @Test
  public void testSkipZeroBytes() throws IOException {
    assertEquals(0, fileHandle.skipBytes(0));
  }

}
