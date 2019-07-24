var app = new Vue({
    el:"#app",
    data:{
        pages:15,//总页数 初始化值为15
        pageNo:1,//当前页 初始值 1
        entity:{},//品牌对象
        searchEntity:{},//搜索条件对象
        ids:[],//存储要删除的品牌的id列表
        list:[]//数组  [{id,name,firstchar},{},{}]
    },
    methods:{
        findAll:function () {
            //发送请求 获取列表数据 赋值给变量
            axios.get('/brand/findAll.shtml').then(
                function (response) {//response.data= list
                    app.list=response.data;
                }
            )
        },
        //第一：页码加载的就应该被调用默认查询第一页数据
        //第二：当点击下一页的时候 也被调用
        //发送请求 获取分页的数据 赋值给变量
        searchList:function (curPage) {
            axios.post('/brand/search.shtml?pageNo='+curPage,this.searchEntity).then(
                function (response) {//response.data=pageinfo
                    app.pageNo=response.data.pageNum;
                    app.pages=response.data.pages;
                    app.list=response.data.list;
                }
            )

            /*axios.post('/brand/findPage.shtml?pageNo='+curPage).then(
                function (response) {//response.data=pageinfo
                    app.pageNo=response.data.pageNum;
                    app.pages=response.data.pages;
                    app.list=response.data.list;
                }
            )*/
        },
        //添加品牌
        add:function () {
            axios.post('/brand/add.shtml',this.entity).then(
                function (response) {//response.data=result
                    if(response.data.success){
                        //刷新页面
                        app.searchList(1);
                    }else{
                        alert(response.data.message);
                    }
                }
            )
        },

        //更新品牌  当点击保存的时候调用
        update:function () {
            axios.post('/brand/update.shtml',this.entity).then(
                function (response) {//response.data=result
                    if(response.data.success){
                        //刷新页面
                        app.searchList(1);
                    }else{
                        alert(response.data.message);
                    }
                }
            )
        },

        //保存
        save: function () {
            if (this.entity.id==null || this.entity.id==undefined) {
                this.add();
            } else {
                //更新
                this.update();
            }
        },
        //删除
        dele:function () {
            axios.post('/brand/delete.shtml',this.ids).then(
                function (response) {//response.data=result
                    if(response.data.success){
                        app.ids=[];//清空
                        app.searchList(1);
                    }else{
                        alert(response.data.message);
                    }
                }
            )
        },


        //当点击修改的时候 根据点击到的品牌的ID 发送请求 获取数据赋值给变量entity
        findOne:function (id) {
            axios.get('/brand/findOne/'+id+'.shtml').then(
                function (response) {//response.data=tbbrand
                    app.entity=response.data;
                }
            )
        }
    },
    //钩子函数
    created:function () {
        // this.findAll();
        this.searchList(1);
    }
})