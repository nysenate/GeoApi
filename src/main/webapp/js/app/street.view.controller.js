var sage = angular.module('sage');

sage.controller("StreetViewController", function($scope, dataBus, uiBlocker, mapService) {
    $scope.visible = false;
    $scope.viewId = "street";
    $scope.streets = [];

    $scope.columnDefs = [
        { "mDataProp": "bldgLoNum", "aTargets":[0]},
        { "mDataProp": "bldgHiNum", "aTargets":[1]},
        { "mDataProp": "street", "aTargets":[2]},
        { "mDataProp": "parity", "aTargets":[3]},
        { "mDataProp": "location", "aTargets":[4]},
        { "mDataProp": "zip5", "aTargets":[5]},
        { "mDataProp": "senate", "aTargets":[6]},
        { "mDataProp": "congressional", "aTargets":[7]},
        { "mDataProp": "assembly", "aTargets":[8]},
        { "mDataProp": "county", "aTargets":[9]},
        { "mDataProp": "town", "aTargets":[10]},
        { "mDataProp": "election", "aTargets":[11]}
    ];

    $scope.overrideOptions = {
        "bStateSave": false,
        "iCookieDuration": 2419200, /* 1 month */
        "bJQueryUI": false,
        "bPaginate": true,
        "bLengthChange": true,
        "bFilter": true,
        "bInfo": true,
        "bDestroy": true,
        "iDisplayLength": 20,
        "sPaginationType" : "full_numbers"
    };

    // Sort by street ( column index 2 )
    $scope.sortDefault = [[2, "asc"]];

    $scope.$on("street", function(){
        $scope.streets = (dataBus.data.streets);
        uiBlocker.unBlock();
    });

    $scope.$on(dataBus.viewHandleEvent, function(){
        $scope.visible = ($scope.viewId == dataBus.viewId);
    });
});