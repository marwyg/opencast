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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO that contains anonymous statistic data of an adopter.
 */
public class StatisticData {

  /**
   * This list represents the number of hosts of an adopter.
   * Each list entry contains the amount of processor cores of a host.
   */
  private List<Host> hosts;

  /** JSON parser */
  private static final Gson gson = new Gson();

  private long jobCount;
  private long eventCount;
  private int seriesCount;
  private long userCount;

  public String jsonify() {
    return gson.toJson(this);
  }

  public List<Host> getHosts() {
    return hosts;
  }

  public void setHosts(List<Host> hosts) {
    this.hosts = hosts;
  }

  public void addHost(Host host) {
    if (this.hosts == null) {
      this.hosts = new ArrayList<>();
    }
    this.hosts.add(host);
  }

  public long getJobCount() {
    return jobCount;
  }

  public void setJobCount(long jobCount) {
    this.jobCount = jobCount;
  }

  public void setEventCount(long eventCount) {
    this.eventCount = eventCount;
  }

  public void setSeriesCount(int seriesCount) {
    this.seriesCount = seriesCount;
  }

  public void setUserCount(long userCount) {
    this.userCount = userCount;
  }

}
