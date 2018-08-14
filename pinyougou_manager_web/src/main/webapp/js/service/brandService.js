// 定义品牌服务
app.service("brandService",function ($http) {
    this.findAll=function () {
        return $http.get('../brand/findPage.do');
    };

    this.findPage=function (page,size) {
        return $http.get('../brand/findPage.do?page='+page+'&rows='+size)
    }

    this.getById=function (id) {
        return $http.get('../brand/selectOne.do?id='+id);
    }

    this.add=function (entity) {
        return $http.post('../brand/add.do',entity)
    }

    this.update=function (entity) {
        return $http.post('../brand/update.do',entity)
    }

    this.del=function (ids) {
        return $http.get('../brand/delete.do?ids='+ids)
    }

    this.search=function (page,size,searchEntity) {
        return $http.post('../brand/search.do?page='+page+'&rows='+size,searchEntity)
    }

    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }

});