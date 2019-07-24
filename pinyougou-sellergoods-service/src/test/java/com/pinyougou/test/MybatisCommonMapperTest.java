package com.pinyougou.test;

import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.test *
 * @since 1.0
 */
@ContextConfiguration("classpath:spring/applicationContext-dao.xml")
@RunWith(SpringRunner.class)
public class MybatisCommonMapperTest {

    @Autowired
    private TbBrandMapper brandMapper;

    @Test
    public void select(){
        //exmaple的查询
        Example exmaple = new Example(TbBrand.class);//  tb_brand
        Example.Criteria criteria = exmaple.createCriteria();
        criteria.andEqualTo("id",1L);// where id = 1
        criteria.andEqualTo("name","联想");// name = '联想'

        List<TbBrand> brands1 = brandMapper.selectByExample(exmaple);//select * from tb_brand where id = 1 and name = '联想'

        System.out.println(brands1);

        //查询所有
        List<TbBrand> brands = brandMapper.selectAll();
        System.out.println(brands);


        //根据条件查询 只能针对=

        TbBrand condition = new TbBrand();//条件
        condition.setId(1L);
        condition.setName("联想");
        List<TbBrand> select = brandMapper.select(condition);//select * from tb_brand where id = 1 and name = '联想'

        System.out.println(select);
    }

    @Test
    public void pagehelper(){
        //1.开始分页   紧跟着的第一个查询条件才会被分页
        PageHelper.startPage(1,10);
        //2.执行查询的逻辑
        List<TbBrand> brands = brandMapper.selectAll();
        List<TbBrand> brands1 = brandMapper.selectAll();
        System.out.println(brands);
        System.out.println(brands1);
    }
}
