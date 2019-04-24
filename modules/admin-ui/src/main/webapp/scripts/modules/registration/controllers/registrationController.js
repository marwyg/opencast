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
'use strict';

// Controller for creating a new event. This is a wizard, so it implements a state machine design pattern.
angular.module('adminNg.controllers')
.controller('RegistrationCtrl', ['$scope', '$timeout', 'Table', 'AdopterRegistrationStates', 'AdopterRegistrationResource', 'CountryResource',
  'NewEventStates', 'NewEventResource', 'EVENT_TAB_CHANGE', 'Notifications', 'Modal',
  function ($scope, $timeout, Table, AdopterRegistrationStates, AdopterRegistrationResource, CountryResource,
      NewEventStates, NewEventResource, EVENT_TAB_CHANGE, Notifications, Modal) {
    $scope.state = AdopterRegistrationStates.getInitialState($scope.$parent.resourceId);
    $scope.states = AdopterRegistrationStates.get($scope.$parent.resourceId);
    // TODO: CountryResource is in fact a service, but should be a resource. Look at countryResource.js for more information
    $scope.countries = CountryResource.getCountries();

    $scope.adopter = new AdopterRegistrationResource();
    // TODO: check if already registered and fetch data. Set $scope.registered
    // TODO: You need to implement the Endpoint first
    //    AdopterRegistrationResource.get({}, function(response) {}});
    $scope.registered = false;

    $scope.nextState = function (inputAction) {
      if($scope.state == 'form' && $scope.adopterRegistrationForm.$invalid
          && (inputAction == 1 || inputAction == 3)) {
        return;
      }
      $scope.state = $scope.states[$scope.state]["nextState"][inputAction];

      if($scope.state == "close"){
        $scope.close();
      } else if($scope.state == "save") {
        $scope.save();
      } else if($scope.state == "update") {
        $scope.updateProfile();
      } else if($scope.state == "delete") {
        $scope.deleteProfile();
      }
    }

    $scope.save = function () {
      // TODO: Perform real POST-Request to save adopter
      // TODO: Endpoint needs to be implemented too
      if($scope.adopterRegistrationForm.$valid) {
        $timeout(function() {
          $scope.nextState(0);
        }, 2000);
      }
      //AdopterRegistrationResource.save($scope.adopter, function($response) {});
    }

    $scope.updateProfile = function () {
      // TODO: Perform real PUT-Request to save adopter
      // TODO: Endpoint needs to be implemented too
      if($scope.adopterRegistrationForm.$valid) {
        $timeout(function() {
          var success = true;
          if(success) {
            Notifications.add('success', 'ADOPTER_PROFILE_UPDATED');
          } else {
            Notifications.add('error', 'ADOPTER_PROFILE_NOT_UPDATED');
          }
          $scope.nextState(0);
        }, 2000);
      }
      //AdopterRegistrationResource.update($scope.adopter, function($response) {});
    }

    $scope.deleteProfile = function () {
      // TODO: Perform real DELETE-Request to save adopter
      // TODO: Endpoint needs to be implemented too
      $timeout(function() {
        console.log("delete profile")
        var success = true;
        if(success) {
          Notifications.add('success', 'ADOPTER_PROFILE_DELETED');
        } else {
          Notifications.add('error', 'ADOPTER_PROFILE_NOT_DELETED');
        }
        $scope.nextState(0);
      }, 2000);
      //AdopterRegistrationResource.delete($scope.adopter, function($response) {});
    }

    $scope.close = function () {
      Modal.$scope.close();
    };
  }]);
