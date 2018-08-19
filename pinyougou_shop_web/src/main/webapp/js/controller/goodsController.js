 //控制层 
app.controller('shop_web_goodsController' ,function($scope,$controller ,goodsService,uploadService,shop_web_itemCatService,typeTemplateService,$location){
	
	$controller('baseController',{$scope:$scope});//继承
	
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
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//添加或者更新
	$scope.save=function(){
		//获取富文本编辑器中内容
        $scope.entity.goodsDesc.introduction=editor.html();

        var objectService;
        if ($scope.entity.goods.id != null){
            objectService=goodsService.update( $scope.entity  )
		} else {
            objectService=goodsService.add( $scope.entity  )
		}

        objectService.success(
			function(response){
				if(response.success){
                    alert(response.message);
                    //没有list列表 保存成功就清空数据
                    $scope.entity={};
                    //清空富文本编辑器
                    editor.html('');
                }else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//图片上传
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
				if (response.success) {
					//上传成功
                    $scope.image_entity.url=response.message;//设置文件地址

				} else {
					//上传失败
					alert(response.message)
				}
            }
		).error(function () {
			alert("上传发生错误,请重试！")
        })
    }

    //定义实体页面
    $scope.entity ={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}
    //添加图片列表
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    }

    //列表中移除图片
	$scope.romove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1)
    }

    //读取商品分类一级列表
	$scope.selectItemCat1List=function () {
		shop_web_itemCatService.findByParentId("0").success(
			function (response) {
				$scope.itemCat1List=response;
            }
		)
    };

	//读取商品分类二级列表
    //$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数。
	$scope.$watch("entity.goods.category1Id",function (newValue, oldValue) {
		shop_web_itemCatService.findByParentId(newValue).success(
			function (response) {
                $scope.itemCat2List=response;
            }
		)
    })

    //读取商品分类三级列表
    $scope.$watch("entity.goods.category2Id",function (newValue, oldValue) {
        shop_web_itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            }
        )
    })

    //三级列表选中后显示模板编号
    $scope.$watch("entity.goods.category3Id",function (newValue, oldValue) {
   		shop_web_itemCatService.findOne(newValue).success(
   			function (response) {
                //更新模板ID
                $scope.entity.goods.typeTemplateId=response.typeId;
            }
		)
    })

    //模板编号显示后，根据模板id更新品牌列表
    $scope.$watch("entity.goods.typeTemplateId",function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                //获取模板
                $scope.typeTemplate=response;
                //获取品牌列表
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);

                // 在用户更新模板ID时，读取模板中的扩展属性 赋给商品的扩展属性 。
				if ($location.search()['id']==null){
					//如果是新增 才执行
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
                }

            }
        );

        // 读取规格列表并包含规格选项里列表
		// 需要[{"id":27,"text":"网络"，options:[{id:93,optionName:"电信4G...},{}],{"id":32,"text":"机身内存"}]的数据
        typeTemplateService.findSpecList(newValue).success(
        	function (response) {
				$scope.specList=response;
            }
		)

    })


    // 将用户选中的选项保存在tb_goods_desc表的specification_items字段中
    // 定义实体方法上写 specificationItems:[] 初始化
	$scope.upadateSpecAttribute=function ($event, name, value) {

		//用户勾选规格选项的时候判断规格对象是否存在；
		// 比如勾选 "移动4G" 就需要判断 entity.goodsDesc.specificationItems 集合中是否存在 "网络制式" 这个对象
		// entity.goodsDesc.specificationItems=[{"attributeName":"网络制式","attributeValue":["移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","4.5寸"]}]
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object != null) {
			if (event.target.checked) {
                //规格对象存在就往attributeValue中添加勾选的项目
                object.attributeValue.push(value)
            } else {
                //用户取消勾选 就得从object.attributeValue 移除规格选项
                object.attributeValue.splice(object.attributeValue.indexOf(value),1)

				//如果规格中的规格选项为空 则把该规格删除
				if (object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object),1)
				}
			}

		} else {
            //规格对象不存在 说明entity.goodsDesc.specificationItems 集合没有
			// 就需要在这个集合中添加对象 {"attributeName":"网络制式","attributeValue":["移动4G"]}
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})

		}



    }


    //商品保存的时候 将SKU列表存到一个集合中
    //创建SKU列表
	$scope.createItemList=function () {
		//声明一个集合，包含一个对象,对象中 初始化一个 spec:{},price,num,status,isDefault 即列表初始化
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}]

		var items=$scope.entity.goodsDesc.specificationItems;
		for (var i = 0;i<items.length;i++){
            $scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue)
		}

    };

    //深克隆 返回一个新的对象 两层嵌套循环
    addColumn=function (list, columnName, columnValues) {
        var  newList=[];

        for (var i = 0;i<list.length;i++){
            var oldRow=list[i];

            for (var j=0;j<columnValues.length;j++) {
                //深克隆 将 oldRow 转成字符串 然后在转成一个对象 赋值给 newRow
                //oldRow 和 newRow 是两个对象 内容一样 这样做到深克隆
                var newRow=JSON.parse(JSON.stringify(oldRow));
                // 再在 newRow 的spec 中添加属性
                newRow.spec[columnName]=columnValues[j];
                newList.push(newRow)
            }
        }

        return newList;
    }


    //商品列表 商品状态显示数组
    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	//使用异步加载分类名称
    $scope.itemCatList=[];//商品分类列表
	$scope.findItemCatList=function () {
        shop_web_itemCatService.findAll().success(
        	function (response) {
				for (var i =1;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		)
    }

    // 查询实体
    $scope.findOne=function () {
		var id = $location.search()['id']
		if (id==null){
			return;
		}

		goodsService.findOne(id).success(
			function (response) {
				//response 是响应的数据
				$scope.entity=response;

				//向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);

                //显示图片列表 查找到的是字符串需要转换
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);

                //扩展属性 与（在用户更新模板ID时，读取模板中的扩展属性 赋给商品的扩展属性 。）发生冲突
				// 根据id是否存在判断是新增还是修改
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                //显示SKU列表 规格列转换
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
                }


            }
		)
		
    }

    //判断规格选项是否被选中
	$scope.checkAttributeValue=function (specName,optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        //判断规格是否存在
        var object= $scope.searchObjectByKey(items,'attributeName',specName)
		if (object != null){
        	//如果能够查找到规格选项(数组) 就返回true
        	if (object.attributeValue.indexOf(optionName)>=0){
        		return true;
			} else {
        		return false;
			}

		} else {
			return false;
		}

    }



});	
