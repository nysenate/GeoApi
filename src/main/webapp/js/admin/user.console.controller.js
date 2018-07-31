var sageAdmin = angular.module('sage-admin', ['sage-common']);

sageAdmin.controller('UserConsoleController', function($scope, $http, menuService, dataBus) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.currentApiUsers = null;
    $scope.currentJobUsers = null;

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.getCurrentApiUsers = function() {
        $http.get(baseAdminApi + "/currentApiUsers").success(function(data){
            $scope.currentApiUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Api users!");
        });
    };

    $scope.getCurrentJobUsers = function() {
        $http.get(baseAdminApi + "/currentJobUsers").success(function(data){
            $scope.currentJobUsers = data;
        }).error(function(data){
            console.log("Failed to retrieve list of current Job users!")
        });
    };

    $scope.createApiUser = function() {
        if (this.apiUserName == null || this.apiUserName == '') {
            alert("A name is required!");
        }
        else {
            $http.post(baseAdminApi + "/createApiUser?name=" + this.apiUserName + "&desc=" + this.apiUserDesc)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                    else {
                        alert("Failed to add Api User!")
                    }
                }).error(function(data){
                console.log("Failed to add Api User, invalid response from Admin Api.");
            });
        }
    };

    $scope.deleteApiUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteApiUser?id=" + id)
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                }).error(function(data){
                console.log("Failed to delete Api User, invalid response from Admin Api");
            });
        }
    };

    $scope.createJobUser = function() {
        if (this.jobEmail == null || this.jobEmail == '' || this.jobPassword == null || this.jobPassword == '') {
            alert("Email and password must be specified!");
        }
        else {
            $http.post(baseAdminApi + "/createJobUser?email=" + this.jobEmail + "&password=" + this.jobPassword
                + "&firstname=" + this.jobFirstName + "&lastname=" + this.jobLastName + "&admin=" + (this.jobAdmin ? "true" : "false"))
                .success(function(data){
                    if (data) {
                        alert(data.message);
                        if (data.success) {
                            $scope.getCurrentJobUsers();
                        }
                    }
                }).error(function(data){
                console.log("Failed to create Job User, invalid response from Admin Api");
            });
        }
    };

    $scope.deleteJobUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteJobUser?id=" + id).success(function(data){
                if (data) {
                    alert(data.message);
                    if (data.success) $scope.getCurrentJobUsers();
                }
            }).error(function(data){
                console.log("Failed to delete Job User, invalid response from Admin Api");
            });
        }
    };

    (function init() {
        $scope.getCurrentApiUsers();
        $scope.getCurrentJobUsers();
    })();

});