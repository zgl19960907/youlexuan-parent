var app = angular.module('youlexuan',[]);//用于不需要分页的页面

/*$sce服务写成过滤器(解决高亮显示HTML代码原样输出的问题)
* 因为angularjs为了防止HTML攻击才去的安全机制
* */
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);
