package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbItemCat;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface ItemCatService extends CoreService<TbItemCat> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize, TbItemCat ItemCat);

	/**
	 * 根据 分类的ID作为父分类的ID  查询该节点下的所有的子分类列表
	 * @param parentId
	 * @return
	 */
    List<TbItemCat> findByParentId(Long parentId);
}
