/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine
 * ================================================================================
 * Copyright (C) 2017, 2019 AT&T Intellectual Property. All rights reserved.
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
app.controller('editDescriptiveScopeController' , function ($scope, $modalInstance, message, UserInfoServiceDS2, Notification){
    if(message.descriptiveScopeDictionaryData==null)
        $scope.label='Add Descriptive Scope',
        $scope.choices = [];
    else{
        $scope.label='Edit Descriptive Scope'
        $scope.disableCd=true;
        $scope.choices = [];
        var headers = message.descriptiveScopeDictionaryData.search;
        var SplitChars = 'AND';
        if (headers.indexOf(SplitChars) >= 0) {
            var splitHeader = headers.split(SplitChars);
            var singleHeader  = splitHeader;
            var splitEqual = ':';
            for(i = 0; i < singleHeader.length; i++){
            	 if (singleHeader[i].indexOf(splitEqual) >= 0) {
                 	var splitValue = singleHeader[i].split(splitEqual);
                 	var key  = splitValue[0];
                 	var value = splitValue[1];
                 	var newItemNo = $scope.choices.length+1;
                 	$scope.choices.push({'id':'choice'+newItemNo, 'option': key , 'number' : value });
                 }
            }
        }else{
        	 var splitEqual = ':';
             if (headers.indexOf(splitEqual) >= 0) {
                 var splitValue = headers.split(splitEqual);
                 var key  = splitValue[0];
                 var value = splitValue[1];
                 $scope.choices.push({'id':'choice'+1, 'option': key , 'number' : value });
             }
        }
    }
	
	/*getting user info from session*/
	var userid = null;
	UserInfoServiceDS2.getFunctionalMenuStaticDetailSession()
	  	.then(function (response) {	  		
	  		userid = response.userid;	  	
	 });
	
    $scope.editDescriptiveScope = message.descriptiveScopeDictionaryData;

    $scope.saveDescriptiveScope = function(descriptiveScopeDictionaryData) {
    	var regex = new RegExp("^[a-zA-Z0-9_]*$");
    	if(!regex.test(descriptiveScopeDictionaryData.scopeName)) {
    		Notification.error("Enter Valid Descriptive Scope Name without spaces or special characters");
    	}else{
    		var finalData = extend(descriptiveScopeDictionaryData, $scope.actions[0]);
    		var uuu = "saveDictionary/descriptive_dictionary/save_descriptive";
    		var postData={descriptiveScopeDictionaryData: finalData, userid: userid};
    		$.ajax({
    			type : 'POST',
    			url : uuu,
    			dataType: 'json',
    			contentType: 'application/json',
    			data: JSON.stringify(postData),
    			success : function(data){
    				$scope.$apply(function(){
    					$scope.descriptiveScopeDictionaryDatas=data.descriptiveScopeDictionaryDatas;});
    				if($scope.descriptiveScopeDictionaryDatas == "Duplicate"){
    					Notification.error("Descriptive Scope Dictionary exists with Same Scope Name.")
    				}else{      
    					console.log($scope.descriptiveScopeDictionaryDatas);
    					$modalInstance.close({descriptiveScopeDictionaryDatas:$scope.descriptiveScopeDictionaryDatas});
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
    
    function extend(obj, src) {
        for (var key in src) {
            if (src.hasOwnProperty(key)) obj[key] = src[key];
        }
        return obj;
    }
    
    $scope.actions = [{"attributes" : $scope.choices}];
    $scope.addNewChoice = function() {
        var newItemNo = $scope.choices.length+1;
        $scope.choices.push({'id':'choice'+newItemNo});
    };
    $scope.removeChoice = function() {
        var lastItem = $scope.choices.length-1;
        $scope.choices.splice(lastItem);
    };

});