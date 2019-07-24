package com.pinyougou.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.es.service.ImportService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.es.service.impl *
 * @since 1.0
 */
public class ImportServiceImpl implements ImportService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private ItemDao itemDao;

    @Override
    public void importDBToES() {
        //1.使用dao 根据条件查询数据库的中(tb_item)的数据
        //select * from tb_item where status=1
        TbItem condition = new TbItem();
        condition.setStatus("1");//状态为正常的数据
        List<TbItem> itemList = tbItemMapper.select(condition);

        //循环遍历集合  获取里面的规格的数据 字符串 {"网络":"移动4G","机身内存":"16G"}
        for (TbItem tbItem : itemList) {
            String spec = tbItem.getSpec(); //{"网络":"移动4G","机身内存":"16G"}
            //转成json对象（Map对象）
            Map map = JSON.parseObject(spec, Map.class);
            //map对象设置 规格的属性中specMap
            tbItem.setSpecMap(map);
        }
        //2.使用es的dao 保存数据到es服务器中
        itemDao.saveAll(itemList);
    }
}
