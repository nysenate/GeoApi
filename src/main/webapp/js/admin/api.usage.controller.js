var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('ApiUsageController', function($scope, $http, dataBus){

    $scope.init = function() {
        this.getUsageStats((+this.from), (+this.to));
    };

    $scope.getUsageStats = function(startTime, endTime) {
        $http.get(baseAdminApi + "/usage?interval=HOUR&from=" + startTime + "&to=" + endTime)
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
        $.each(data, function(i, v) {
            while (next < v.time && next < endDate) {
                seriesData.push(0);
                next += intervalMilli;
            }
            seriesData.push(v.count);
            next += intervalMilli;
        });
        makeApiUsageChart(startDate, seriesData);
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
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
            text: null
        },
        subtitle: {
            text: null
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
            areaspline : {
                fillColor: '#CC333F',
                lineWidth: 1,
                lineColor: '#CC333F',
                marker: {
                    enabled: false
                },
                shadow: false,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },
        series: [{
            type: 'areaspline',
            name: 'Requests',
            pointInterval: 3600 * 1000,
            pointStart: startDate - (4 * 3600000), /* EST Time zone correction ~.~ */
            data: seriesData
        }]
    });
}