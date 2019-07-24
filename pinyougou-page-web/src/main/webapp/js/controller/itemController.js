var app = new Vue({
    el: "#app",
    data: {
        num: 1,
        sku:skuList[0],
        //{"网络":"移动3G"}
        specificationItems:JSON.parse(JSON.stringify(skuList[0].spec))   //用于存储当前点击到的规格的数据
    },
    methods: {
        addNum: function (num) {
            this.num += num;
            if (this.num <= 0) {
                this.num = 1;
            }
        },
        //用于在点击的时候调用 改变变量的值
        selectSpecifcation:function(name,value){
            this.$set(this.specificationItems,name,value);
            this.search();
        },
        //判断循环到的选项 是否在当前的变量中存在,如果是 返回true 否则返回false
        isSelected:function (name,value) {
            if(this.specificationItems[name]==value){
                return true;
            }
            return false;
        },
        //循环遍历SKU的列表数组
        // 判断 当前的变量的值是否在数组中存在,如果存在,将对应的数组的元素赋值给变量sku

        search:function () {
            for(var i=0;i<skuList.length;i++){
                //{"id":14383881,"title":"iphonex60 移动3G 16G","price":0.01,spec:{"网络":"移动3G","机身内存":"16G"}}
                var obj = skuList[i];//
                if(JSON.stringify(this.specificationItems)==JSON.stringify(obj.spec)){
                    this.sku=obj;
                    break;
                }
            }
        },

        //添加购物车
        addGoodsToCart:function () {
            axios.get('http://localhost:9107/cart/addGoodsToCartList.shtml?itemId='+this.sku.id+'&num='+this.num, {withCredentials:true}).
            then(
                function (response) {//response.data=result
                    if(response.data.success){
                        window.location.href="http://localhost:9107/cart.html";
                    }else{
                        //添加购物车失败
                        alert(response.data.message);
                    }
                }
            )
        }
    },
    created: function () {

    }
});