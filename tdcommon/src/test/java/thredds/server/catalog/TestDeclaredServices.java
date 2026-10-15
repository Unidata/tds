/*
 * Copyright (c) 1998-2026 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.Service;
import thredds.client.catalog.ServiceType;
import thredds.client.catalog.builder.CatalogBuilder;
import thredds.client.catalog.builder.DatasetBuilder;
import ucar.nc2.constants.FeatureType;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Test {@link DeclaredServices} */
public class TestDeclaredServices {

  @Test
  public void shouldFlattenSimpleService() {
    assertThat(DeclaredServices.flatten(makeService("odap", ServiceType.OPENDAP))).containsExactly(ServiceType.OPENDAP);
  }

  @Test
  public void shouldFlattenCompoundService() {
    final Service compound =
        makeCompound("all", makeService("odap", ServiceType.OPENDAP), makeService("http", ServiceType.HTTPServer));

    assertThat(DeclaredServices.flatten(compound)).containsExactly(ServiceType.OPENDAP, ServiceType.HTTPServer);
  }

  @Test
  public void shouldFlattenNestedCompoundService() {
    final Service inner = makeCompound("inner", makeService("ncml", ServiceType.NCML));
    final Service outer = makeCompound("outer", inner, makeService("odap", ServiceType.OPENDAP));

    assertThat(DeclaredServices.flatten(outer)).containsExactly(ServiceType.NCML, ServiceType.OPENDAP);
  }

  @Test
  public void shouldFlattenNullService() {
    assertThat(DeclaredServices.flatten(null)).isEmpty();
  }

  @Test
  public void shouldFindLocallyDeclaredService() {
    final Service service = makeService("http", ServiceType.HTTPServer);
    final Dataset ds = makeDataset(service, Collections.singletonMap(Dataset.ServiceName, "http"), null);

    assertThat(DeclaredServices.forDataset(ds, null)).containsExactly(ServiceType.HTTPServer);
  }

  @Test
  public void shouldFindInheritedService() {
    final Service service =
        makeCompound("all", makeService("odap", ServiceType.OPENDAP), makeService("http", ServiceType.HTTPServer));

    final CatalogBuilder catBuilder = new CatalogBuilder();
    catBuilder.setName("test");
    catBuilder.addService(service);

    final DatasetBuilder parent = new DatasetBuilder(null);
    parent.setName("parent");
    parent.putInheritedField(Dataset.ServiceName, "all");

    final DatasetBuilder child = new DatasetBuilder(parent);
    child.setName("child");
    child.put(Dataset.UrlPath, "test/child.nc");
    parent.addDataset(child);
    catBuilder.addDataset(parent);

    final Catalog cat = catBuilder.makeCatalog();
    final Dataset childDs = cat.getDatasetsLocal().get(0).getDatasetsLocal().get(0);

    assertThat(DeclaredServices.forDataset(childDs, null)).containsExactly(ServiceType.OPENDAP, ServiceType.HTTPServer);
  }

  @Test
  public void shouldFindGlobalServiceWhenNotInCatalog() {
    final Service global = makeService("odap", ServiceType.OPENDAP);
    final Dataset ds = makeDataset(null, Collections.singletonMap(Dataset.ServiceName, "odap"), null);

    assertThat(DeclaredServices.forDataset(ds, new TestAllowedServices(global, null, null)))
        .containsExactly(ServiceType.OPENDAP);
  }

  @Test
  public void shouldReturnNullForUnresolvableServiceName() {
    final Dataset ds = makeDataset(null, Collections.singletonMap(Dataset.ServiceName, "notAService"), null);

    assertThat(DeclaredServices.forDataset(ds, new TestAllowedServices(null, null, null))).isNull();
  }

  @Test
  public void shouldReturnNullWhenNothingDeclared() {
    final Dataset ds = makeDataset(null, Collections.singletonMap(Dataset.UrlPath, "test/ds.nc"), null);

    assertThat(DeclaredServices.forDataset(ds, new TestAllowedServices(null, null, null))).isNull();
  }

  @Test
  public void shouldUseStandardServicesWhenNoServiceNameDeclared() {
    final Service standard =
        makeCompound("GridServices", makeService("odap", ServiceType.OPENDAP), makeService("wms", ServiceType.WMS));

    final Map<String, Object> flds = new HashMap<>();
    flds.put(Dataset.UrlPath, "test/ds.nc");
    flds.put(Dataset.FeatureType, FeatureType.GRID.toString());
    final Dataset ds = makeDataset(null, flds, null);

    assertThat(DeclaredServices.forDataset(ds, new TestAllowedServices(null, standard, null)))
        .containsExactly(ServiceType.OPENDAP, ServiceType.WMS);
  }

  @Test
  public void shouldNotUseStandardServicesWithoutUrlPath() {
    final Service standard = makeCompound("GridServices", makeService("odap", ServiceType.OPENDAP));
    final Dataset ds =
        makeDataset(null, Collections.singletonMap(Dataset.FeatureType, FeatureType.GRID.toString()), null);

    assertThat(DeclaredServices.forDataset(ds, new TestAllowedServices(null, standard, null))).isNull();
  }

  @Test
  public void shouldNarrowGlobalWhenServiceMissing() {
    final AllowedServicesIF allowed =
        new TestAllowedServices(null, null, EnumSet.of(ServiceType.OPENDAP, ServiceType.HTTPServer));

    assertThat(DeclaredServices.narrowsGlobal(EnumSet.of(ServiceType.HTTPServer), allowed)).isTrue();
  }

  @Test
  public void shouldNotNarrowGlobalWhenAllServicesDeclared() {
    final AllowedServicesIF allowed =
        new TestAllowedServices(null, null, EnumSet.of(ServiceType.OPENDAP, ServiceType.HTTPServer));

    assertThat(DeclaredServices.narrowsGlobal(EnumSet.of(ServiceType.OPENDAP, ServiceType.HTTPServer, ServiceType.WMS),
        allowed)).isFalse();
  }

  @Test
  public void shouldIgnoreNotGatedServicesWhenComparingToGlobal() {
    final AllowedServicesIF allowed =
        new TestAllowedServices(null, null, EnumSet.of(ServiceType.OPENDAP, ServiceType.Catalog, ServiceType.Resolver));

    assertThat(DeclaredServices.narrowsGlobal(EnumSet.of(ServiceType.OPENDAP), allowed)).isFalse();
  }

  @Test
  public void shouldNotNarrowGlobalForNullSet() {
    final AllowedServicesIF allowed = new TestAllowedServices(null, null, EnumSet.of(ServiceType.OPENDAP));

    assertThat(DeclaredServices.narrowsGlobal(null, allowed)).isFalse();
  }

  private static Service makeService(String name, ServiceType type) {
    return new Service(name, "/thredds/" + name + "/", type.toString(), type.getDescription(), null, null, null,
        type.getAccessType());
  }

  private static Service makeCompound(String name, Service... nested) {
    return new Service(name, "", ServiceType.Compound.toString(), ServiceType.Compound.getDescription(), null,
        Arrays.asList(nested), null, ServiceType.Compound.getAccessType());
  }

  private static Dataset makeDataset(@Nullable Service service, Map<String, Object> flds,
      @Nullable String catalogName) {
    final CatalogBuilder catBuilder = new CatalogBuilder();
    catBuilder.setName(catalogName == null ? "test" : catalogName);
    catBuilder.addService(service);

    final DatasetBuilder dsBuilder = new DatasetBuilder(null);
    dsBuilder.setName("ds");
    for (Map.Entry<String, Object> entry : flds.entrySet())
      dsBuilder.put(entry.getKey(), entry.getValue());
    catBuilder.addDataset(dsBuilder);

    final Catalog cat = catBuilder.makeCatalog();
    return cat.getDatasetsLocal().get(0);
  }

  private static class TestAllowedServices implements AllowedServicesIF {
    private final Service globalService;
    private final Service standardService;
    private final Set<ServiceType> globallyEnabled;

    TestAllowedServices(@Nullable Service globalService, @Nullable Service standardService,
        @Nullable Set<ServiceType> globallyEnabled) {
      this.globalService = globalService;
      this.standardService = standardService;
      this.globallyEnabled = globallyEnabled == null ? EnumSet.noneOf(ServiceType.class) : globallyEnabled;
    }

    @Override
    public Service getStandardServices(FeatureType featType) {
      return standardService;
    }

    @Override
    public boolean isAThreddsDataset(String filename) {
      return true;
    }

    @Override
    public Service findGlobalService(String name) {
      return (globalService != null && globalService.getName().equals(name)) ? globalService : null;
    }

    @Override
    public Set<ServiceType> getGloballyEnabledServiceTypes() {
      return globallyEnabled;
    }
  }
}
