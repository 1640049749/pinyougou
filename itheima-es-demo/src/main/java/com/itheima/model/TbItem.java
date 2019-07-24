package com.itheima.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.itheima.model *
 * @since 1.0
 */

/**
 * 保证以下的情况
 * 1.创建 索引 创建 类型
 * 2.创建文档（唯一的标识）
 * 3.文档中有field ：有映射（是否分词 是否索引 是否存储 使用哪个分词器 数据类型是什么）
 *
 */
@Document(indexName = "pinyougou",type = "item")
public class TbItem implements Serializable {
    /**
     * 商品id，同时也是商品编号
     */
    @Id//标识 文档的唯一标识
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 商品标题
     */
    @Field(index = true,analyzer = "ik_smart",searchAnalyzer = "ik_smart",type = FieldType.Text,copyTo = "keyword")
    private String title;

    //表示是一个对象类型
    @Field(type = FieldType.Object)
    private Map<String,String> specMap;

    @Field(type = FieldType.Long)
    private Long goodsId;

    /**
     * 冗余字段 存放三级分类名称  关键字 只能按照确切的词来搜索
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String category;

    /**
     * 冗余字段 存放品牌名称
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String brand;

    /**
     * 冗余字段，用于存放商家的店铺名称
     */
    @Field(index = true,type = FieldType.Keyword,copyTo = "keyword")
    private String seller;



    public Map<String, String> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, String> specMap) {
        this.specMap = specMap;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }
}
