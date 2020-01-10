package org.opencastproject.adopterstatistics.registration;

import com.google.gson.Gson;
import org.opencastproject.security.api.Organization;
import org.opencastproject.util.EqualsUtil;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * JPA-annotated registration form object.
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "statistics_registration")
@NamedQueries({
        @NamedQuery(name = "Form.findByUsername", query = "Select f FROM Form f where f.username = :username"),
        @NamedQuery(name = "Form.findByAdopterkey", query = "Select f FROM Form f where f.adopterkey = :adopterkey") })
public class Form {

  @Id
  @Column(name = "user", length = 128)
  private String userName;

  @Column(name = "adopter_key")
  private String adopterKey;

  @Column(name = "organisation")
  private String organisationName = "Example University";

  @Column(name = "department")
  private String departmentName = "Mathematics and Computer Science";

  @Column(name = "first_name")
  private String firstName = "Max";

  @Column(name = "last_name")
  private String lastName = "Mustermann";

  @Column(name = "email")
  private String email = "max.mustermann@qweewqqweewq.de";

  @Column(name = "country")
  private String country = "DE";

  @Column(name = "postal_code")
  private String postalCode = "12345";

  @Column(name = "city")
  private String city = "Exampletown";

  @Column(name = "street")
  private String street = "Main Street";

  @Column(name = "street_no")
  private String streetNo = "1A";

  @Column(name = "contact_me")
  private boolean contactMe = false;

  @Column(name = "allows_statistics")
  private boolean allowsStatistics = true;

  @Column(name = "allows_error_reports")
  private boolean allowsErrorReports = true;

  @Column(name = "allows_tech_data")
  private boolean allowsTechData = true;


  /**
   * No-arg constructor needed by JPA
   */
  public Form() {

  }

  public Form(String adopterKey)  {
    this.adopterKey = adopterKey;
  }

  /**
   * Constructor with all parameters.
   *
   * @param userName The name of the user of the registration.
   * @param adopterKey The world wide unique key for this specific user.
   * @param organisationName Organisation name.
   * @param departmentName Department name.
   * @param firstName First name of the user.
   * @param lastName Last name of the user.
   * @param email E-Mail address of the user.
   * @param country The country code (XX).
   * @param postalCode The postal code.
   * @param city The city name.
   * @param street The street name.
   * @param streetNo The street number.
   * @param contactMe Are we allowed to contact the user.
   * @param allowsStatistics Are we allowed to gather information for statistics.
   * @param allowsErrorReports Are we allowed to gather error reports.
   * @param allowsTechData Are we allowed to gather tech data.
   */
  public Form(String userName, String adopterKey,
              String organisationName, String departmentName,
              String firstName, String lastName, String email,
              String country, String postalCode, String city,
              String street, String streetNo, boolean contactMe,
              boolean allowsStatistics, boolean allowsErrorReports,
              boolean allowsTechData) {
    this.userName = userName;
    this.adopterKey = adopterKey;
    this.organisationName = organisationName;
    this.departmentName = departmentName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.country = country;
    this.postalCode = postalCode;
    this.city = city;
    this.street = street;
    this.streetNo = streetNo;
    this.contactMe = contactMe;
    this.allowsStatistics = allowsStatistics;
    this.allowsErrorReports = allowsErrorReports;
    this.allowsTechData = allowsTechData;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAdopterKey() {
    return adopterKey;
  }

  public void setAdopterKey(String adopterKey) {
    this.adopterKey = adopterKey;
  }

  public String getOrganisationName() {
    return organisationName;
  }

  public void setOrganisationName(String organisationName) {
    this.organisationName = organisationName;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getStreetNo() {
    return streetNo;
  }

  public void setStreetNo(String streetNo) {
    this.streetNo = streetNo;
  }

  public boolean isContactMe() {
    return contactMe;
  }

  public void setContactMe(boolean contactMe) {
    this.contactMe = contactMe;
  }

  public boolean isAllowsStatistics() {
    return allowsStatistics;
  }

  public void setAllowsStatistics(boolean allowsStatistics) {
    this.allowsStatistics = allowsStatistics;
  }

  public boolean isAllowsErrorReports() {
    return allowsErrorReports;
  }

  public void setAllowsErrorReports(boolean allowsErrorReports) {
    this.allowsErrorReports = allowsErrorReports;
  }

  public boolean isAllowsTechData() {
    return allowsTechData;
  }

  public void setAllowsTechData(boolean allowsTechData) {
    this.allowsTechData = allowsTechData;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Organization))
      return false;
    return ((Form) obj).getAdopterKey().equals(adopterKey);
  }

  @Override
  public int hashCode() {
    return EqualsUtil.hash(adopterKey);
  }

  @Override
  public String toString() {
    return adopterKey;
  }

  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public static Form fromJson(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, Form.class);
  }

}
