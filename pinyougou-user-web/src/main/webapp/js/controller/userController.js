//控制层 
app.controller('userController', function($scope, userService) {

	// 注册
	$scope.reg = function() {
		if ($scope.entity.password != $scope.password) {
			alert("两次输入的密码不一致，请重新输入");
			return;
		}

		userService.add($scope.entity,$scope.smscode).success(function(response) {
			alert(response.message);
		});
	}
	
	
	
	$scope.btnMsg = "获取短信验证码";
	var active = true;
	var second = 60; // 倒计时60秒
	var secondInterval;

	$scope.sendCode=function(){
			if($scope.entity.phone==null || $scope.entity.phone == ""){
				alert("请输入手机号！");
				return ;
			}	
			
			if(active == false) {
				return;
			}
			//发送验证码
			userService.sendCode($scope.entity.phone).success(
					function(response){
						if(response.message=='亲,验证码发送成功'){
							// 显示倒计时按钮，60秒后，允许重新发送 
							active = false;
							secondInterval = setInterval(function() {
								if(second < 0) {
									// 倒计时结束，允许重新发送 
									$scope.btnMsg = "重发验证码";
									// 强制更新视图
									$scope.$digest();
									active = true;
									second = 60;
									// 关闭定时器
									clearInterval(secondInterval);
									secondInterval = undefined;
								} else {
									// 继续计时
									$scope.btnMsg = second + "秒后重发";
									// 强制更新视图
									$scope.$digest();
									second--;
								}
							}, 1000);
						}else{
							alert(response.message);
						}
					}				
			);
		}
});
