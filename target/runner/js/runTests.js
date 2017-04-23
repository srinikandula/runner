/**
 * Created by Srini Kandula on 4/23/17.
 */
var app = angular.module("myApp", [
    'ui.router',
    'ngTable']);

app.controller("myCtrl", function($scope, $http,NgTableParams) {
    $scope.firstName = "John";
    $scope.lastName = "Doe";
    $scope.testClasses = [];
    $scope.testClassesToRun = [];

    var loadTableData = function (tableParams) {
        $scope.loading = true;
        // var pageable = {page:tableParams.page(), size:tableParams.count(), sort:sortProps};
        $http({url:'/tests',method: "GET"})
            .then(function (response) {
                $scope.testClasses = response.data;
            },function (error) {
                $log.debug("error retrieving test names");
            });
    };
    $scope.testTableParams = new NgTableParams({
        page: 1, // show first page
        size: 10,
        count: 10,
        sorting: {
            name: 'asc'
        },
    }, {
        getData: function (params) {
            loadTableData(params);
        }
    });

    $scope.selectTest = function(testName) {
        console.log(JSON.stringify(testName));
    }

});