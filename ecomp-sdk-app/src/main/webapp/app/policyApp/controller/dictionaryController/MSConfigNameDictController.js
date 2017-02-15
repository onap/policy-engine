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
var editMSConfigController =  function ($scope, $modalInstance, message, PapUrlService, UserInfoService, Notification){
    if(message.microServiceCongigNameDictionaryData==null)
        $scope.label='Add Config Name'
    else{
        $scope.label='Edit Config Name'
        $scope.disableCd=true;
    }
    
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
	
    $scope.editMSConfig = message.microServiceCongigNameDictionaryData;

    $scope.saveMSConfig = function(microServiceCongigNameDictionaryData) {
        var uuu = papUrl + "/ecomp/ms_dictionary/save_configName.htm";
        var postData={microServiceCongigNameDictionaryData: microServiceCongigNameDictionaryData, loginId: loginId};
        $.ajax({
            type : 'POST',
            url : uuu,
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(postData),
            success : function(data){
                $scope.$apply(function(){
                    $scope.microServiceCongigNameDictionaryDatas=data.microServiceCongigNameDictionaryDatas;});
                if($scope.microServiceCongigNameDictionaryDatas == "Duplicate"){
                	Notification.error("MS ConfigName Dictionary exists with Same Config Name.")
                }else{      
                	console.log($scope.microServiceCongigNameDictionaryDatas);
                    $modalInstance.close({microServiceCongigNameDictionaryDatas:$scope.microServiceCongigNameDictionaryDatas});
                }
            },
            error : function(data){
                alert("Error while saving.");
            }
        });
    };

    $scope.close = function() {
        $modalInstance.close();
    };
}