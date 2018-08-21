package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *  搜索工具
 * @author hongbin
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据
     */
    public void importItemData(){
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        // 已审核状态的
        criteria.andStatusEqualTo("1");
        List<TbItem> items = itemMapper.selectByExample(itemExample);
        for(TbItem item:items){
            System.out.println(item.getTitle());
            //从数据库中提取规格的json字符串 转换为Map
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);

        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();

        System.out.println("===结束===");

    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil=  (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();

    }
}
