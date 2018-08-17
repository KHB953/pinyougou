app.service("shop_loginService",function ($http) {
    this.showLoginName=function () {
        return $http.get("../login/name.do");
    }
})