/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package dap4.servlet;

import static com.google.common.truth.Truth.assertThat;
import static dap4.servlet.ChunkWriter.MAXCHUNKSIZE;

import dap4.core.dmr.DapType;
import dap4.core.util.DapConstants;
import dap4.dap4lib.RequestMode;
import dap4.servlet.ChunkWriter.State;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.Test;

public class TestChunkWriter {

  /* compute chunk data size from three bytes */
  private int getSize(byte[] sizeBytes) {
    assertThat(sizeBytes.length).isEqualTo(3);
    return (sizeBytes[2] & 0xFF) | ((sizeBytes[1] & 0xFF) << 8) | ((sizeBytes[0] & 0x0F) << 16);
  }


  private void checkChunkType(byte chunkTypeByte, int expectedType) {
    // the first bit of the byte is chunk type
    // 0 - data
    // 1 - end
    assertThat(chunkTypeByte & 0b0000_0001).isEqualTo(expectedType);
    // the second bit of the byte is the error flag
    // 0 - no error
    // 1 - error
    assertThat(chunkTypeByte & 0b0000_0010).isEqualTo(0);
  }

  /**
   * Test that the chunk writer handles a value whose bytes are split between two chunks
   */
  @Test
  public void testTwoChunkSplit() throws IOException {
    final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    final int sourceLen = 18022;
    final int[] source = new int[sourceLen];

    // verify that our source array requires two chunks
    assertThat(source.length * 4).isGreaterThan(MAXCHUNKSIZE);

    // populate the source array
    for (int i = 0; i < source.length; i++) {
      source[i] = i;
    }

    // number of integers contained within the first chunk
    final int intsInFirstChunk = MAXCHUNKSIZE / 4;
    final int numberOfRemainingBytes = MAXCHUNKSIZE - intsInFirstChunk * 4;

    // reduce ints in the second chunk by 1 to account for split integer
    final int intsInSecondChunk = sourceLen - intsInFirstChunk - 1;

    // prepare chunk writer
    ByteArrayOutputStream chunkResponseStream = new ByteArrayOutputStream();
    ChunkWriter cw = new ChunkWriter(chunkResponseStream, RequestMode.DAP, byteOrder);
    cw.state = State.DATA;

    // encode source array into byte array
    byte[] sourceByteArray = SerialWriter.encodeArray(DapType.INT32, source, byteOrder).array();

    // use chunk writer to write the source array
    cw.write(sourceByteArray, 0, sourceByteArray.length);
    cw.close();

    // read chunk writer output into a byte buffer
    byte[] bytes = chunkResponseStream.toByteArray();
    ByteBuffer chunkResponseByteBuffer = ByteBuffer.wrap(bytes);
    chunkResponseByteBuffer.order(byteOrder);
    chunkResponseByteBuffer.position(0);

    ///////////////////////
    // test the first chunk

    // verify the CHUNKTYPE is DATA
    checkChunkType(chunkResponseByteBuffer.get(), DapConstants.CHUNK_DATA);

    // verify CHUNKSIZE (first chunk contains full MAXCHUNKSIZE bytes)
    assertThat(getSize(
        new byte[] {chunkResponseByteBuffer.get(), chunkResponseByteBuffer.get(), chunkResponseByteBuffer.get()}))
            .isEqualTo(MAXCHUNKSIZE);

    // verify int values of first CHUNKDATA,
    int lastCompleteValueFromChunks = -1;
    for (int i = 0; i < intsInFirstChunk; i++) {
      lastCompleteValueFromChunks = chunkResponseByteBuffer.getInt();
      assertThat(lastCompleteValueFromChunks).isEqualTo(i);
    }
    assertThat(lastCompleteValueFromChunks).isEqualTo(intsInFirstChunk - 1);

    // save remaining bytes from chunk 1 (not enough bytes to make an integer)
    byte[] boundaryByteArray = new byte[4];
    boolean split = false;
    for (int i = 0; i < numberOfRemainingBytes; i++) {
      split = true;
      boundaryByteArray[i] = chunkResponseByteBuffer.get();
    }

    // ensure we have an integer whose bytes are split between chunks
    assertThat(split).isTrue();

    ///////////////////////
    // test the second chunk

    // verify the CHUNKTYPE is END
    checkChunkType(chunkResponseByteBuffer.get(), DapConstants.CHUNK_END);

    // verify that the second chunk contains the remaining source bytes and that
    // it will hold the expected number of ints
    int size = getSize(
        new byte[] {chunkResponseByteBuffer.get(), chunkResponseByteBuffer.get(), chunkResponseByteBuffer.get()});
    assertThat(size).isEqualTo(sourceLen * 4 - MAXCHUNKSIZE);
    assertThat(size / 4).isEqualTo(intsInSecondChunk);

    // The second CHUNKDATA starts with the remaining bytes from an int split
    // between chunk 1 and chunk 2, so get those into the boundaryByteArray
    for (int i = numberOfRemainingBytes; i < boundaryByteArray.length; i++) {
      boundaryByteArray[i] = chunkResponseByteBuffer.get();
    }

    // verify the int value of the bytes split between the two chunks
    final int boundaryInt = ByteBuffer.wrap(boundaryByteArray).getInt();
    final int expectedBoundaryIntValue = lastCompleteValueFromChunks + 1;
    assertThat(boundaryInt).isEqualTo(expectedBoundaryIntValue);

    // read out remaining ints from chunk 2 and verify
    for (int i = 1; i <= intsInSecondChunk; i++) {
      lastCompleteValueFromChunks = chunkResponseByteBuffer.getInt();
      assertThat(lastCompleteValueFromChunks).isEqualTo(i + expectedBoundaryIntValue);
    }

    // assert that the last int read matches the last int in the source array
    assertThat(lastCompleteValueFromChunks).isEqualTo(source[sourceLen - 1]);

    // confirm we have exhausted the chunk byte buffer
    assertThat(chunkResponseByteBuffer.remaining()).isEqualTo(0);
  }
}
