app.controller('indexController',function ($scope,$controller,loginService) {

    $controller('baseController',{$scope:$scope})
    //读取当前登录人
    $scope.getLoginName=function () {
        loginService.getLoginName().success(
            function (response) {
                $scope.userName=response.userName;
                $scope.lastLoginTime=response.lastLoginTime;
            }
        )
    }

})