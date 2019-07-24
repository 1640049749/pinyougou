package com.pinyougou.es.service;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.es.service *
 * @since 1.0
 */
public interface ImportService  {
    /**
     * 查询数据库的数据 将其导入到ES服务器中
     */
    public void importDBToES();
}
