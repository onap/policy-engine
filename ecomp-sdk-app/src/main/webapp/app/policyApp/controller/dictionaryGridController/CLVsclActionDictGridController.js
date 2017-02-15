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
app.controller('vsclActionDictGridController', function ($scope, PapUrlService, CLDictionaryService,modalService, $modal){
    $( "#dialog" ).hide();
    
	var papUrl;
	PapUrlService.getPapUrl().then(function(data) {
		var config = data;
		papUrl = config.PAP_URL;
		console.log(papUrl);
		
	    CLDictionaryService.getVSCLActionDictionaryData(papUrl).then(function (data) {
	        var j = data;
	        $scope.data = JSON.parse(j.data);
	        console.log($scope.data);
	        $scope.vsclActionDictionaryDatas = JSON.parse($scope.data.vsclActionDictionaryDatas);
	        console.log($scope.vsclActionDictionaryDatas);
	    }, function (error) {
	        console.log("failed");
	    });
	    
	});

    $scope.vsclActionDictionaryGrid = {
        data : 'vsclActionDictionaryDatas',
        enableFiltering: true,
        exporterCsvFilename: 'VSCLAction.csv',
        enableGridMenu: true,
        enableSelectAll: true,
        columnDefs: [{
            field: 'id', enableFiltering: false, headerCellTemplate: '' +
            '<button id=\'New\' ng-click="grid.appScope.createvsclActionWindow()" class="btn btn-success">' + 'Create</button>',
            cellTemplate:
            '<button  type="button"  class="btn btn-primary"  ng-click="grid.appScope.editvsclActionWindow(row.entity)"><i class="fa fa-pencil-square-o"></i></button> ' +
            '<button  type="button"  class="btn btn-danger"  ng-click="grid.appScope.deletevsclAction(row.entity)" ><i class="fa fa-trash-o"></i></button> ',  width: '8%'
        },
            { field: 'vsclaction', displayName : 'VSCL Action'},
            { field: 'description'},
            {field: 'userCreatedBy.userName', displayName : 'Created By'},
            {field: 'userModifiedBy.userName', displayName : 'Modified By'},
            {field: 'createdDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\'' },
            {field: 'modifiedDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\'' }
        ],
        exporterPdfDefaultStyle: {fontSize: 9},
        exporterPdfTableStyle: {margin: [30, 30, 30, 30]},
        exporterPdfTableHeaderStyle: {fontSize: 10, bold: true, italics: true, color: 'red'},
        exporterPdfHeader: { text: "My Header", style: 'headerStyle' },
        exporterPdfFooter: function ( currentPage, pageCount ) {
       	 return { text: currentPage.toString() + ' of ' + pageCount.toString(), style: 'footerStyle' };
        },
        exporterPdfCustomFormatter: function ( docDefinition ) {
       	 docDefinition.styles.headerStyle = { fontSize: 22, bold: true };
       	 docDefinition.styles.footerStyle = { fontSize: 10, bold: true };
       	 return docDefinition;
        },
        exporterPdfOrientation: 'portrait',
        exporterPdfPageSize: 'LETTER',
        exporterPdfMaxGridWidth: 500,
        exporterCsvLinkElement: angular.element(document.querySelectorAll(".custom-csv-link-location")),
        onRegisterApi: function(gridApi){
        	$scope.gridApi = gridApi;
        }
    };

    $scope.editvsclAction = null;
    $scope.createvsclActionWindow = function(){
        $scope.editvsclAction = null;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_vsclAction_popup.html',
            controller: 'editVsclActionController',
            resolve: {
                message: function () {
                    var message = {
                        vsclActionDictionaryDatas: $scope.editvsclAction
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.vsclActionDictionaryDatas=response.vsclActionDictionaryDatas;
        });
    };

    $scope.editvsclActionWindow = function(vsclActionDictionaryData) {
        $scope.editvsclAction = vsclActionDictionaryData;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_vsclAction_popup.html',
            controller: 'editVsclActionController',
            resolve: {
                message: function () {
                    var message = {
                        vsclActionDictionaryData: $scope.editvsclAction
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.vsclActionDictionaryDatas = response.vsclActionDictionaryDatas;
        });
    };

    $scope.success = false;
    $scope.deletevsclAction = function(data) {
    	var uuu = "searchDictionary.htm";
    	var postData={data: data, type: "clVSCL"};
    	var searchString = "\n";
    	$.ajax({
    		type : 'POST',
    		url : uuu,
    		dataType: 'json',
    		contentType: 'application/json',
    		data: JSON.stringify(postData),
    		success : function(resultList){
    			$scope.$apply(function(){
    				$scope.list =resultList.result;});
    			$scope.searchData = JSON.stringify(resultList.result);
    			$scope.searchDatas = JSON.parse($scope.searchData);	   
    			$scope.success = true;
    			var i;
    			if($scope.searchDatas.length > 0){
    				for(i = 0 ; i < $scope.searchDatas.length; i++){
    					searchString += $scope.searchDatas[i].name + "\n";
    				}	
    			}else{
    				searchString += "No Policies is Using this Value"
    			}

    			console.log($scope.list);
    			if($scope.success){
    				modalService.popupConfirmWin("Confirm","You are about to delete the Vscl Action Item  "+data.vsclaction+". \n "+searchString+" \n Do you want to continue?",
    						function(){
    					var uuu = papUrl + "/ecomp/cl_dictionary/remove_VsclAction.htm";
    					var postData={data: data};
    					$.ajax({
    						type : 'POST',
    						url : uuu,
    						dataType: 'json',
    						contentType: 'application/json',
    						data: JSON.stringify(postData),
    						success : function(data){
    							$scope.$apply(function(){$scope.vsclActionDictionaryDatas=data.vsclActionDictionaryDatas;});
    						},
    						error : function(data){
    							console.log(data);
    							modalService.showFailure("Fail","Error while deleting: "+ data.responseText);
    						}
    					});

    				})}
    		},
    		error : function(data){
    			alert("Error while Searching.");
    		}
    	});
    };

});