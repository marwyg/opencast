package org.opencastproject.adopterstatistics.registration;

/**
 * API for the repository that handles registration
 * forms for the adopter statistics.
 */
public interface AdopterStatisticsRegistrationRepository {

  /**
   * Saves the registration form data.
   */
  void save();

  /**
   * Deletes an existing registration entry.
   */
  void delete();


  AdopterStatisticsRegistrationForm findByAdopterKey(String adopterKey);

}
