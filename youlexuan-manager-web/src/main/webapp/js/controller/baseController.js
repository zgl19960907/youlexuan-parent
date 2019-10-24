app.controller('baseController',function ($scope,$http) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    //重新加载列表 数据
    $scope.reloadList=function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //定义对象保存id集合
    $scope.selectIds = [];
    $scope.updateSelection = function ($event,id) {
        if ($event.target.checked){//如果是被选中,则添加到数组
            $scope.selectIds.push(id);
        } else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);//删除
        }
    }

    /**
     * 参数
     * 1.json串
     * 2.要取json串中的某个key对应的value
     * 思路: json串转成对象,遍历对象取key对应的value进行拼接
     */
    $scope.jsonToStr=function (jsonStr,key) {
        var value="";
        var json = JSON.parse(jsonStr);
        for (var i=0;i<json.length;i++){
            if (i>0){
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    }

})