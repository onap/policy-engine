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
app.controller('decisionRainyDayDictGridController', function ($scope, PolicyAppService, modalService, $modal){
    $( "#dialog" ).hide();
    
    PolicyAppService.getData('getDictionary/get_RainyDayDictionaryData').then(function (data) {
    	var j = data;
    	$scope.data = JSON.parse(j.data);
    	console.log($scope.data);
    	$scope.rainyDayDictionaryDatas = JSON.parse($scope.data.rainyDayDictionaryDatas);
    	console.log($scope.rainyDayDictionaryDatas);
    }, function (error) {
    	console.log("failed");
    });

    PolicyAppService.getData('get_LockDownData').then(function(data){
		 var j = data;
		 $scope.data = JSON.parse(j.data);
		 $scope.lockdowndata = JSON.parse($scope.data.lockdowndata);
		 if($scope.lockdowndata[0].lockdown == true){
			 $scope.decisionRainyDayDictionaryGrid.columnDefs[0].visible = false;
			 $scope.gridApi.grid.refresh();
		 }else{
			 $scope.decisionRainyDayDictionaryGrid.columnDefs[0].visible = true;
			 $scope.gridApi.grid.refresh();
		 }
	 },function(error){
		 console.log("failed");
	 });
    $scope.decisionRainyDayDictionaryGrid = {
        data : 'rainyDayDictionaryDatas',
        enableFiltering: true,
        exporterCsvFilename: 'AllowedTreatments.csv',
        enableGridMenu: true,
        enableSelectAll: true,
        columnDefs: [{
            field: 'id', enableFiltering: false, headerCellTemplate: '' +
            '<button id=\'New\' ng-click="grid.appScope.createNewRainyDayDictWindow()" class="btn btn-success">' + 'Create</button>',
            cellTemplate:								
            '<button  type="button"  class="btn btn-primary"  ng-click="grid.appScope.editRainyDayDictWindow(row.entity)"><i class="fa fa-pencil-square-o"></i></button> ' +
            '<button  type="button"  class="btn btn-danger"  ng-click="grid.appScope.deleteRainyDayDict(row.entity)" ><i class="fa fa-trash-o"></i></button> ',  width: '8%'
        	},	
        	{field: 'bbid', displayName : 'Building Block ID', sort: { direction: 'asc', priority: 0 }},
            {field: 'workstep', displayName : 'Work Step' },
            {field: 'treatments', displayName : 'Allowed Treatments'}
        ],
        exporterMenuPdf: false,
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

    $scope.editRainyDayTreatment = null;
    $scope.createNewRainyDayDictWindow = function(){
        $scope.editRainyDayTreatment = null;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_RainyDayDict_popup.html',
            controller: 'editRainyDayDictController',
            resolve: {
                message: function () {
                    var message = {
                        rainyDayDictionaryDatas: $scope.editRainyDayTreatment
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.rainyDayDictionaryDatas=response.rainyDayDictionaryDatas;
        });
    };

    $scope.editRainyDayDictWindow = function(rainyDayDictionaryData) {
        $scope.editRainyDayTreatment = rainyDayDictionaryData;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_RainyDayDict_popup.html',
            controller: 'editRainyDayDictController',
            resolve: {
                message: function () {
                    var message = {
                        rainyDayDictionaryData: $scope.editRainyDayTreatment
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.rainyDayDictionaryDatas = response.rainyDayDictionaryDatas;
        });
    };

    $scope.deleteRainyDayDict = function(data) {
        modalService.popupConfirmWin("Confirm","You are about to delete the Rainy Day Allowed Treatment Dictionary  "+data.allowedTreatments+". Do you want to continue?",
            function(){
                var uuu = "deleteDictionary/decision_dictionary/remove_rainyDay";
                var postData={data: data};
                $.ajax({
                    type : 'POST',
                    url : uuu,
                    dataType: 'json',
                    contentType: 'application/json',
                    data: JSON.stringify(postData),
                    success : function(data){
                        $scope.$apply(function(){$scope.rainyDayDictionaryDatas=data.rainyDayDictionaryDatas;});
                    },
                    error : function(data){
                        console.log(data);
                        modalService.showFailure("Fail","Error while deleting: "+ data.responseText);
                    }
                });

            })
    };


});