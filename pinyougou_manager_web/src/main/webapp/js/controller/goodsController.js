 //控制层 
app.controller('goodsController' ,function($scope,$controller ,manager_goodsService,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		manager_goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		manager_goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		manager_goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=manager_goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=manager_goodsService.add( $scope.entity  );//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		manager_goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		manager_goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    //商品列表 商品状态显示数组
    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
    //使用异步加载分类名称
    $scope.itemCatList=[];//商品分类列表
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i =1;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        )
    }
    
    $scope.updatagGoodsStatus=function (status) {
		manager_goodsService.updateGoodsStatus($scope.selectIds,status).success(
			function (response) {
				if (response.success){
					//更新成功就刷新列表
					$scope.reloadList();
					//清空集合
					$scope.selectIds=[]
				} else {
					alert(response.message)
				}
            }
		)
    }
    
});	
