/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import thredds.client.catalog.Dataset;
import thredds.client.catalog.Service;
import thredds.client.catalog.ServiceType;
import ucar.nc2.constants.FeatureType;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Derive the set of services declared for a dataset in a THREDDS Configuration Catalog.
 * <br>
 * The set is used to build the {@code <access>} elements of the generated Client Catalog. The effective
 * (local or inherited) {@code serviceName} of the dataset, with {@link ServiceType#Compound} services
 * flattened. It is used to restrict which services may actually be used against the dataset, in addition
 * to the global service control of {@code threddsConfig.xml}.
 * <br>
 * A {@code null} result means "no information was declared", which callers must treat as "allow everything that is
 * globally enabled", never as "deny everything".
 *
 * @see "thredds.core.DatasetManager#serviceControlOk"
 * @since 5.10
 */
public class DeclaredServices {
  static private final org.slf4j.Logger logCatalogInit = org.slf4j.LoggerFactory.getLogger("catalogInit");

  /** Services that are never gated per dataset, so they are ignored when comparing against the global set. */
  private static final Set<ServiceType> notGated = EnumSet.of(ServiceType.Catalog, ServiceType.Resolver);

  private DeclaredServices() {}

  /**
   * The effective service types declared for this dataset, Compound services flattened.
   * <br>
   * 
   * @param ds the config catalog dataset
   * @param allowedServices used to resolve global and standard services; may be null
   * @return the declared service types, or null if the dataset declares no services that
   *         we can resolve
   */
  @Nullable
  public static Set<ServiceType> forDataset(Dataset ds, @Nullable AllowedServicesIF allowedServices) {
    return forDataset(ds, null, allowedServices);
  }

  /**
   * The effective service types declared for this dataset, Compound services flattened.
   * <br>
   * 
   * @param ds the config catalog dataset
   * @param featTypeFallback use the standard services of this feature type when no serviceName is declared.
   *        Needed for datasetScan and featureCollection, which have no urlPath of their own
   * @param allowedServices used to resolve global and standard services; may be null
   * @return the declared service types, or null if the dataset declares no services that we can resolve
   */
  @Nullable
  public static Set<ServiceType> forDataset(Dataset ds, @Nullable FeatureType featTypeFallback,
      @Nullable AllowedServicesIF allowedServices) {
    Service service = findDeclaredService(ds, featTypeFallback, allowedServices);
    if (service == null)
      return null;

    Set<ServiceType> result = flatten(service);
    return result.isEmpty() ? null : result;
  }

  /** The service types of this service, expanding Compound services recursively. */
  public static Set<ServiceType> flatten(@Nullable Service service) {
    Set<ServiceType> result = EnumSet.noneOf(ServiceType.class);
    flatten(service, result, new HashSet<>());
    return result;
  }

  /**
   * Does this set forbid anything the global configuration currently allows?
   * Used to avoid storing sets that permit everything that is already enabled.
   *
   * @param declared the declared service types, may be null
   * @param allowedServices the global service control; may be null
   * @return true if the declared set is more restrictive than the global one
   */
  public static boolean narrowsGlobal(@Nullable Set<ServiceType> declared,
      @Nullable AllowedServicesIF allowedServices) {
    if (declared == null)
      return false;
    if (allowedServices == null)
      return true;

    for (ServiceType type : allowedServices.getGloballyEnabledServiceTypes()) {
      if (notGated.contains(type))
        continue;
      if (!declared.contains(type))
        return true;
    }
    return false;
  }

  /** The service types that are never gated per dataset. */
  public static Set<ServiceType> getNotGatedServiceTypes() {
    return Collections.unmodifiableSet(notGated);
  }

  /** Externalize a service type set as a list of names, for persistence. */
  public static List<String> toNames(@Nullable Set<ServiceType> types) {
    if (types == null)
      return Collections.emptyList();
    List<String> result = new ArrayList<>(types.size());
    for (ServiceType type : types)
      result.add(type.name());
    return result;
  }

  /** Internalize a list of service type names; null when the list is empty, so that callers fail open. */
  @Nullable
  public static Set<ServiceType> fromNames(@Nullable Collection<String> names) {
    if (names == null || names.isEmpty())
      return null;
    Set<ServiceType> result = EnumSet.noneOf(ServiceType.class);
    for (String name : names) {
      ServiceType type = ServiceType.getServiceTypeIgnoreCase(name);
      if (type == null)
        logCatalogInit.warn("DeclaredServices: unknown persisted service type '{}'", name);
      else
        result.add(type);
    }
    return result.isEmpty() ? null : result;
  }

  @Nullable
  private static Service findDeclaredService(@Nullable Dataset ds, @Nullable FeatureType featTypeFallback,
      @Nullable AllowedServicesIF allowedServices) {
    if (ds == null)
      return null;

    String serviceName = ds.getServiceNameDefault();
    if (serviceName != null) {
      Service service = ds.getServiceDefault(); // resolve against the dataset's own catalog
      if (service == null && allowedServices != null)
        service = allowedServices.findGlobalService(serviceName);
      if (service == null)
        logCatalogInit.warn(
            "DeclaredServices: dataset '{}' names unknown service '{}'; no per-dataset service control applied",
            ds.getName(), serviceName);
      return service;
    }

    if (allowedServices == null)
      return null;

    // try to use standard services for datasets that have a featureType
    FeatureType featType = featTypeFallback;
    if (featType == null && ds.getUrlPath() != null)
      featType = ds.getFeatureType();

    return (featType == null) ? null : allowedServices.getStandardServices(featType);
  }

  private static void flatten(@Nullable Service service, Set<ServiceType> result, Set<String> seen) {
    if (service == null)
      return;

    ServiceType type = service.getType();
    if (type == ServiceType.Compound) {
      if (!seen.add(service.getName())) // guard against cycles
        return;
      for (Service nested : service.getNestedServices())
        flatten(nested, result, seen);

    } else if (type != null) {
      result.add(type);
    }
  }
}
