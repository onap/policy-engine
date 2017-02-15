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
app.controller('riskTypeDictGridController', function ($scope, SafePolicyService,modalService,PapUrlService, $modal, uiGridConstants,Grid){
    $( "#dialog" ).hide();
    
	var papUrl;
	PapUrlService.getPapUrl().then(function(data) {
		var config = data;
		papUrl = config.PAP_URL;
		console.log(papUrl);
		
		SafePolicyService.getRiskTypeDictionaryData(papUrl).then(function (data) {
	        var j = data;
	        $scope.data = JSON.parse(j.data);
	        console.log($scope.data);
	        $scope.riskTypeDictionaryDatas = JSON.parse($scope.data.riskTypeDictionaryDatas);
	        console.log($scope.riskTypeDictionaryDatas);
	    }, function (error) {
	        console.log("failed");
	    });
	});
    
    $scope.riskTypeDictionaryGrid = {
        data : 'riskTypeDictionaryDatas',
        enableFiltering: true,
        exporterCsvFilename: 'RiskType.csv',
        enableGridMenu: true,
        enableSelectAll: true,
        columnDefs: [{
            field: 'id', enableFiltering: false, headerCellTemplate: '' +
            '<button id=\'New\' ng-click="grid.appScope.createNewRiskType()" class="btn btn-success">' + 'Create</button>',
            cellTemplate:
            '<button  type="button"  class="btn btn-primary"  ng-click="grid.appScope.editRiskTypeWindow(row.entity)"><i class="fa fa-pencil-square-o"></i></button> ' +
            '<button  type="button"  class="btn btn-danger"  ng-click="grid.appScope.deleteRiskType(row.entity)" ><i class="fa fa-trash-o"></i></button> ',  width: '8%'
        },
            { field: 'riskName', displayName : 'Risk Type Name' },
            { field: 'description', width: '20%' },
            {field: 'userCreatedBy.userName', displayName : 'Created By'},
            {field: 'userModifiedBy.userName', displayName : 'Modified By' },
            {field: 'createdDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\''},
            {field: 'modifiedDate',type: 'date', cellFilter: 'date:\'yyyy-MM-dd\''}
        ],
        enableColumnResize : true,
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
        modalService.popupConfirmWin("Confirm","You are about to delete the Ecomp Name  "+data.riskType+". Do you want to continue?",
            function(){
                var uuu = papUrl + "/ecomp/sp_dictionary/remove_riskType.htm";
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