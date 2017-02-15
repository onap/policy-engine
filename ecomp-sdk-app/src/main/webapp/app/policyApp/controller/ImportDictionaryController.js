/*-
 * ============LICENSE_START=======================================================
 * ECOMP Policy Engine
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

/**
 */
var importDictionaryController =  function ($scope, $modalInstance, message, $http, PapUrlService, UserInfoService){ 
	
	var papUrl;
	PapUrlService.getPapUrl().then(function(data) {
		var config = data;
		papUrl = config.PAP_URL;
		console.log(papUrl);
	});
	
	/*getting user info from session*/
	var loginId = null;
	UserInfoService.getFunctionalMenuStaticDetailSession()
	  	.then(function (response) {	  		
	  		loginId = response.userid;	  	
	 });
	
    $scope.uploadFile = function(files) {
        var fd = new FormData();
        fd.append("file", files[0]);
        
        $http.post(papUrl + "/ecomp/dictionary/import_dictionary.htm/" + loginId, fd,  {
            withCredentials: false,
            headers: {'Content-Type': undefined},
            transformRequest: angular.identity
        }).success().error( );
    };
    
    $scope.close = function() {
        $modalInstance.close();
    };
}