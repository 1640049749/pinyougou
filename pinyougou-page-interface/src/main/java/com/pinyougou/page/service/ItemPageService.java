package com.pinyougou.page.service;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.page.service *
 * @since 1.0
 */
public interface ItemPageService {
    /**
     * 根据SPU的ID 获取SPU的数据和相关的数据 生成静态页
     * @param id
     */
    void genItemHtml(Long id);

    public void deleteById(Long[] goodsId);
}
