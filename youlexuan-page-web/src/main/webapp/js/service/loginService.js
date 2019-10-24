app.service('loginService',function ($http) {
    
    //获取登录用户名
    this.getLoginName=function () {
        return $http.get('../login/name.do');
    }
})