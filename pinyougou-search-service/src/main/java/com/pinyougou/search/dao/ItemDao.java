package com.pinyougou.search.dao;

import com.pinyougou.pojo.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.dao *
 * @since 1.0
 */
public interface ItemDao extends ElasticsearchRepository<TbItem,Long> {

}
