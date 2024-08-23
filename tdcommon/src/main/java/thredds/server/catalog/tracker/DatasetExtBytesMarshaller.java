package thredds.server.catalog.tracker;

import com.google.protobuf.InvalidProtocolBufferException;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

public class DatasetExtBytesMarshaller
    implements BytesWriter<DatasetExt>, BytesReader<DatasetExt>, ReadResolvable<DatasetExtBytesMarshaller> {

  static final DatasetExtBytesMarshaller INSTANCE = new DatasetExtBytesMarshaller();

  private DatasetExtBytesMarshaller() {}

  @Override
  public @NotNull DatasetExtBytesMarshaller readResolve() {
    return INSTANCE;
  }

  @NotNull
  @Override
  public DatasetExt read(Bytes in, @Nullable DatasetExt datasetExt) throws RuntimeException {
    if (datasetExt == null) {
      datasetExt = new DatasetExt();
    }
    int len = in.readInt();
    byte[] b = new byte[len];
    in.read(b);
    try {
      datasetExt.fromProtoBytes(b);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException("Cannot restore dataset from protobuf serialization", e);
    }
    return datasetExt;
  }

  @Override
  public void write(Bytes out, @NotNull DatasetExt datasetExt) {
    // to parts to write - size of protoBytes, then actual protoByte array
    byte[] protoBytes = datasetExt.toProtoBytes();
    out.writeInt(protoBytes.length);
    out.write(protoBytes);
  }
}
