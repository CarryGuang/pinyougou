//��Ʒ��ϸҳ�����Ʋ㣩
app.controller('itemController',function($scope){
	
	//��������
	$scope.addNum=function(x){
		$scope.num= parseInt($scope.num);
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	$scope.specificationItems={};//��¼�û�ѡ��Ĺ��
	//�û�ѡ����
	$scope.selectSpecification=function(name,value){ 
		$scope.specificationItems[name]=value;
		searchSku();//��ȡ sku
		} 
		
	//�ж�ĳ���ѡ���Ƿ��û�ѡ��
	$scope.isSelected=function(name,value){
	if($scope.specificationItems[name]==value){
	return true;
	}else{
	return false;
	} 
}

	//����Ĭ�� SKU
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
	//ƥ����������
	matchObject=function(map1,map2){ 
	for(var k in map1){
		if(map1[k]!=map2[k]){
		return false;
	} 
	}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
	} 
	}
		return true; 
}

	//��ѯ SKU
	searchSku=function(){
		for(var i=0;i<skuList.length;i++ ){
		if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
		$scope.sku=skuList[i];
		return ;
		} 
		} 
		$scope.sku={id:0,title:'--------',price:0};//���û��ƥ���
		}
	//�����Ʒ�����ﳵ
	$scope.addToCart=function(){
		alert('skuid:'+$scope.sku.id);
	}	
		

});