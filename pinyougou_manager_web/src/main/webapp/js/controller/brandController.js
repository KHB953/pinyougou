// 定义一个控制器
app.controller('brandContorller',function ($scope,$http,$controller,brandService) {
    //继承baseController
    $controller('baseController',{$scope:$scope});


    // 查询品牌列表 读取列表数据绑定到表单中
    $scope.findAll=function () {
        //发送get请求到URL地址并响应回来数据
        //修改为调用brandService服务
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        )
    };

    // 加载数据
    $scope.findPage=function (page, size) {
        brandService.findPage(page,size).success(function (response) {
            //显示当前页的数据
            $scope.list=response.rows;
            //更新总记录数
            $scope.paginationConf.totalItems=response.total;
        });
    };

    // 增加 点击新建按钮的之前清空entity的数据 根据是否有Id判断执行更新还是添加 方法
    $scope.save=function () {
        var object=null;
        if ($scope.entity.id != null){
            object=brandService.update($scope.entity);
        }else {
            object=brandService.add($scope.entity);
        }

        object.success(
            function (response) {
                if (response.success) {
                    //刷新列表
                    $scope.reloadList();
                }else {
                    //失败就提示信息
                    alert(response.message)
                }
            }
        )
    };

    //点击修改回显
    $scope.getById=function(id){
        brandService.getById(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    };



    //删除选中的项
    $scope.del=function(){
        brandService.del($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    //刷新列表
                    $scope.reloadList();
                }
            }
        )
    };

    //条件查询
    $scope.searchEntity={};
    $scope.search=function(page,size){
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                //显示当前页的数据
                $scope.list=response.rows;
                //更新总记录数
                $scope.paginationConf.totalItems=response.total;
            }
        );
    }

})