var app = new Vue({
    el: "#app",
    data: {
        username:'a'
    },
    methods:{
        getInfo:function () {
            axios.get('/login/userinfo.shtml').then(
                function (response) {//response.data=username
                    app.username=response.data;
                }
            )
        }
    },
    created:function () {
        this.getInfo();
    }
});