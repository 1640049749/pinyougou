var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        seckillId: 0,//秒杀商品的id，页面加载的时候获取
        goodsInfo:{},//根据id获取秒杀商品信息（剩余时间，剩余库存）
        timeString:'',//倒计时
        messageInfo: '',
        ids: [],
        searchEntity: {}
    },
    methods: {
        //点击立即抢购时调用 后台生成订单
        submitOrder: function () {
            axios.get('/seckillOrder/submitOrder.shtml?id=' + this.seckillId).then(
                function (response) {

                    if (response.data.success) {
                        app.messageInfo = response.data.message;//提示，正在排队中
                    } else {
                        if (response.data.message == '403') {
                            //说明没有登录  去登录
                            var url = window.location.href;//获取当前页面的路径
                            window.location.href = "http://localhost:9109/page/login.shtml?url=" + url;
                        } else {
                            alert(response.data.message);
                        }
                    }
                }
            )
        },
        //页面需要不断查询订单是否创建成功 点击立即抢购就是需要调用了
        queryStatus:function () {
            var count=0;
            let queryObject = window.setInterval(function () {
                count+=1;
                axios.get('/seckillOrder/queryOrderStatus.shtml').then(
                    function (response) {
                        console.log("正在查询.............状态值"+response.data.message);
                        if(response.data.success){
                            //去支付
                            window.location.href="pay.html";
                        }else{
                            app.messageInfo=response.data.message+"......"+count;
                        }
                    }
                )
            },3000);
        },
        convertTimeString: function (alltime) {
            var allsecond = Math.floor(alltime / 1000);//毫秒数转成 秒数。
            var days = Math.floor(allsecond / (60 * 60 * 24));//天数
            var hours = Math.floor((allsecond - days * 60 * 60 * 24) / (60 * 60));//小数数
            var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟数
            var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60; //秒数
            if (days >= 0 && days < 10) {
                days = "0" + days + "天 ";
            }
            if (days >= 10) {
                days = days + "天 ";
            }
            if (hours < 10) {
                hours = "0" + hours;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (seconds < 10) {
                seconds = "0" + seconds;
            }
            return days + hours + ":" + minutes + ":" + seconds;
        },

        //倒计时
        calculate: function (alltime) {
            var clock = window.setInterval(function () {
                alltime = alltime - 1000;
                //反复被执行的函数
                app.timeString = app.convertTimeString(alltime);
                if (alltime <= 0) {
                    //取消
                    window.clearInterval(clock);
                }
            }, 1000);//相隔1000毫秒执行一次
        },
        //根据商品的ID 获取商品的数据：剩余时间的毫秒数以及 商品的库存
        getGoodsById: function (id) {
            axios.get('/seckillGoods/getGoodsById.shtml', {
                params: {
                    id: id
                }
            }).then(function (response) {
                console.log(response.data);
                app.goodsInfo = response.data;
                app.caculate(response.data.time);
                console.log(app.goodsInfo);
            })

        }
    },
    //钩子函数 初始化了事件和
    created: function () {
        //页面加载的时候从URL中解析出ID 的值 赋值给变量seckillId
        let urlParam = this.getUrlParam();
        this.seckillId = urlParam.id;

        this.getGoodsById(this.seckillId)
    }

});
