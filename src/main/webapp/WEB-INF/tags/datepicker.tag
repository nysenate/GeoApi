<%@tag description="Shared from/to date range picker bound to DashboardController scope" pageEncoding="UTF-8"%>
<div>
    <span>The time frame to view stats is between &nbsp;</span>
    <input ng-model="dateRange.fromMonth" style="width:35px;" min="1" max="12" maxlength="2"
           type="number"/>/
    <input ng-model="dateRange.fromDate" style="width:35px;" min="1" max="31" maxlength="2"
           type="number"/>/
    <input ng-model="dateRange.fromYear" style="width:70px;" min="2024" maxlength="4"
           type="number"/>

    <span>&nbsp; and &nbsp;</span>

    <input ng-model="dateRange.toMonth" style="width:35px;" min="1" max="12" maxlength="2"
           type="number"/>/
    <input ng-model="dateRange.toDate" style="width:35px;" min="1" max="31" maxlength="2"
           type="number"/>/
    <input ng-model="dateRange.toYear" style="width:70px;" min="2024" maxlength="4"
           type="number"/>

    <button ng-click="update()" class="submit" style="width:auto; padding:5px 10px;">
        <span>Update</span>
    </button>
</div>
