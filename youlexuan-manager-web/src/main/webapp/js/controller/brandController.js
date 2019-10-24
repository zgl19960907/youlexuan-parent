app.controller('brandController',function ($scope,$controller,brandService) {
    //继承父controller
    $controller('baseController',{$scope:$scope});

    //分页
    $scope.findPage=function (pageNum,pageSize) {
        brandService.findPage().success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        )
    }

    /**
     * @param pageNum
     * @param pageSize
     * 根据条件模糊查询
     */
    $scope.searchEntity={};//定义搜索对象
    $scope.search=function (pageNum,pageSize) {
        brandService.search(pageNum,pageSize,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        )
    }

    $scope.save=function () {
        brandService.save($scope.entity).success(
            function (response) {
                if (response.success){
                    //重新加载
                    $scope.reloadList();
                } else{
                    alert(response.message);
                }
            }
        )
    }

    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }

    //批量删除
    $scope.delet=function () {
        //获取选中的复选框
        brandService.delet($scope.selectIds).success(
            function (response) {
                $scope.reloadList();//刷新列表
            }
        )
    }


})