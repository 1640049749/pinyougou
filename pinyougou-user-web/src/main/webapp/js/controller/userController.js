var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        smsCode: '',//验证码的值
        ids: [],
        searchEntity: {},
        name: ''//用户登录名
    },
    methods: {
        //用户注册
        register: function () {
            axios.post('/user/add' + this.smsCode + '.shtml', this.entity).then(function (response) {
                if (response.data.success) {
                    console.log(response);
                    //跳转到其用户后台的首页
                    window.location.href = "home-index.html";
                }
            }).catch(function (error) {
                console.log("1231312131321");
            })
        },
        //发送短信验证码
        createSmsCode: function () {
            axios.get('/user/sendCode.shtml?phone=' + this.entity.phone).then(function (response) {
                if (response.data.success) {
                    alert(response.data.message);//显示数据
                } else {
                    //发送失败
                    alert(response.data.message);//
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        //获取登录名
        getName: function () {
            axios.get('/login/name.shtml').then(function (response) {
                app.name = response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },

    },
    //钩子函数 初始化了事件和
    created: function () {
        this.getName();
    }

});
