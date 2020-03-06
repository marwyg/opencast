/**
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 *   http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.opencastproject.adopter.statistic.dto;

import org.opencastproject.adopter.registration.Form;

import com.google.gson.Gson;

/** DTO for adopters general data. */
public class GeneralData {

  /** JSON parser */
  private static final Gson gson = new Gson();

  //================================================================================
  // Properties
  //================================================================================

  private final String adopterKey;
  private final String organisationName;
  private final String departmentName;
  private final String firstName;
  private final String lastName;
  private final String country;
  private final String city;
  private final String postalCode;
  private final String street;
  private final String streetNo;
  private final String email;

  public GeneralData(Form adopterRegistrationForm) {
    this.adopterKey = adopterRegistrationForm.getAdopterKey();
    this.organisationName = adopterRegistrationForm.getOrganisationName();
    this.departmentName = adopterRegistrationForm.getDepartmentName();
    this.firstName = adopterRegistrationForm.getFirstName();
    this.lastName = adopterRegistrationForm.getLastName();
    this.country = adopterRegistrationForm.getCountry();
    this.city = adopterRegistrationForm.getCity();
    this.postalCode = adopterRegistrationForm.getPostalCode();
    this.street = adopterRegistrationForm.getStreet();
    this.streetNo = adopterRegistrationForm.getStreetNo();
    this.email = adopterRegistrationForm.getEmail();
  }

  public String jsonify() {
    return gson.toJson(this);
  }

  //================================================================================
  // Getter and Setter
  //================================================================================

  public String getAdopterKey() {
    return adopterKey;
  }

  public String getOrganisationName() {
    return organisationName;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getCountry() {
    return country;
  }

  public String getCity() {
    return city;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getStreet() {
    return street;
  }

  public String getStreetNo() {
    return streetNo;
  }

  public String getEmail() {
    return email;
  }
}
