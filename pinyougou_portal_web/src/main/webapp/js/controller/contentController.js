//  广告控制层 运营商
app.controller('contentController',function ($scope,contentService) {

    // 根据广告分类id查询广告集合
    $scope.contentList=[];
	$scope.findByCategoryId=function (categoryId) {
		contentService.findByCategoryId(categoryId).success(
			function (response) {
                $scope.contentList[categoryId]=response;
            }
		)
    }


    //搜索 传递参数 跳转
    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }




});
