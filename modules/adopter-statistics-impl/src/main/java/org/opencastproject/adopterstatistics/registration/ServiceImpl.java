package org.opencastproject.adopterstatistics.registration;

import org.opencastproject.adopterstatistics.registration.AdopterStatisticsRegistrationService;
import org.opencastproject.security.api.SecurityService;

/**
 * This service is used for registration and retrieving form data for
 * the logged in user in the context of adopter statistics.
 */
public class AdopterStatisticsRegistrationServiceImpl implements AdopterStatisticsRegistrationService {

  /**
   * Security service for getting user information.
   */
  private SecurityService securityService;

  /**
   *
   * @param securityService Instance of this class.
   */
  protected void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

  @Override
  public void registerUser() {

  }

  @Override
  public Object retrieveFormData() {
    return null;
  }

}
