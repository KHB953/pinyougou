//搜索控制层
app.controller("searchController",function ($scope,$location,searchService) {
    //搜索
    $scope.search=function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                //返回搜索结果
                $scope.resultMap=response;
                // 构建页码
                buildPageTotal();
            }
        )
    }

    // 面包屑
    $scope.searchMap={'keywords':'', 'brand':'', 'category':'', 'spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
    $scope.addSearchItem=function (key,value) {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;

        if (key == 'brand' || key == 'category' || key == 'price' ) {
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        //执行搜索
        $scope.search();

    }

    //移除面包屑中的选项
    $scope.removeSearchItem=function (key) {
        if (key == 'brand' || key == 'category' || key == 'price' ) {
            $scope.searchMap[key]="";
        } else {
            delete $scope.searchMap.spec[key];
        }

        $scope.search();
    }

    //构建分页标签(totalPages为总页数)
    buildPageTotal = function () {
        $scope.pageLabel=[];//新增分页栏属性
        //得到最大的页码
        var maxPageNo = $scope.resultMap.totalPages;
        var firstPage =1;
        var lastPage=maxPageNo;

        // 省略号显示 默认前后都显示
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点


        //只显示5页
        if ($scope.resultMap.totalPages > 5){
            //当前页码<=3 就显示1~5
            if($scope.searchMap.pageNo <= 3){
                lastPage =5;
                $scope.firstDot=false;//前面没点


                //当前页码大于等于 最大页码-2
            } else if ($scope.searchMap.pageNo >= lastPage-2 ){
                firstPage=maxPageNo-4;

                $scope.lastDot=false;//后边没点

            } else {
                // 显示已当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;

            }

        }else {
            $scope.firstDot=false;//前面没点
            $scope.lastDot=false;//后边有点
        }

        //循环产生页面标签
        for (var i = firstPage ;i<=lastPage ;i++) {
            $scope.pageLabel.push(i);
        }

    }

    $scope.qureyByPage= function (pageNo) {
        //页码验证
        if (pageNo <1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }else {
            $scope.searchMap.pageNo=pageNo;
            $scope.search();

        }

    }

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }

    //设置排序规则
    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
                return true;
            }
        }
        return false;
    }


    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        //查询
        $scope.search();
    }




})