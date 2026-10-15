/*
 * Copyright (c) 2015-2026 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog.tracker;

import com.google.protobuf.InvalidProtocolBufferException;
import thredds.client.catalog.ServiceType;
import thredds.server.catalog.DeclaredServices;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

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
  List<String> declaredServices; // ServiceType names

  public String getNcml() {
    return ncml;
  }

  public String getRestrictAccess() {
    return restrictedAccess;
  }

  /**
   * The services declared for this dataset in the config catalog.
   *
   * @return the declared service types, or null if none were recorded
   */
  @Nullable
  public Set<ServiceType> getDeclaredServices() {
    return DeclaredServices.fromNames(declaredServices);
  }

  public DatasetExt() {}

  public DatasetExt(long catId, String restrictedAccess, String ncml) {
    this(catId, restrictedAccess, ncml, null);
  }

  public DatasetExt(long catId, String restrictedAccess, String ncml, @Nullable Set<ServiceType> declaredServices) {
    this.catId = catId;
    this.restrictedAccess = restrictedAccess;
    this.ncml = ncml;
    List<String> names = DeclaredServices.toNames(declaredServices);
    this.declaredServices = names.isEmpty() ? null : new ArrayList<>(names);
  }

  @Override
  public String toString() {
    Formatter f = new Formatter();
    f.format("DatasetTrackerInfo{ catId=%d, restrict=%s", catId, restrictedAccess);
    if (declaredServices != null)
      f.format(", services=%s", declaredServices);
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
    if (declaredServices != null)
      builder.addAllDeclaredService(declaredServices);

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
    if (pDataset.getDeclaredServiceCount() > 0)
      declaredServices = new ArrayList<>(pDataset.getDeclaredServiceList());
  }
}
