package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbGoods;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import com.pinyougou.pojo.TbItem;
import entity.Goods;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService extends CoreService<TbGoods> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize, TbGoods Goods);

	//添加商品的方法 ：组合对象
	void add(Goods goods);

	public void update(Goods goods);

	public Goods findOne(Long id);

    void updateStatus(Long[] ids, String status);

	/**
	 * 根据SPU的ID的数组 查询 该数组下所有的SKU的列表数据返回
	 * @param ids
	 * @return
	 */
	public List<TbItem> findTbItemListByIds(Long[] ids);

}
