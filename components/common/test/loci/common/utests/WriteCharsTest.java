//
// WriteCharsTest.java
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
 * Tests for writing bytes to a loci.common.IRandomAccess.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/common/test/loci/common/utests/WriteCharsTest.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/common/test/loci/common/utests/WriteCharsTest.java">SVN</a></dd></dl>
 *
 * @see loci.common.IRandomAccess
 */
@Test(groups="writeTests")
public class WriteCharsTest {

  private static final byte[] PAGE = new byte[] {
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
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
    assertEquals(16, fileHandle.length());
  }
  
  @Test
  public void testWriteSequential() throws IOException {
    fileHandle.writeChars("ab");
    if (checkGrowth) {
      assertEquals(4, fileHandle.length());
    }
    fileHandle.writeChars("cd");
    if (checkGrowth) {
      assertEquals(8, fileHandle.length());
    }
    fileHandle.writeChars("ef");
    if (checkGrowth) {
      assertEquals(12, fileHandle.length());
    }
    fileHandle.writeChars("gh");
    assertEquals(16, fileHandle.length());
    fileHandle.seek(0);
    for (byte i = (byte) 0x61; i < 0x69; i++) {
      assertEquals((byte) 0x00, fileHandle.readByte());
      assertEquals(i, fileHandle.readByte());
    }
  }

  @Test
  public void testWrite() throws IOException {
    fileHandle.writeChars("ab");
    assertEquals(4, fileHandle.getFilePointer());
    if (checkGrowth) {
      assertEquals(4, fileHandle.length());
    }
    fileHandle.seek(0);
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x61, fileHandle.readByte());
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x62, fileHandle.readByte());
  }

  @Test
  public void testWriteOffEnd() throws IOException {
    fileHandle.seek(16);
    fileHandle.writeChars("wx");
    assertEquals(20, fileHandle.getFilePointer());
    assertEquals(20, fileHandle.length());
    fileHandle.seek(16);
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x77, fileHandle.readByte());
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x78, fileHandle.readByte());
  }

  @Test
  public void testWriteTwiceOffEnd() throws IOException {
    fileHandle.seek(16);
    fileHandle.writeChars("wx");
    fileHandle.writeChars("yz");
    assertEquals(24, fileHandle.getFilePointer());
    assertEquals(24, fileHandle.length());
    fileHandle.seek(16);
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x77, fileHandle.readByte());
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x78, fileHandle.readByte());
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x79, fileHandle.readByte());
    assertEquals((byte) 0x00, fileHandle.readByte());
    assertEquals((byte) 0x7A, fileHandle.readByte());
  }

}
