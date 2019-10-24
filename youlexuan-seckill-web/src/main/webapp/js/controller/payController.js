 //购物车控制层
app.controller('payController' ,function($scope,$controller,$location,payService){
	
	$controller('baseController',{$scope:$scope});//继承

	//生成二维码
	$scope.createNative=function () {
		payService.createNative().success(
			function (response) {
				if (response){
					$scope.out_trade_no = response.out_trade_no;
					$scope.total_fee = response.total_fee;
					var qr = new QRious({
						element:document.getElementById("qrious"),
						size:200,
						level:"H",
						value:response.qrcode
					})
				}
				queryPayStatus(response.out_trade_no);//查询支付状态
			}
		)
	}


	//查询支付状态
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{
					if(response.message=='二维码超时'){
						location.href="payTimeOut.html";
					}else{
						location.href="payfail.html";
					}
				}
			}
		);
	}

	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}

});	