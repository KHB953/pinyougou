app.service("shop_loginService",function ($http) {
    this.loginName=function () {
        return $http.get("../login/name.do");
    }
})