var app = new Vue({
    el: "#app",
    data: {
        searchMap:{'keywords':'','category':'','brand':'',spec:{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sortType':''},//作为条件查询的对象
        resultMap:{brandList:[]},//返回的结果对象
        pageLabels:[],//存储页码的变量
        preDott:false,//设置前面是否有点的标记
        nextDott:false,//设置后面是否有点的标记
        searchEntity: {}
    },
    methods: {
        //根据搜索的条件 执行查询 返回结果 resultmap 点击的时候调用
        search:function () {
            axios.post('/itemSearch/search.shtml',this.searchMap).then(
                function (response) {//response.data=map 会有集合数据
                    app.resultMap=response.data;
                    app.buildPageLabel();
                }
            )
        },
        //添加搜索项
        addSearchItem:function(key,value){
            if(key=='category' || key=='brand' || key=='price') {
                this.searchMap[key] = value;
            }else{
                this.searchMap.spec[key]=value;
            }
            //发送请求 执行搜索
            this.search();
        },
        //移除掉搜索项
        removeSearchItem:function (key) {
            //1.移除变量里面的值
            if(key=='category' || key=='brand' || key=='price') {
                this.searchMap[key] = '';
            }else{
                //{ }
                delete this.searchMap.spec[key];
            }
            //2.重新发送请求查询
            this.search();
        },
        //目的就是构建分页的标签数据 跟总页数有关系,每次搜索的时候都需要调用。
        buildPageLabel:function () {
            this.pageLabels=[];
            this.preDott=false;
            this.nextDott=false;
            var firstPage=1;//开始页码

            var lastPage= this.resultMap.totalPages;//结束页码


            if(this.resultMap.totalPages>5){
                //如果 当前页 <=3 显示前5页
                if(this.searchMap.pageNo<=3){
                    firstPage=1;
                    lastPage=5;

                    this.preDott=false;
                    this.nextDott=true;
                }else if(this.searchMap.pageNo>=(this.resultMap.totalPages-2)){// 如果 当前页>=总页数-2 只显示后5页
                    firstPage=this.resultMap.totalPages-4;
                    lastPage=this.resultMap.totalPages;
                    this.preDott=true;
                    this.nextDott=false;
                }else{
                    firstPage=this.searchMap.pageNo-2;
                    lastPage=this.searchMap.pageNo+2;
                    this.preDott=true;
                    this.nextDott=true;
                }
            }else{
                //啥也不敢
            }

            for(var i=firstPage;i<=lastPage;i++){
                this.pageLabels.push(i);
            }

        },
        //根据页码来进行分页查询
        queryByPage:function (page) {
            //先转换成数字
            var number = parseInt(page);
            if(number>this.resultMap.totalPages){
                number=this.resultMap.totalPages;
            }
            if(number<1){
                number=1;
            }

            //1.改变当前页码的值
            this.searchMap.pageNo=number;
            //2.发送请求 执行分页的查询
            this.search();

        },
        //清楚
        clear:function() {
            this.searchMap={'keywords':this.searchMap.keywords,'category':'','brand':'',spec:{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sortType':''};
        },
        //进行排序
        doSort:function (sortField,sortType) {
            //1.改变两个变量的值
            this.searchMap.sortField=sortField;
            this.searchMap.sortType=sortType;

            //2.执行搜素
            this.search();

        },
        //判断 搜素的关键字是否就是品牌 如果是 返回true  否则返回false
        isKeywordsIsBrand:function () {
            //循环遍历品牌的列表  判断 关键字中是否包含品牌即可 如果是 返回true 否则 返回false

            for(var i=0;i<this.resultMap.brandList.length;i++){//[{id:1,text:"联想"},{}]

                if(this.searchMap.keywords.indexOf(this.resultMap.brandList[i].text)!=-1){
                    //赋值brand
                    this.searchMap.brand=this.resultMap.brandList[i].text;
                    return true;
                }
            }
            return false;
        }
    },
    //页面记载
    created: function () {
        //1.获取URL中的参数的值
        var jsonObj = this.getUrlParam();
        //2.赋值给变量searchMap.keywords
        // decodeURIComponent 解码
        this.searchMap.keywords=decodeURIComponent(jsonObj.keywords);
        //3.执行搜索
        this.search();

    }
});