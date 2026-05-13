var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('UserConsoleController', function($scope, $http, menuService, dataBus) {
    $scope.id = 3;
    $scope.visible = false;
    $scope.currentApiUsers = null;
    $scope.currentJobUsers = null;

    $scope.$on(menuService.menuToggleEvent, function(){
        $scope.visible = ($scope.id == dataBus.data);
    });

    $scope.getCurrentApiUsers = function() {
        $http.get(baseAdminApi + "/currentApiUsers").then(function(response){
            $scope.currentApiUsers = response.data;
            $scope.resetApiUser();
        }, function(response){
            console.log("Failed to retrieve list of current Api users!");
        });
    };

    $scope.getCurrentJobUsers = function() {
        $http.get(baseAdminApi + "/currentJobUsers").then(function(response){
            $scope.currentJobUsers = response.data;
            $scope.resetJobUser();
        }, function(response){
            console.log("Failed to retrieve list of current Job users!")
        });
    };

    $scope.createApiUser = function() {
        if (this.apiUserName == null || this.apiUserName == '') {
            alert("A name is required!");
        }
        else {
            $http.post(baseAdminApi + "/createApiUser?name=" + this.apiUserName + "&desc=" + this.apiUserDesc + "&admin=" + this.apiUserAdmin)
                .then(function(response){
                    var data = response.data;
                    if (data) {
                        alert(data.message);
                        if (data.success) {
                            $scope.getCurrentApiUsers();
                            $scope.resetApiUser();
                        }

                    }
                    else {
                        alert("Failed to add Api User!")
                    }
                }, function(response){
                console.log("Failed to add Api User, invalid response from Admin Api.");
            });
        }
    };

    $scope.resetApiUser = function() {
        this.apiUserName = '';
        this.apiUserDesc = '';
        this.apiUserAdmin = false;
    };

    $scope.deleteApiUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteApiUser?id=" + id)
                .then(function(response){
                    var data = response.data;
                    if (data) {
                        alert(data.message);
                        if (data.success) $scope.getCurrentApiUsers();
                    }
                }, function(response){
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
                .then(function(response){
                    var data = response.data;
                    if (data) {
                        alert(data.message);
                        if (data.success) {
                            $scope.getCurrentJobUsers();
                            $scope.resetJobUser();
                        }
                    }
                }, function(response){
                console.log("Failed to create Job User, invalid response from Admin Api");
            });
        }
    };

    $scope.resetJobUser = function() {
        this.jobEmail = '';
        this.jobPassword = '';
        this.jobFirstName = '';
        this.jobLastName = '';
        this.jobAdmin = false;
    };

    $scope.deleteJobUser = function(id) {
        if (id != null && confirm("Are you sure you want to delete this user?")) {
            $http.post(baseAdminApi + "/deleteJobUser?id=" + id).then(function(response){
                var data = response.data;
                if (data) {
                    alert(data.message);
                    if (data.success) $scope.getCurrentJobUsers();
                }
            }, function(response){
                console.log("Failed to delete Job User, invalid response from Admin Api");
            });
        }
    };

    (function init() {
        $scope.getCurrentApiUsers();
        $scope.getCurrentJobUsers();
    })();

});