app.service('brandService',function ($http) {

    //分页
    this.findPage=function (pageNum,pageSize) {
        return $http.get('../brand/findPage.do?page='+pageNum+'&rows='+pageSize);
    }

    /**
     * @param pageNum
     * @param pageSize
     * 根据条件模糊查询
     */
    this.search = function (pageNum,pageSize,searchEntiy) {
        return $http.post('../brand/search.do?page='+pageNum+'&rows='+pageSize,searchEntiy);
    }

    this.save=function (entity) {
        var mname="add.do";
        if (entity.id != null){
            mname="update.do";
        }
        return $http.post('../brand/'+mname,entity);
    }

    this.findOne=function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    }

    //批量删除
    this.delet=function () {
        //获取选中的复选框
        return $http.get('../brand/delet.do?ids='+selectIds);
    }

    this.selectOption=function () {
        return $http.get('../brand/selectOption.do')
    }

})