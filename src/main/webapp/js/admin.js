var sage = angular.module('sage', []);

$(document).ready(function() {
    initVerticalMenu();
});

$(function () {
    $('#geocoder-stats-pie').highcharts({
        colors: ['#058DC7', '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4'],
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            margin: [0,0,60,0],
            spacing: [0,0,0,0],
            height: 270
        },
        credits: { enabled: false },
        title: { text: null },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage}%</b>',
            percentageDecimals: 1
        },
        legend: {
            backgroundColor: '#fefefe',
            borderRadius: 0,
            verticalAlign: 'bottom',
            y: -10
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        series: [{
            type: 'pie',
            name: 'Geocoder usage',
            data: [
                ['YahooDao',   60.0],
                ['YahooBossDao',  32.0],
                ['MapQuest',    2.0],
                ['Tiger',     6.0]
            ]
        }]
    });
    $('#api-usage-stats').highcharts({
            chart: {
                zoomType: 'x',
                spacingRight: 20,
                height: 300
            },
            title: {
                text: 'Api Requests Since Deployment'
            },
            subtitle: {
                text: document.ontouchstart === undefined ?
                    'Click and drag in the plot area to zoom in' :
                    'Drag your finger over the plot to zoom in'
            },
            xAxis: {
                type: 'datetime',
                maxZoom: 3600000, // 1 hour
                title: {
                    text: null
                }
            },
            yAxis: {
                title: {
                    text: 'Requests per hour'
                }
            },
            tooltip: {
                shared: true
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    lineWidth: 1,
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
                type: 'area',
                name: 'Requests',
                pointInterval: 3600 * 1000,
                pointStart: Date.UTC(2013, 5, 11),
                data: [12, 0, 2, 2, 5, 5, 0, 0, 0, 0 , 0, 0, 0, 18, 19, 20, 34]
            }]
        });


});
