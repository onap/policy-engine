/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine
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
app.controller('editOnapNameController' ,  function ($scope, $modalInstance, message, UserInfoServiceDS2, Notification){
    if(message.onapNameDictionaryData==null)
        $scope.label='Add New Onap Name'
    else{
        $scope.label='Edit Onap Name'
        $scope.disableCd=true;
    }
    $scope.editOnapName = message.onapNameDictionaryData;
    
	
	/*getting user info from session*/
	var userid = null;
	UserInfoServiceDS2.getFunctionalMenuStaticDetailSession()
	  	.then(function (response) {	  		
	  		userid = response.userid;	  	
	 });
   
    $scope.saveOnapName = function(onapNameDictionaryData) {
    	var regex = new RegExp("^[a-zA-Z0-9_]*$");
		if(!regex.test(onapNameDictionaryData.onapName)) {
			Notification.error("Enter Valid Onap Name without spaces or special characters");
		}else{
			var uuu = "saveDictionary/onap_dictionary/save_onapName";
			var postData={onapNameDictionaryData: onapNameDictionaryData, userid: userid};
			$.ajax({
				type : 'POST',
				url : uuu,
				dataType: 'json',
				contentType: 'application/json',
				data: JSON.stringify(postData),
				success : function(data){
					$scope.$apply(function(){
						$scope.onapNameDictionaryDatas=data.onapNameDictionaryDatas;});
					if($scope.onapNameDictionaryDatas == "Duplicate"){
						Notification.error("OnapName Dictionary exists with Same Onap Name.")
					}else{      
						console.log($scope.onapNameDictionaryDatas);
						$modalInstance.close({onapNameDictionaryDatas:$scope.onapNameDictionaryDatas});
					}
				},
				error : function(data){
					Notification.error("Error while saving.");
				}
			});
		}
    };

    $scope.close = function() {
        $modalInstance.close();
    };
});