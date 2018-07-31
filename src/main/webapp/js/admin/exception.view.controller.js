var sageAdmin = angular.module('sage-admin');

sageAdmin.controller('ExceptionViewController', function($scope, $http, dataBus){
    $scope.exceptions = [];

    $scope.init = function() {
        this.getExceptions();
    };

    $scope.getExceptions = function() {
        $http.get(baseAdminApi + "/exception")
            .success(function(data){
                $scope.exceptions = data;
            })
            .error(function(){});
    };

    $scope.hideException = function(id) {
        $http.post(baseAdminApi + "/hideException?id=" + id)
            .success(function(data){
                if (data) {
                    if (data.success) {
                        $scope.getExceptions();
                    }
                    else {
                        alert(data.message);
                    }
                }
            })
            .error(function(){});
    };

    $scope.$on("update", function() {
        $scope.init();
    });

    $scope.init();
});