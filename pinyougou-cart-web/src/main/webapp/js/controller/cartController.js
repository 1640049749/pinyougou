var app = new Vue({
    el: "#app",
    data: {
        totalMoney: 0,//总金额
        totalNum: 0,//总数量
        cartList: [],
        addressList: [],//地址列表
        address:{},
        order:{paymentType:'1'},//订单对象
    },
    methods: {
        //查询所有的购物车的列表数据
        findCartList: function () {
            axios.get('/cart/findCartList.shtml').then(
                function (response) {
                    app.cartList = response.data;//List<Cart>   cart { List<ORDERiMTE> }
                    app.totalMoney = 0;
                    app.totalNum = 0;
                    for (var i = 0; i < response.data.length; i++) {
                        var obj = response.data[i];//Cart
                        for (var n = 0; n < obj.orderItemList.length; n++) {
                            var objx = obj.orderItemList[n];//ORDERiMTE
                            app.totalMoney += objx.totalFee;
                            app.totalNum += objx.num;
                        }
                    }

                }
            )
        },
        //向已有的购物车中添加商品
        addGoodsToCartList: function (itemId, num) {
            axios.get('/cart/addGoodsToCartList.shtml?itemId=' + itemId + '&num=' + num).then(
                function (response) {
                    if (response.data.success) {
                        //
                        app.findCartList();
                    }
                }
            )
        },
        //查询用户所有地址列表
        findAddressList: function () {
            axios.get('/address/findAddressListByUserId.shtml').then(function (response) {
                app.addressList = response.data;
                for(var i=0;i<app.addressList.length;i++){
                    if(app.addressList[i].isDefault=='1'){
                        app.address=app.addressList[i];
                        break;
                    }
                }
            });
        },
        //选中地址时给变量address赋值此地址
        selectAddress:function (address) {
            this.address=address;
        },
        //判断此地址是否与变量address地址相同，如果相同返回true，页面标签会select勾选
        isSelectedAddress:function (address) {
            if(address==this.address){
                return true;
            }
            return false;
        },
        //选择付款方式
        selectType:function (type) {
            console.log(type);
            this.$set(this.order,'paymentType',type);
            //this.order.paymentType=type;
        },
        //提交订单
        submitOrder: function () {
            //设置值
            this.$set(this.order,'receiverAreaName',this.address.address);//详细地址
            this.$set(this.order,'receiverMobile',this.address.mobile);//收货人手机号
            this.$set(this.order,'receiver',this.address.contact);//收货人
            axios.post('/order/submitOrder.shtml', this.order).then(
                function (response) {
                    if(response.data.success){
                        //跳转到支付页面
                        window.location.href="pay.html";
                    }else{
                        alert(response.data.message);
                    }
                }
            )
        },
    },
    created: function () {
        //findCartList请求会被放行，findAddressList请求会被拦截，会出现错误。
        // 只有用户进入结算页面时才执行findAddressList（此时用户已经登录，不会被拦截）
        this.findCartList();
        if (window.location.href.indexOf("getOrderInfo.html") != -1)
            this.findAddressList();
    }
});