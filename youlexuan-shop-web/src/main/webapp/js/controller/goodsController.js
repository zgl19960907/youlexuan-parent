 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	$controller('baseController',{$scope:$scope});//继承

	//一级分类下拉选择框
	//读取一级分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List=response;
			}
		)
	}
	
	//二级分类下拉选择框
	//读取分类
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		//根据选择的值,查询二级分类
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat2List=response;
			}
		)
	})

	// 三级分类下拉选择框
	// 读取三级分类
	$scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
		//根据选择的值,查询二级分类
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List=response;
			}
		)
	})

	//读取模板id  三级分类选择后,读取模板id
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId=response.typeId;//更新模板ID
			}
		)
	})

	//选择模板id,更新品牌对象
	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
		typeTemplateService.findOne(newValue).success(
			function (response) {
				$scope.typeTemplate=response;//获取类型模板
				//格式化json为对象
				$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				//扩展属性 如果没有id,则加载模板中的扩展数据
				if ($location.search()['id']==null){
					$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				}
				//根据模板id得到对应的规格和规格项
				typeTemplateService.findSpecList(newValue).success(
					function (response) {
						$scope.specList=response;
					}
				)
			}
		)
	})

	// $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};
	//保存规格项信息,根据规格是否打钩加工对象
	$scope.updateSpecAttribute=function($event,name,value){
		//判断$scope.entity.goodsDesc.specificationItems对象中是否存在一个attributeName是name的一个对象
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object!=null){
			if ($event.target.checked){
				object.attributeValue.push(value);
			} else{//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				//如果选项都移除了,将此条记录删除
				if (object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}

	//创建SKU列表(深克隆)
	// $scope.entity.goodsDesc.specificationItems =
	// 	[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}]
	// $scope.entity.itemList =
	// 	[{spec: {"机身内存":"16G","网络":"联通3G"},price:0,num:99999,status:'0',isDefault:'0' }]

	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:999,status:'0',isDefault:'0'}];//初始化skulist
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0;i<items.length;i++){
			$scope.entity.itemList=addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
		}
	}
	//添加列值
	addColumn=function(list,columnName,conlumnValues){
		var newList=[];//新的集合加工后的
		for(var i=0;i<list.length;i++){
			var oldRow=list[i];
			for(var j=0;j<conlumnValues.length;j++){
				var newRow=JSON.parse(JSON.stringify(oldRow));//深克隆
				newRow.spec[columnName]=conlumnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}


    //列表中移除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }
    /**
     * 上传图片
     */
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(
        	function(response) {
				if(response.success){//如果上传成功，取出url
					$scope.image_entity.url=response.message;//设置文件地址
				}else{
					alert(response.message);
				}
		}).error(
			function() {
				alert(response.message);
			});
    };
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id'];//获取参数值
		if (id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity = response;
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//显示商品规格属性
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//回显sku列表中的spec
				for (var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}

	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		if (object==null){
			return false;
		} else{
			if (object.attributeValue.indexOf(optionName)>=0){
				return true;
			} else{
				return false;
			}
		}
	}
	
	//保存 
	$scope.save=function(){
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add($scope.entity);//增加
		}
		$scope.entity.goodsDesc.introduction=editor.html();
		serviceObject.success(
			function(response){
				if(response.success){
		        	location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	$scope.entity={'goodsDesc':{'itemImages':[],'specificationItems':[]},'goods':{},'customAttributeItems':[]};

	/*$scope.add=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function (response) {
				if (response.success){
					alert('添加成功');
					$scope.entity={};
					editor.html('');//清空富文本
				} else{
					alert(response.message);
				}
			}
		)
	}*/
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.status = ['待审核','审核通过','驳回','删除'];//状态
	$scope.itemCatList=[];//商品分类
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		)
	}
    
});	