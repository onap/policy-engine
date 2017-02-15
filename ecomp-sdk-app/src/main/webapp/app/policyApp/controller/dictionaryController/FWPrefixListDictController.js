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
var editFWPrefixListController =  function ($scope, $modalInstance, message, Notification, PapUrlService, UserInfoService, Notification){
	$scope.validate = 'false';
    if(message.prefixListDictionaryData==null)
        $scope.label='Add PrefixList'
    else{
        $scope.label='Edit PrefixList'
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
	
    $scope.editPrefixList = message.prefixListDictionaryData;

    $scope.saveFWPrefixList = function(prefixListDictionaryData) {
    	if($scope.validate == 'true'){
    		var uuu = papUrl + "/ecomp/fw_dictionary/save_prefixList.htm";
            var postData={prefixListDictionaryData: prefixListDictionaryData, loginId: loginId};
            $.ajax({
                type : 'POST',
                url : uuu,
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(postData),
                success : function(data){
                    $scope.$apply(function(){
                        $scope.prefixListDictionaryDatas=data.prefixListDictionaryDatas;});
                    if($scope.prefixListDictionaryDatas == "Duplicate"){
                    	Notification.error("FW PrefixList Dictionary exists with Same PrefixList Name.")
                    }else{      
                    	console.log($scope.prefixListDictionaryDatas);
                        $modalInstance.close({prefixListDictionaryDatas:$scope.prefixListDictionaryDatas});
                    }
                },
                error : function(data){
                    alert("Error while saving.");
                }
            });
    	}else{
    		Notification.error('Prefix List Validation is Not Successful');
    	}
        
    };

    $scope.validateFWPrefixList = function(prefixListDictionaryData) {
        var uuu = papUrl + "/ecomp/fw_dictionary/validate_prefixList.htm";
        var postData={prefixListDictionaryData: prefixListDictionaryData};
        $.ajax({
            type : 'POST',
            url : uuu,
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(postData),
            success : function(data){
                $scope.$apply(function(){
                $scope.result=data.result;});
                console.log($scope.result);
                if($scope.result == 'error'){
                	Notification.error('IP not according to CIDR notation');
                }else{
                	$scope.validate = 'true';
                }
            }
        });
    };
    
    $scope.close = function() {
        $modalInstance.close();
    };
}