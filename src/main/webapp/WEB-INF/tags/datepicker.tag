<%@tag description="Shared from/to date range picker bound to DashboardController scope" pageEncoding="UTF-8"%>
<div>
    <span>The time frame to view stats is between &nbsp;</span>
    <input type="date" ng-model="dateRange.from" max="{{maxDate}}"/>
    <span>&nbsp; and &nbsp;</span>
    <input type="date" ng-model="dateRange.to" max="{{maxDate}}"/>

    <button ng-click="update()" class="submit" style="width:auto; padding:5px 10px;">
        <span>Update</span>
    </button>
</div>
