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

package org.opencastproject.adopterstatistics.registration;

import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The REST endpoint for the {@link AdopterStatisticsRegistrationService} service
 */
@Path("/")
@RestService(name = "AdopterStatisticsRegistrationServiceEndpoint",
        title = "Adopter Statistics Registration Service Endpoint",
        abstractText = "Rest Endpoint for the registration form.",
        notes = {""})
public class AdopterStatisticsRegistrationRestEndpoint  {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(AdopterStatisticsRegistrationRestEndpoint.class);

  /** The rest docs */
  protected String docs;

  /** The service that provides methods for the registration */
  protected AdopterStatisticsRegistrationService registrationService;

  public void setRegistrationService(AdopterStatisticsRegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @GET
  @Path("adopterstatistics/formdata")
  @Produces(MediaType.TEXT_PLAIN)
  @RestQuery(name = "adopterstatistics",
          description = "GETs the form data for the current logged in user",
          reponses = {@RestResponse(description = "Successful retrieved form data.",
                  responseCode = HttpServletResponse.SC_OK),
                  @RestResponse(description = "The underlying service could not output something.",
                          responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR)},
          returnDescription = "GETs the form data for a specific user.")
  public Response retrieveFormData() throws Exception {
    logger.info("Retrieve form data for user");
    return Response.ok().entity(registrationService.retrieveFormData()).build();
  }

/*
  @GET
  @Path("helloname")
  @Produces(MediaType.TEXT_PLAIN)
  @RestQuery(name = "helloname", description = "example service call with parameter",
          restParameters = { @RestParameter(description = "name to output", isRequired = false, name = "name",
                  type = RestParameter.Type.TEXT) },
          reponses = {@RestResponse(description = "Hello or Hello Name", responseCode = HttpServletResponse.SC_OK),
                  @RestResponse(description = "The underlying service could not output something.",
                          responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR) },
          returnDescription = "The text that the service returns.")
  public Response helloName(@FormParam("name") String name) throws Exception {
    logger.info("REST call for Hello Name");
    return Response.ok().entity(adopterStatisticsRegistrationService.registerUser()).build();
  }
 */


  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("docs")
  public String getDocs() {
    return docs;
  }

}
