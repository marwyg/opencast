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

package org.opencastproject.adopter.statistic;

import org.opencastproject.adopter.registration.Form;
import org.opencastproject.adopter.registration.Service;
import org.opencastproject.adopter.statistic.dto.GeneralData;
import org.opencastproject.adopter.statistic.dto.Host;
import org.opencastproject.adopter.statistic.dto.StatisticData;
import org.opencastproject.assetmanager.api.AssetManager;
import org.opencastproject.assetmanager.api.query.AQueryBuilder;
import org.opencastproject.series.api.SeriesService;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.userdirectory.JpaUserAndRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/** It collects and sends statistic data of an registered adopter. */
public class ScheduledDataCollector extends TimerTask {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledDataCollector.class);

  //================================================================================
  // OSGi
  //================================================================================

  /** Provides access to the adopter form information */
  private Service adopterFormService;

  /** Provides access to job and host information */
  private ServiceRegistry serviceRegistry;

  /** Provides access to recording information */
  private AssetManager assetManager;

  /** Provides access to series information */
  private SeriesService seriesService;

  /** User and role provider */
  protected JpaUserAndRoleProvider userAndRoleProvider;


  /** OSGi setter for the adopter form service. */
  public void setAdopterFormService(Service adopterFormService) {
    this.adopterFormService = adopterFormService;
  }

  /** OSGi setter for the service registry. */
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  /** OSGi setter for the asset manager. */
  public void setAssetManager(AssetManager assetManager) {
    this.assetManager = assetManager;
  }

  /** OSGi setter for the series service. */
  public void setSeriesService(SeriesService seriesService) {
    this.seriesService = seriesService;
  }

  /** OSGi setter for the user provider. */
  public void setUserAndRoleProvider(JpaUserAndRoleProvider userAndRoleProvider) {
    this.userAndRoleProvider = userAndRoleProvider;
  }

  //================================================================================
  // Properties
  //================================================================================

  /** Provides methods for sending statistic data */
  private Sender sender;

  //================================================================================
  // Scheduler methods
  //================================================================================

  /** Entry point of the scheduler. Configured with the activate parameter at OSGi component declaration. */
  public void start() {
    Timer time = new Timer();
    Sender sender = new Sender();
    ScheduledDataCollector task = new ScheduledDataCollector();
    //time.schedule(task, 0, 1000 * 60 * 60 * 24);
    time.schedule(task, 0, 1000 * 5);
  }

  /** The scheduled method. It collects statistic data around Opencast and sends it via POST request. */
  @Override
  public void run() {
    Form adopter = (Form) adopterFormService.retrieveFormData();
    if (adopter.isRegistered() && adopter.agreedToPolicy()) {

      try {
        String generalDataAsJson = collectGeneralData(adopter);
        sender.send(generalDataAsJson);
      } catch (Exception e) {
        logger.error("Error occurred while processing adopter general data.", e);
      }

      if (adopter.allowsStatistics()) {
        try {
          String statisticDataAsJson = collectStatisticData();
          sender.send(statisticDataAsJson);
        } catch (Exception e) {
          logger.error("Error occurred while processing adopter statistic data.", e);
        }
      }

      if (adopter.allowsErrorReports()) {
        // todo: implement in future
      }

      if (adopter.allowsContacting()) {
        // todo: implement in future
      }
    }
  }

  //================================================================================
  // Data collecting methods
  //================================================================================

  private String collectGeneralData(Form adopterRegistrationForm) {
    GeneralData generalData = new GeneralData(adopterRegistrationForm);
    return generalData.jsonify();
  }

  private String collectStatisticData() throws Exception {
    StatisticData statisticData = new StatisticData();
    serviceRegistry.getHostRegistrations().forEach(host -> statisticData.addHost(new Host(host)));
    statisticData.setJobCount(serviceRegistry.count(null, null));
    AQueryBuilder q = assetManager.createQuery();
    long eventCount = q.select(q.snapshot()).where(q.version().isLatest()).run().getTotalSize();
    statisticData.setEventCount(eventCount);
    statisticData.setSeriesCount(seriesService.getSeriesCount());
    statisticData.setUserCount(userAndRoleProvider.countUsers());
    return statisticData.jsonify();
  }

}
