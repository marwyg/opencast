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
.controller('RegistrationCtrl', ['$scope', '$timeout', 'Table', 'AdopterRegistrationStates',
  'AdopterRegistrationResource', 'CountryResource', 'NewEventStates', 'NewEventResource', 'EVENT_TAB_CHANGE',
  'Notifications', 'Modal', 'AuthService',
  function ($scope, $timeout, Table, AdopterRegistrationStates, AdopterRegistrationResource, CountryResource,
    NewEventStates, NewEventResource, EVENT_TAB_CHANGE, Notifications, Modal, AuthService) {

  $scope.state = AdopterRegistrationStates.getInitialState($scope.$parent.resourceId);

    $scope.states = AdopterRegistrationStates.get($scope.$parent.resourceId);
    // TODO: CountryResource is in fact a service, but should be a resource. Look at countryResource.js
    //       for more information
    $scope.countries = CountryResource.getCountries();

    $scope.adopter = new AdopterRegistrationResource();
    // TODO: check if already registered and fetch data. Set $scope.registered
    // TODO: You need to implement the Endpoint first
    // AdopterRegistrationResource.get({}, function(response) {}});

    //TODO: dies dient nur zu testzwecken ################################################
    $scope.adopter.adopter_key = "C2B1262C424F905466FBCF2ACA1148CF";
    $scope.adopter.organisation_name = "Example University";
    $scope.adopter.department_name = "Mathematics and Computer Science";
    $scope.adopter.gender = "male";
    $scope.adopter.first_name = "Max";
    $scope.adopter.last_name = "Mustermann";
    $scope.adopter.mail = "max.mustermann@qweewqqweewq.de";
    $scope.adopter.country = "DE";
    $scope.adopter.postal_code = "12345";
    $scope.adopter.city = "Exampletown";
    $scope.adopter.street = "Main Street";
    $scope.adopter.street_no = "1A";
    //$scope.adopter.address_additional = null;
    $scope.adopter.contact_me = false;
    $scope.adopter.allows_statistics = true;
    $scope.adopter.allows_error_reports = true;
    $scope.adopter.allows_tech_data = true;
    // ###################################################################################

    $scope.registered = false;

    $scope.nextState = function (inputAction) {
      if($scope.state == 'form' && $scope.adopterRegistrationForm.$invalid
          && (inputAction == 1 || inputAction == 3)) {
        return;
      }
      $scope.state = $scope.states[$scope.state]['nextState'][inputAction];

      if($scope.state == 'close'){
        $scope.close();
      } else if($scope.state == 'save') {
        $scope.save();
      } else if($scope.state == 'update') {
        $scope.updateProfile();
      } else if($scope.state == 'delete') {
        $scope.deleteProfile();
      }
    };

    $scope.save = function () {
      if($scope.adopterRegistrationForm.$valid) {
        AuthService.getUser().$promise.then(function(authObject) {

          //$scope.adopter.adopter_key = generateBase64($scope, authObject);

          AdopterRegistrationResource.save($scope.adopter,
            function ($response, header) {
              // success callback
              $scope.nextState(0);
            }, function(error) {
              // error callback
              $scope.nextState(1);
              console.log(error);
            });
        }).catch(angular.noop);

      }
    };

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
    };

    $scope.deleteProfile = function () {
      // TODO: Perform real DELETE-Request to save adopter
      // TODO: Endpoint needs to be implemented too
      $timeout(function() {
        var success = true;
        if(success) {
          Notifications.add('success', 'ADOPTER_PROFILE_DELETED');
        } else {
          Notifications.add('error', 'ADOPTER_PROFILE_NOT_DELETED');
        }
        $scope.nextState(0);
      }, 2000);
      //AdopterRegistrationResource.delete($scope.adopter, function($response) {});
    };

    $scope.close = function () {
      Modal.$scope.close();
    };
  }]);
