package org.opencastproject.adopterstatistics.registration;

/**
 * API for the Adopter Statistics Registrations
 */
public interface AdopterStatisticsRegistrationService {

  void registerUser();

  Object retrieveFormData();

}
