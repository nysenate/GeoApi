var sageAdmin = angular.module('sage-admin', ['sage-common']);
var baseAdminApi = contextPath + "/admin";

sageAdmin.controller('DashboardController', function($scope, $http, dataBus) {
    $scope.id = 1;
    $scope.visible = true;
    $scope.$on('toggleView', function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.getDeploymentStats = function() {
        $http.post(baseAdminApi + "/deployment")
            .success(function(data){
                $scope = angular.extend($scope, data);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    $scope.getUsageStats = function(startTime, endTime) {
        $http.post(baseAdminApi + "/usage?interval=HOUR&from=" + startTime + "&to=" + endTime)
            .success(function(data){
                $scope = angular.extend($scope, data);
                getSeriesData($scope.intervalFrom, $scope.intervalTo, $scope.intervalSizeInMinutes, $scope.intervalUsageCounts);
            })
            .error(function(data){
                console.log("Error retrieving deployment stats! " + data);
            });
    };

    var getSeriesData = function(startDate, endDate, interval, data) {
        var seriesData = [];
        var intervalMilli = interval * 60000;
        var next = startDate;
        console.log("Start: " + new Date(next));
        $.each(data, function(i, v) {
            while (next < v.time && next < endDate) {
                console.log("-- " + new Date(next));
                seriesData.push(0);
                next += intervalMilli;
            }
            console.log("HIT " + new Date(v.time));
            seriesData.push(v.count);
            next += intervalMilli;
        });
        makeApiUsageChart(startDate, seriesData);
    };

    (function(){
        var now = new Date();
        var from = new Date(2013,4,19);
        var toTimestamp = +(now);
        var fromTimestamp = +(from);

        $scope.getDeploymentStats();
        $scope.getUsageStats(fromTimestamp, toTimestamp);
    }());
});

$(document).ready(function() {
    initVerticalMenu();
});

function makeApiUsageChart(startDate, seriesData) {
    $('#api-usage-stats').highcharts({
        chart: {
            zoomType: 'x',
            spacingRight: 20,
            height: 300
        },
        credits: {
            enabled: false
        },
        title: {
            text: 'Api Hourly Usage'
        },
        subtitle: {
            text: 'Highlight an area to zoom in'
        },
        xAxis: {
            type: 'datetime',
            maxZoom: 3600000, // 1 hour
            title: {
                text: null
            },
            tickColor: 'teal',
            tickWidth: 3
        },
        yAxis: {
            title: {
                text: 'Requests per hour'
            },
            min: 0
        },
        tooltip: {
            shared: true
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            area: {
                fillColor: 'orangered',
                lineWidth: 1,
                lineColor: 'orangered',
                marker: {
                    enabled: false
                },
                shadow: true,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },
        series: [{
            type: 'area',
            name: 'Requests',
            pointInterval: 3600 * 1000,
            pointStart: startDate - (4 * 3600000), /* EST Time zone correction ~.~ */
            data: seriesData
        }]
    });
}