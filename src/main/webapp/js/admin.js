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
            name: 'Browser share',
            data: [
                ['YahooDao',   60.0],
                ['YahooBossDao',  32.0],
                ['MapQuest',    2.0],
                ['Tiger',     6.0]
            ]
        }]
    });
});
