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
.controller('RegistrationCtrl', ['$scope', '$timeout', 'Table', 'NewEventStates', 'NewEventResource', 'EVENT_TAB_CHANGE',
  'Notifications', 'Modal',
  function ($scope, $timeout, Table, NewEventStates, NewEventResource, EVENT_TAB_CHANGE, Notifications, Modal) {
    $scope.state = 'information';
    $scope.submitButtonText = "REGISTRATION.MODAL.CONTINUE";
//    $scope.states = NewEventStates.get();
//    // This is a hack, due to the fact that we need to read html from the server :(
//    // Shall be banished ASAP
//
//    var metadata,
//        accessController,
//        // Reset all the wizard states
//        resetStates = function () {
//          angular.forEach($scope.states, function(state)  {
//            if (angular.isDefined(state.stateController.reset)) {
//              state.stateController.reset({resetDefaults: true});
//            }
//          });
//        };
//
//    angular.forEach($scope.states, function (state) {
//      if (state.stateController.isAccessState) {
//        accessController = state.stateController;
//      } else if (state.stateController.isMetadataState) {
//        // MH-12854 get latest collections (series)
//        state.stateController.reset({resetDefaults: true});
//        metadata = state.stateController;
//      }
//    });
//
//    if (angular.isDefined(metadata) && angular.isDefined(accessController)) {
//      accessController.setMetadata(metadata);
//    }
//
//    $scope.$on(EVENT_TAB_CHANGE, function (event, args) {
//      if (args.old !== args.current && args.old.stateController.isProcessingState) {
//        args.old.stateController.save();
//      }
//
//      if (args.current.stateController.isAccessState) {
//        args.current.stateController.loadSeriesAcl();
//      }
//
//      if (args.current.stateController.isSourceState) {
//        if (!args.current.stateController.defaultsSet) {
//          args.current.stateController.loadCaptureAgents();
//          args.current.stateController.setDefaultsIfNeeded();
//        }
//        if (!args.current.stateController.hasAgents()) {
//          args.current.stateController.ud.type = 'UPLOAD';
//        }
//      }
//    });
//
//    $scope.submit = function () {
//      var messageId, userdata = { metadata: []}, ace = [];
//
//      window.onbeforeunload = function (e) {
//        var confirmationMessage = 'The file has not completed uploading.';
//
//        (e || window.event).returnValue = confirmationMessage;     //Gecko + IE
//        return confirmationMessage;                                //Webkit, Safari, Chrome etc.
//      };
//
//      angular.forEach($scope.states, function (state) {
//
//        var o;
//        if (state.stateController.isMetadataState) {
//          for (o in state.stateController.ud) {
//            if (state.stateController.ud.hasOwnProperty(o)) {
//              userdata.metadata.push(state.stateController.ud[o]);
//            }
//          }
//        } else if (state.stateController.isMetadataExtendedState) {
//          for (o in state.stateController.ud) {
//            if (state.stateController.ud.hasOwnProperty(o)) {
//              userdata.metadata.push(state.stateController.ud[o]);
//            }
//          }
//        } else if (state.stateController.isAccessState) {
//          angular.forEach(state.stateController.ud.policies, function (policy) {
//            if (angular.isDefined(policy.role)) {
//              if (policy.read) {
//                ace.push({
//                  'action' : 'read',
//                  'allow'  : policy.read,
//                  'role'   : policy.role
//                });
//              }
//
//              if (policy.write) {
//                ace.push({
//                  'action' : 'write',
//                  'allow'  : policy.write,
//                  'role'   : policy.role
//                });
//              }
//            }
//
//            angular.forEach(policy.actions.value, function(customAction){
//              ace.push({
//                'action' : customAction,
//                'allow'  : true,
//                'role'   : policy.role
//              });
//            });
//          });
//
//          userdata.access = {
//            acl: {
//              ace: ace
//            }
//          };
//        } else {
//          userdata[state.name] = state.stateController.ud;
//        }
//      });
//
//      NewEventResource.save({}, userdata, function () {
//        $timeout(function () {
//          Table.fetch();
//        }, 500);
//
//        Notifications.add('success', 'EVENTS_CREATED');
//        Notifications.remove(messageId);
//        resetStates();
//        window.onbeforeunload = null;
//      }, function () {
//        Notifications.add('error', 'EVENTS_NOT_CREATED');
//        Notifications.remove(messageId);
//        resetStates();
//        window.onbeforeunload = null;
//      });
//
//      Modal.$scope.close();
//      // add message that never disappears
//      messageId = Notifications.add('success', 'EVENTS_UPLOAD_STARTED', 'global', -1);
//    };
//    
    $scope.adopter = { 
                       "allows_statistics": false,
                       "allows_error_reports": false,
                       "allows_tech_data": false, 
                       "contact_me": false 
                     };
    
    $scope.states = {
        "information": {
          "nextState": {
            0: "skip",
            1: "form"
          },
          "buttons": {
            "submit": true,
            "back": false,
            "skip": true,
            "close": false,
            "submitButtonText": "REGISTRATION.MODAL.CONTINUE"
          }
        },
        "form": {
          "nextState": {
            0: "information",
            1: "save"
          },
          "buttons": {
            "submit": true,
            "back": true,
            "skip": false,
            "close": false,
            "submitButtonText": "SUBMIT"
          }
        },
        "save": {
          "nextState": {
            0: "close",
            1: "error"
          },
          "buttons": {
            "submit": false,
            "back": false,
            "skip": false,
            "close": true,
            "submitButtonText": null
          }
        },
        "skip": {
          "nextState": {
            0: "close",
            1: "error"
          },
          "buttons": {
            "submit": false,
            "back": false,
            "skip": false,
            "close": true,
            "submitButtonText": null
          }
        }
    }
    
    $scope.nextState = function (inputAction) {
      // inputAction is 0 or 1
      if($scope.state == null) {
        // init
        $scope.state = "information";
      } else {
        $scope.state = $scope.states[$scope.state]["nextState"][inputAction];
        if($scope.state == "close"){
          $scope.close();
        } else if($scope.state == "save") {
          $scope.save();
        }
      }
    }
    $scope.save = function () {
      console.log($scope.adopter);
      console.log($scope.adopterRegistrationForm.$valid)
      $scope.state = "form"; //TODO: Delete this
    }

    
    $scope.close = function () {
      Modal.$scope.close();
    };
    
    $scope.skip = function () {
      $scope.state = 'skip';
    };
    
    $scope.back = function() {
      $scope.state = 'information';
      $scope.submitButtonText = "REGISTRATION.MODAL.CONTINUE";
    }
    
    $scope.submitAction = function() {
      if($scope.state == "information") {
        $scope.state = "form";
        $scope.submitButtonText = "SAVE"
        return;
      } else if($scope.state == "form") {
        $scope.state = "save";
      }
    };
    
    $scope.change = function (o) { console.log(o) }
  }]);
