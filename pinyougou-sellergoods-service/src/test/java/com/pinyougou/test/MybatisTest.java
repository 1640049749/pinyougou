package com.pinyougou.test;

import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
public class MybatisTest {

    @Autowired
    private TbBrandMapper brandMapper;


   /* @Test
    public void testQuryAll(){
        List<TbBrand> all = brandMapper.findAll();

        for (TbBrand brand : all) {
            System.out.println(brand.getId()+";"+brand.getName()+":"+brand.getFirstChar());
        }
    }*/

   //逆向只能针对单表的操作。

   //查询
   /*@Test
    public void select(){
       //example 就是查询的条件  where条件
       TbBrandExample exmaple = new TbBrandExample();
       TbBrandExample.Criteria criteria = exmaple.createCriteria();
       criteria.andNameEqualTo("联想");//  select * from tb_brand where name="联想"

       //select * from tb_brand name="联想" and first_char = 'L'
       criteria.andFirstCharEqualTo("L");// first_char = 'L'

       List<TbBrand> brands = brandMapper.selectByExample(exmaple);
       for (TbBrand brand : brands) {
           System.out.println( brand.getName());
       }
   }

   //新增
    @Test
    public void insert(){
        TbBrand brand = new TbBrand();
        brand.setName("黑马");
        brand.setFirstChar("H");
        brandMapper.insert(brand);
    }

    //修改
    @Test
    public void update(){
        TbBrand brand = new TbBrand();
        brand.setName("黑马H");
        brand.setId(45L);
        brand.setFirstChar("H");
        brandMapper.updateByPrimaryKey(brand);
    }

    //删除
    @Test
    public void delete(){

        brandMapper.deleteByPrimaryKey(45L);
    }*/
}
