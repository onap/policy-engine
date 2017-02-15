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
app.controller('addressGroupDictGridController', function ($scope, FWDictionaryService,modalService, $modal, PapUrlService){
    $( "#dialog" ).hide();
    
	var papUrl;
	PapUrlService.getPapUrl().then(function(data) {
		var config = data;
		papUrl = config.PAP_URL;
		console.log(papUrl);
		
		FWDictionaryService.getAddressGroupDictionaryData(papUrl).then(function (data) {
	        var j = data;
	        $scope.data = JSON.parse(j.data);
	        console.log($scope.data);
	        $scope.addressGroupDictionaryDatas = JSON.parse($scope.data.addressGroupDictionaryDatas);
	        console.log($scope.addressGroupDictionaryDatas);
	    }, function (error) {
	        console.log("failed");
	    });
	});
    
    $scope.addressGroupDictionaryGrid = {
        data : 'addressGroupDictionaryDatas',
        enableFiltering: true,
        exporterCsvFilename: 'AddressGroup.csv',
        enableGridMenu: true,
        enableSelectAll: true,
        columnDefs: [{
            field: 'id', enableFiltering: false, headerCellTemplate: '' +
            '<button id=\'New\' ng-click="grid.appScope.createNewFWAddressGroupWindow()" class="btn btn-success">' + 'Create</button>',
            cellTemplate:
            '<button  type="button"  class="btn btn-primary"  ng-click="grid.appScope.editFWAddressGroupWindow(row.entity)"><i class="fa fa-pencil-square-o"></i></button> ' +
            '<button  type="button"  class="btn btn-danger"  ng-click="grid.appScope.deleteFWAddressGroup(row.entity)" ><i class="fa fa-trash-o"></i></button> ',  width: '8%'
        },{ field: 'groupName', displayName : 'Group Name'},
            { field: 'prefixList', displayName : 'Prefix List' },
            { field: 'description' }
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


    $scope.editAddressGroup = null;
    $scope.createNewFWAddressGroupWindow = function(){
        $scope.editAddressGroup = null;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_FWAddressGroup_popup.html',
            controller: 'editFWAddressGroupController',
            resolve: {
                message: function () {
                    var message = {
                        addressGroupDictionaryDatas: $scope.editAddressGroup
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.addressGroupDictionaryDatas=response.addressGroupDictionaryDatas;
        });
    };

    $scope.editFWAddressGroupWindow = function(addressGroupDictionaryData) {
        $scope.editAddressGroup = addressGroupDictionaryData;
        var modalInstance = $modal.open({
        	backdrop: 'static', keyboard: false,
            templateUrl : 'add_FWAddressGroup_popup.html',
            controller: 'editFWAddressGroupController',
            resolve: {
                message: function () {
                    var message = {
                        addressGroupDictionaryData: $scope.editAddressGroup
                    };
                    return message;
                }
            }
        });
        modalInstance.result.then(function(response){
            console.log('response', response);
            $scope.addressGroupDictionaryDatas = response.addressGroupDictionaryDatas;
        });
    };

    $scope.deleteFWAddressGroup = function(data) {
        modalService.popupConfirmWin("Confirm","You are about to delete the Address Group  "+data.groupName+". Do you want to continue?",
            function(){
                var uuu = papUrl + "/ecomp/fw_dictionary/remove_AddressGroup.htm";
                var postData={data: data};
                $.ajax({
                    type : 'POST',
                    url : uuu,
                    dataType: 'json',
                    contentType: 'application/json',
                    data: JSON.stringify(postData),
                    success : function(data){
                        $scope.$apply(function(){$scope.addressGroupDictionaryDatas=data.addressGroupDictionaryDatas;});
                    },
                    error : function(data){
                        console.log(data);
                        modalService.showFailure("Fail","Error while deleting: "+ data.responseText);
                    }
                });

            })
    };
});