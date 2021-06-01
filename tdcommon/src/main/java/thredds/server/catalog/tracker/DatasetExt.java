/* Copyright */
package thredds.server.catalog.tracker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Formatter;

/**
 * TrackedDataset, externalized by ConfigCatalogExtProto
 *
 * @author caron
 * @since 3/28/2015
 */
public class DatasetExt {
  static public int total_count = 0;
  static public long total_nbytes = 0;

  static private final boolean showParsedXML = false;

  long catId;
  // Dataset ds;
  String ncml;
  String restrictedAccess;

  public String getNcml() {
    return ncml;
  }

  public String getRestrictAccess() {
    return restrictedAccess;
  }

  public DatasetExt() {}

  public DatasetExt(long catId, String restrictedAccess, String ncml) {
    this.catId = catId;
    this.restrictedAccess = restrictedAccess;
    this.ncml = ncml;
  }

  @Override
  public String toString() {
    Formatter f = new Formatter();
    f.format("DatasetTrackerInfo{ catId=%d, restrict=%s", catId, restrictedAccess);
    if (ncml != null)
      f.format("%n%s%n", ncml);
    f.format("}");
    return f.toString();
  }

  public byte[] toProtoBytes() {
    ConfigCatalogExtProto.Dataset.Builder builder = ConfigCatalogExtProto.Dataset.newBuilder();
    builder.setCatId(catId);
    builder.setName("");
    if (restrictedAccess != null)
      builder.setRestrict(restrictedAccess);
    if (ncml != null)
      builder.setNcml(ncml);

    ConfigCatalogExtProto.Dataset index = builder.build();

    byte[] protoBytes = index.toByteArray();

    total_count++;
    total_nbytes += protoBytes.length + 4;

    return protoBytes;
  }

  public void fromProtoBytes(byte[] b) throws InvalidProtocolBufferException {
    ConfigCatalogExtProto.Dataset pDataset = ConfigCatalogExtProto.Dataset.parseFrom(b);
    this.catId = pDataset.getCatId(); // LOOK not used
    if (pDataset.getRestrict().length() > 0)
      restrictedAccess = pDataset.getRestrict();
    if (pDataset.getNcml().length() > 0)
      ncml = pDataset.getNcml();
  }
}
