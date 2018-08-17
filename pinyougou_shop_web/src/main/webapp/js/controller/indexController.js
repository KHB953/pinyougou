app.controller('shop_indexController',function ($scope,shop_loginService) {
    //显示登录用户名
    $scope.showLoginName=function () {
       shop_loginService.showLoginName().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        )
    }
})