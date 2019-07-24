var app = new Vue({
    el:"#app",
    data: {
        totalMoney:0,
        payObject:{}//封装支付的金额 二维码连接 交易订单号
    },
    methods: {
        createNative:function () {
            var that=this;
            axios.get('/pay/createNative.shtml').then(
                function (response) {
                    //如果有数据
                    if(response.data){
                        app.payObject=response.data;
                        app.payObject.total_fee=app.payObject.total_fee/100;
                        //生成二维码
                        var qr = new QRious({
                            element:document.getElementById('qrious'),
                            size:250,
                            level:'H',
                            value:app.payObject.code_url
                        });
                        //已经生成二维码了
                        if(qr){
                            //发送请求获取值
                            app.queryPayStatus(app.payObject.out_trade_no);
                        }
                    }
                }
            )
        },
        queryPayStatus:function (out_trade_no) {
            axios.get('/pay/queryPayStatus.shtml',{
                params:{
                    out_trade_no:out_trade_no
                }
            }).then(function (response) {
                if (response.data.success) {
                    //支付成功
                    window.location.href="paysuccess.html?money="+app.payObject.total_fee;
                } else {
                    //支付失败 1.支付超时 2.支付失败
                    
                    if (response.data.message == '支付超时') {
                        //取消订单
                    } else {
                        window.location.href="payfail.html";
                    }
                }
            })
        },
    },
    created: function () {
        //页面一加载就应当调用
        if(window.location.href.indexOf("pay.html")!=-1){
            this.createNative();
        }else {
            let urlParamObject = this.getUrlParam();
            if(urlParamObject.money)
                this.totalMoney=urlParamObject.money;
        }
    }
});