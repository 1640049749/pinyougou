var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        brandOptions:[],//绑定品牌列表下拉框数据的
        specOptions:[{id:1,text:"机身内存"}],//绑定规格的数据列表，
        entity:{customAttributeItems:[]},
        ids:[],
        searchEntity:{}
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/typeTemplate/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
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
            axios.get('/typeTemplate/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/typeTemplate/findPage.shtml',{params:{
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
            axios.post('/typeTemplate/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/typeTemplate/update.shtml',this.entity).then(function (response) {
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
            axios.get('/typeTemplate/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;

                //将字符串转成JSON 赋值给原来的变量
                // JSON.parse () 将字符串转成JSON对象
                //JSON.stringify(json对象) 将json对象转成字符串
               // var  stringify = JSON.stringify(app.entity.brandIds);
                app.entity.brandIds=JSON.parse( app.entity.brandIds);
                app.entity.specIds=JSON.parse( app.entity.specIds);
                app.entity.customAttributeItems=JSON.parse( app.entity.customAttributeItems);

            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/typeTemplate/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        //页面加载的时候调用 发送请求 获取所有的品牌列表数据  赋值给变量 brandOptions  要求：[{id:"",text:""}]
        findBrandIds:function () {
            axios.get('/brand/findAll.shtml').then(
                function (response) {
                    //response.data=[{id:,name:,firstchar:}]
                    //[{id:1,text:"联想"},{id:2,text:"华为"}]
                    for(var i=0;i<response.data.length;i++){
                        var obj = response.data[i];//{id:,name:,firstchar:}
                        app.brandOptions.push({"id":obj.id,"text":obj.name});
                    }

                }
            )
        },
        //页面加载的时候调用 发送请求 获取所有的规格的数据列表  赋值给变量 specOptions  要求：[{id:"",text:""}]
        findSpecList:function () {
            axios.get('/specification/findAll.shtml').then(
                function (response) {
                    //response.data=[{id:,specName:}]//规格的列表
                    //[{id:1,text:"机身内存"}]
                    for(var i=0;i<response.data.length;i++){
                        var obj = response.data[i];//{id:,specName:}
                        app.specOptions.push({"id":obj.id,"text":obj.specName});
                    }

                }
            )
        },
        //向数组中添加js对象
        addTableRow:function () {
            this.entity.customAttributeItems.push({});
        },
        removeTableRow:function (index) {
            this.entity.customAttributeItems.splice(index,1);
        },


        //var obj ={ id:1}
        //var id = obj.id 或者  var idx = obj['id'];


        //key    'text'

        jsonToString:function (list,key) {
            //1.将字符串转成JSON对象
            var jsonObj = JSON.parse(list); //[{}]
            //2.循环遍历[{}]
            var str="";
            for(var i=0;i<jsonObj.length;i++){
                str+=jsonObj[i][key]+",";//{id,text}
            }
            if(str.length>0){
                str=str.substring(0,str.length-1)
            }
            //3.获取里面的text文本的值 通过逗号分隔 返回
            return str;
        }




    },
    //钩子函数 初始化了事件和
    created: function () {
      
        this.searchList(1);

        this.findBrandIds();
        this.findSpecList();

    }

})
