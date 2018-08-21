package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务实现类
 * @author hongbin
 */
@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索 item_keywords
     *  技巧：搜索方法 中将每个功能分为几块,将每一小块作为为私有方法，而搜索方法作为主线来调用,便于后期维护
     *
     * @param searchMap 使用Map 实现传入参数的多元化
     * @return 返回一个map 实现传出参数的多元化
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map = new HashMap<>();

        //1、查询列表 putAll 将查询列表的map 最近到新的map中
        map.putAll(searchList(searchMap));

        //2、分组查询 商品分类列表
        map.put("categoryList", searchCategoryList(searchMap));

        //3、查询品牌和规格列表
        if (searchCategoryList(searchMap).size()>0){
            Map brandAndSpec = searchBrandAndSpecList(searchCategoryList(searchMap).get(0));
            map.putAll(brandAndSpec);
        }


        return map;
    }

    /**
     * 查询列表
     * 高亮显示设置
     * 步骤：1.在哪一列加  2.加前缀 和 后缀
     * @param searchMap 使用Map 实现传入参数的多元化
     * @return  返回一个map 实现传出参数的多元化
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();

        // 构建高亮选线对象 设置高亮域 item_title 如果有多个可以在后面再用addField方法添加
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀 后缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);

        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //循环高亮入口集合
        for (HighlightEntry<TbItem> entry : page.getHighlighted()) {
            //获取高亮列表 (高亮域的个数 是集合，因为可以设置多个，前面代码只设置了一个;get(0)就是 item_title 域)
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            //每个域中可能存在多个值
            for (HighlightEntry.Highlight highlight : highlights) {
                List<String> snipplets = highlight.getSnipplets();
                System.out.println(snipplets);
            }

            //获取原实体类
            TbItem item = entry.getEntity();
            if (entry.getHighlights().size()>0 && entry.getHighlights().get(0).getSnipplets().size()>0){
                //设置高亮结果
                item.setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());

        return map;
    }


    /**
     *  分组查询(查询商品分类列表)
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<>();
        Query query= new SimpleQuery("*:*");
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项  （可以设置多个分组选项）
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取 item_category 分组结果对象
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        // 获取分组结果对象入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            String groupValue = entry.getGroupValue();
            list.add(groupValue);
        }

        return list;
    }


    /**
     * 查询品牌列表和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null){
            //根据模板id查询规格列表和品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("brandList", brandList);
            map.put("specList", specList);
        }

        return  map;
    }

}
