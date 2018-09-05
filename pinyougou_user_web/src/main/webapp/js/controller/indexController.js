//index页面控制器
app.controller('indexController',function($scope,user_loginService){
    $scope.showName=function(){
        user_loginService.showName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    }
});


