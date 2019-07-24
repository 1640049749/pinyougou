package com.pinyougou.sellergoods.service;
import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbTypeTemplate;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface TypeTemplateService extends CoreService<TbTypeTemplate> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize, TbTypeTemplate TypeTemplate);

	/**
	 * 根据模板的ID 获取模板对应的规格的数据  返回的格式为：
	 * [{"id":27,"text":"网络",optionsList:[{optionName:'移动3G'},{optionName:'移动4G'}]},{"id":32,"text":"机身内存"}]
	 * @param typeTemplateId
	 * @return
	 */
    List<Map> findSpecList(Long typeTemplateId);
}
