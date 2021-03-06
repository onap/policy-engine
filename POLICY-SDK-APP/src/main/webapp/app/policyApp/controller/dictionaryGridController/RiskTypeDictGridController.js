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
app.controller('riskTypeDictGridController', function ($scope, PolicyAppService, modalService, $modal, uiGridConstants,Grid){
    $( "#dialog" ).hide();
		
    PolicyAppService.getData('getDictionary/get_RiskTypeData').then(function (data) {
    	var j = data;
    	$scope.data = JSON.parse(j.data);
    	console.log($scope.data);
    	$scope.riskTypeDictionaryDatas = JSON.parse($scope.data.riskTypeDictionaryDatas);
    	console.log($scope.riskTypeDictionaryDatas);
    }, function (error) {
    	console.log("failed");
    });
	
    PolicyAppService.getData('get_LockDownData').then(function(data){
		 var j = data;
		 $scope.data = JSON.parse(j.data);
		 $scope.lockdowndata = JSON.parse($scope.data.lockdowndata);
		 if($scope.lockdowndata[0].lockdown == true){
			 $scope.riskTypeDictionaryGrid.columnDefs[0].visible = false;
			 $scope.gridApi.grid.refresh();
		 }else{
			 $scope.riskTypeDictionaryGrid.columnDefs[0].visible = true;
			 $scope.gridApi.grid.refresh();
		 }
	 },function(error){
		 console.log("failed");
	 });
    
    $scope.riskTypeDictionaryGrid = {
        data : 'riskTypeDictionaryDatas',
        enableFiltering: true,
        enableSelectAll: true,
        columnDefs: [{
            field: 'id', enableFiltering: false, headerCellTemplate: '' +
            '<button id=\'New\' ng-click="grid.appScope.createNewRiskType()" class="btn btn-success">' + 'Create</button>',
            cellTemplate:
            '<button  type="button"  class="btn btn-primary"  ng-click="grid.appScope.editRiskTypeWindow(row.entity)"><i class="fa fa-pencil-square-o"></i></button> ' +
            '<button  type="button"  class="btn btn-danger"  ng-click="grid.appScope.deleteRiskType(row.entity)" ><i class="fa fa-trash-o"></i></button> ',  width: '8%'
        },
            { field: 'riskName', displayName : 'Risk Type Name', sort: { direction: 'asc', priority: 0 } },
            { field: 'description', width: '20%' },
            {field: 'userCreatedBy.userName', displayName : 'Created By'},
            {field: 'userModifiedBy.userName', displayName : 'Modified By' },
            {field: 'createdDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\''},
            {field: 'modifiedDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\''}
        ],
        enableColumnResize : true,
        onRegisterApi: function(gridApi){
        	$scope.gridApi = gridApi;
        	$scope.gridApi.core.refresh();
        }
    };

    $scope.editRiskType = null;
    $scope.createNewRiskType = function(){
        $scope.editRiskType = null;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_riskType_popup.html',
            controller: 'editRiskTypeController',
            resolve: {
                message: function () {
                    var message = {
                        riskTypeDictionaryDatas: $scope.editRiskType
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.riskTypeDictionaryDatas=response.riskTypeDictionaryDatas;
        });
    };

    $scope.editRiskTypeWindow = function(riskTypeDictionaryData) {
        $scope.editRiskType = riskTypeDictionaryData;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_riskType_popup.html',
            controller: 'editRiskTypeController',
            resolve: {
                message: function () {
                    var message = {
                        riskTypeDictionaryData: $scope.editRiskType
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.riskTypeDictionaryDatas = response.riskTypeDictionaryData;
        });
    };

    $scope.deleteRiskType = function(data) {
        modalService.popupConfirmWin("Confirm","You are about to delete the Onap Name  "+data.riskType+". Do you want to continue?",
            function(){
                var uuu = "deleteDictionary/sp_dictionary/remove_riskType";
                var postData={data: data};
                $.ajax({
                    type : 'POST',
                    url : uuu,
                    dataType: 'json',
                    contentType: 'application/json',
                    data: JSON.stringify(postData),
                    success : function(data){
                        $scope.$apply(function(){$scope.riskTypeDictionaryDatas=data.riskTypeDictionaryDatas;});
                    },
                    error : function(data){
                        console.log(data);
                        modalService.showFailure("Fail","Error while deleting: "+ data.responseText);
                    }
                });

            })
    };

});