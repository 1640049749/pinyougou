package com.pinyougou;

import static org.junit.Assert.assertTrue;

import com.itheima.dao.ItemDao;
import com.itheima.model.TbItem;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContextConfiguration("classpath:spring-es.xml")
@RunWith(SpringRunner.class)
public class AppTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemDao itemDao;

    //创建索引和创建 映射
    @Test
    public void putMapping() {
        elasticsearchTemplate.createIndex(TbItem.class);
        elasticsearchTemplate.putMapping(TbItem.class);

    }

    //创建文档数据
    @Test
    public void addDocument(){

        TbItem item = new TbItem();
        item.setId(1000L);
        item.setGoodsId(1000L);
        item.setTitle("华为手机");
        item.setSeller("华为旗舰店");
        item.setBrand("华为");
        item.setCategory("手机");
        Map<String, String> specMap= new HashMap<>();
        specMap.put("网络制式","移动4G");
        specMap.put("机身内存","16G");
        item.setSpecMap(specMap);
        itemDao.save(item);

        /*for (long i = 0; i < 100; i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setGoodsId(i);
            item.setTitle("华为手机"+i);
            item.setSeller("华为旗舰店"+i);
            item.setBrand("华为"+i);
            item.setCategory("手机"+i);
            itemDao.save(item);
        }*/

    }

    @Test
    public void deleteByID(){
        itemDao.deleteById(1000L);
    }

    //查询所有

    @Test
    public void findAll(){
        Iterable<TbItem> all = itemDao.findAll();
        for (TbItem item : all) {
            System.out.println(item.getTitle());
        }
    }

    @Test
    public void QueryById(){
        System.out.println(itemDao.findById(98L).get().getTitle());
    }

    //分页查询

    @Test
    public void queryBypage(){
        //第一个参数 标识当前的页码  第一页从0 开始
        Pageable pageable = PageRequest.of(0,10);
        Page<TbItem> page = itemDao.findAll(pageable);

        System.out.println("获取总记录数："+page.getTotalElements());
        int totalPages = page.getTotalPages();
        System.out.println("totalPages:"+totalPages);

        List<TbItem> content = page.getContent();//当前页的集合

        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }

    //spring data elasticasearch中的elasticsearchTemplate对象的查询的方法=====================================================elasticsearchTemplate

    @Test
    public void queryBywildcardQuery(){
        //1.创建一个查询对象  //2.设置查询的条件
        SearchQuery query = new NativeSearchQuery(QueryBuilders.wildcardQuery("title","手*"));

        //3.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        //4.获取到查询结果 处理结果
        System.out.println("获取到总记录数："+tbItems.getTotalElements());
        System.out.println("获取到页数："+tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }

    //match 先分词的查询
    @Test
    public void matchQuery(){
        //1.创建一个查询对象 设置查询的条件
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("title","三星手机"));
        //2.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        //3.获取结果
        System.out.println("获取到总记录数："+tbItems.getTotalElements());
        System.out.println("获取到页数："+tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }

    //组合域进行搜索
    @Test
    public void matchZuheQuery(){
        //1.创建一个查询对象 设置查询的条件
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("keyword","手机"));
        //2.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        //3.获取结果
        System.out.println("获取到总记录数："+tbItems.getTotalElements());
        System.out.println("获取到页数："+tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }

    //对象域的查询
    @Test
    public void queryByObject(){
        //1.创建一个查询对象 设置查询的条件
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("specMap.网络制式.keyword","移动4G"));
        //2.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);
        //3.获取结果
        System.out.println("获取到总记录数："+tbItems.getTotalElements());
        System.out.println("获取到页数："+tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }

    }

    //多条件组合查询

    @Test
    public void booleanquery(){

        //1.创建查询对象的构建对象 buidler
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //2.设置查询的条件
        queryBuilder.withIndices("pinyougou");
        queryBuilder.withTypes("item");
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","华为手机"));//主要的查询条件

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//组合多个条件
        //组合查询的条件中 如果都是must 可以使用filter查询 性能要比must要高
        boolQueryBuilder.filter(QueryBuilders.termQuery("specMap.网络制式.keyword","移动4G"));
        boolQueryBuilder.filter(QueryBuilders.termQuery("specMap.机身内存.keyword","16G"));

        queryBuilder.withFilter(boolQueryBuilder);
        //3.构建查询对象
        SearchQuery query = queryBuilder.build() ;

        //4.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(query, TbItem.class);

        //5.获取结果
        System.out.println("获取到总记录数："+tbItems.getTotalElements());
        System.out.println("获取到页数："+tbItems.getTotalPages());
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }



    }



}
