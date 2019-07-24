var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        entity_1:{},//绑定第一个对象
        entity_2:{},//绑定第二个对象
        grade:1,//默认当前的等级 1
        ids:[],
        searchEntity:{}
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/itemCat/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        //查询所有品牌列表
        findAll:function () {
            console.log(app);
            axios.get('/itemCat/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/itemCat/findPage.shtml',{params:{
                pageNo:this.pageNo
            }}).then(function (response) {
                console.log(app);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data.list;
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            }).catch(function (error) {

            })
        },
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/itemCat/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/itemCat/update.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        save:function () {
            if(this.entity.id!=null){
                this.update();
            }else{
                this.add();
            }
        },
        findOne:function (id) {
            axios.get('/itemCat/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/itemCat/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },

        //目的 就是当页面加载的时候调用 根据条件查询分类的列表
        findByParentId:function (parentId) {
            axios.get('/itemCat/findByParentId/'+parentId+'.shtml').then(
                function (response) {//response.data=list<itemcat>
                    app.list=response.data;
                }
            )
        },
        //方法 当点击 查询一级的按钮的时候调用：目的就是影响变量 entiy_1 和entity_2的值
        selectList:function (p_entity) {
            // 定义两个变量 entity_1  entity_2  ,grade =1

            // 判断 当前的等级 如果是 1
            if(this.grade==1){
                // 赋值给entity_1=null ,entity_2 = null
                this.entity_1={};
                this.entity_2={};
            }

            // 判断 当前的等级 如果是 2
           if(this.grade==2){
               //把被点击到的对象 赋值给entity_1  entity_2=null
                this.entity_1=p_entity;
                this.entity_2={};
           }
            // 判断 当前的等级 如果是 3
            if(this.grade==3){
                // 把被点击到的对象 赋值给entity_2  entity_1 不用动
                this.entity_2=p_entity;
            }

            this.findByParentId(p_entity.id)
        }




    },
    //钩子函数 初始化了事件和
    created: function () {
        this.findByParentId(0);
    }

})
