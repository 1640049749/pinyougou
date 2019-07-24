package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.dao.ItemDao;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service.impl *
 * @since 1.0
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {


    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {

        Map<String,Object> resultMap = new HashMap<>();

        //1.获取关键字
        String keywords = (String) searchMap.get("keywords");

        //2.创建查询对象的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件
        //ativeSearchQueryBuilder.withIndices("pinyougou") 指定索引 默认查询所有的索引
        //nativeSearchQueryBuilder.withTypes("item") 指定类型 默认 查询所有的类型

//        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("keyword",keywords));
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"seller","category","brand","title"));

        //设置一个聚合查询的条件 ：1.设置聚合查询的名称（别名）2.设置分组的字段
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("category_group").field("category").size(50));



        //3.1 设置高亮显示的域（字段） 设置 前缀 和后缀
        nativeSearchQueryBuilder
                .withHighlightFields(new HighlightBuilder.Field("title"))
                .withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));



        //3.2 过滤查询  ----商品分类的过滤查询

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


        String category = (String) searchMap.get("category");
        if(StringUtils.isNotBlank(category)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
        }

        //3.3 过滤查询 ----商品的品牌的过滤查询

        String brand = (String) searchMap.get("brand");
        if(StringUtils.isNotBlank(brand)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }

        //3.4 过滤查询 ----规格的过滤查询 获取到规格的名称 和规格的值  执行过滤查询
        Map<String,String> spec = (Map<String, String>) searchMap.get("spec");//{"网络":"移动4G","机身内存":"16G"}
        if(spec!=null) {
            for (String key : spec.keySet()) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key + ".keyword", spec.get(key)));
            }
        }


        //3.5 过滤查询    ----价格区间的过滤查询  范围查询

        String price = (String) searchMap.get("price");// 0-500  3000-*

        if(StringUtils.isNotBlank(price)){
            String[] split = price.split("-");// 0   500
            if(split[1].equals("*")){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            }
        }


        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);


        //4.构建查询对象
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();

        //分页条件设置
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo==null){
            pageNo=1;
        }
        if(pageSize==null){
            pageSize=40;
        }

        //第一个参数 表示当前的页码 0 表示第一页
        //第二个参数 表示每页显示的行
        Pageable pageable = PageRequest.of(pageNo-1,pageSize);
        searchQuery.setPageable(pageable);

        //设置排序

        String sortField = (String) searchMap.get("sortField");//排序的字段
        String sortType = (String) searchMap.get("sortType");//排序的类型 DESC  ASC

        if(StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortType)) {
            if(sortType.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, sortField);
                searchQuery.addSort(sort);
            }else if(sortType.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, sortField);
                searchQuery.addSort(sort);
            }else{
                //"afadfssa";
                //不排序
            }
        }

        //5.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(searchQuery, TbItem.class, new SearchResultMapper() {

            //自定义 进行结果集的映射   //获取高亮
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //1.创建当前的页的集合
                List<T> content = new ArrayList<>();
                //2.获取查询的结果 获取总记录数
                SearchHits hits = response.getHits();

                //3.判断是否有记录 如果没有 返回
                if(hits==null || hits.getTotalHits()<=0){
                    return new AggregatedPageImpl(content);
                }
                //4.有记录  获取高亮的数据
                for (SearchHit hit : hits) {
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    HighlightField highlightField = highlightFields.get("title");//获取高亮的字段的数据对象

                    if(highlightField!=null) {
                        StringBuffer sb= new StringBuffer();
                        Text[] fragments = highlightField.getFragments();
                        if(fragments!=null && fragments.length>0){
                            for (Text fragment : fragments) {
                                String string = fragment.string();//<em style="core">aaaaaaaa</em>
                                sb.append(string);
                            }

                            String sourceAsString = hit.getSourceAsString();//btitem的数据JSON格式
                            TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);
                            tbItem.setTitle(sb.toString());//有高亮
                            content.add((T)tbItem);
                        }else{
                            //设置值。
                        }
                    }else{
                        //没有高亮的数据
                        String sourceAsString = hit.getSourceAsString();//btitem的数据JSON格式
                        TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);
                        content.add((T)tbItem);
                    }
                }



                return new AggregatedPageImpl<T>(content,pageable,hits.getTotalHits(),response.getAggregations(),response.getScrollId());
            }
        });



        //6.获取结果集

        //获取分组的结果
        Aggregation category_group = tbItems.getAggregation("category_group");
        StringTerms stringTerms = (StringTerms)category_group;
        System.out.println(stringTerms);
        List<String> categoryList= new ArrayList<>(); //  平板电视  手机
        if(stringTerms!=null ){
            List<StringTerms.Bucket> buckets = stringTerms.getBuckets();

            for (StringTerms.Bucket bucket : buckets) {
                categoryList.add(bucket.getKeyAsString());//就是商品分类的名称   平板电视  手机
            }
        }

        //搜索之后 默认 展示第一个商品分类的品牌和规格的列表

        //判断 商品分类是否为空 如果不为空 根据点击到的商品分类查询 该分类下的所有的品牌和规格的列表
        if(StringUtils.isNotBlank(category)){
            Map map = searchBrandAndSpecList(category);//{ "brandList",[],"specList":[]}
            resultMap.putAll(map);
        }else {
            //否则 查询默认的商品分类下的品牌和规格的列表
            if(categoryList!=null && categoryList.size()>0) {
                Map map = searchBrandAndSpecList(categoryList.get(0));//{ "brandList",[],"specList":[]}
                resultMap.putAll(map);
            }else{
                resultMap.put("specList", new HashMap<>());
                resultMap.put("brandList",new HashMap<>());
            }

        }

        //7.设置结果集到map 返回(总页数 总记录数 当前页的集合 ......)
        resultMap.put("total",tbItems.getTotalElements());
        resultMap.put("rows",tbItems.getContent());//当前页的集合
        resultMap.put("totalPages",tbItems.getTotalPages());//总页数
        resultMap.put("categoryList",categoryList);//商品分类的列表数据
        //resultMap.putAll(map);

        return resultMap;
    }

    @Autowired
    private ItemDao itemDao;


    @Override
    public void updateIndex(List<TbItem> tbItemList) {
        for (TbItem tbItem : tbItemList) {
            String spec = tbItem.getSpec(); //{"网络":"移动4G","机身内存":"16G"}
            //转成json对象（Map对象）
            Map map = JSON.parseObject(spec, Map.class);
            //map对象设置 规格的属性中specMap
            tbItem.setSpecMap(map);
        }
        itemDao.saveAll(tbItemList);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        //ids  里面是goods_id的值
        //delte from tb_item where goods_id in (1,2,) 从ES 删除

        DeleteQuery query = new DeleteQuery();
        query.setQuery(QueryBuilders.termsQuery("goodsId",ids));
        elasticsearchTemplate.delete(query,TbItem.class);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *  根据分类的名称 获取 分类下的品牌的列表 和规格的列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        //1.集成redis
        //2.注入rediTemplate
        //3.获取分类的名称对应的模板的ID
        //hset bigkey field1 value1     hget bigkey field1
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //4.根据模板的ID 获取品牌的列表 和规格的列表
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map>  specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        //5.存储到map中返回
        Map<String,Object> map = new HashMap<>();
        map.put("specList",specList);
        map.put("brandList",brandList);
        return map;
    }
}
