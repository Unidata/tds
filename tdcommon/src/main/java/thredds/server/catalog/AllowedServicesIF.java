/*
 * Copyright (c) 1998-2026 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import thredds.client.catalog.Service;
import thredds.client.catalog.ServiceType;
import ucar.nc2.constants.FeatureType;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Describe
 *
 * @author caron
 * @since 3/16/2016.
 */
public interface AllowedServicesIF {
  Service getStandardServices(FeatureType featType);

  boolean isAThreddsDataset(String filename);

  /**
   * Find a service declared in the root catalog, by name.
   *
   * @param name the service name
   * @return the global service, or null if there is none with that name
   */
  @Nullable
  Service findGlobalService(String name);

  /** The service types that are currently enabled server wide. */
  Set<ServiceType> getGloballyEnabledServiceTypes();
}
