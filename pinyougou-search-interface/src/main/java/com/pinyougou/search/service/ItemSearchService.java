package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map; /**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service *
 * @since 1.0
 */
public interface ItemSearchService {
    /**
     *
     * @param searchMap  页面传递过来的条件 执行查询
     * @return 返回一个Map 包括所有的数据（集合，规格数据 总页数 总个记录数。。。。。）
     */
    Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 更新ES索引
     * @param tbItemList 要更新的数据
     */
    void updateIndex(List<TbItem> tbItemList);

    /**
     * 移除ES中的数据
     * @param ids
     */
    void deleteByIds(Long[] ids);
}
