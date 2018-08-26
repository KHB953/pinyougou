package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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

    @Reference




    /**
     * 搜索 item_keywords
     *  技巧：搜索方法 中将每个功能分为几块,将每一小块作为为私有方法，而搜索方法作为主线来调用,便于后期维护
     *
     * @param searchMap 使用Map 实现传入参数的多元化
     * @return 返回一个map 实现传出参数的多元化
     *  注意 1、查询列表2、分组查询 3、查询品牌规格列表   放在search.html页面的位置
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map = new HashMap<>();

        //1、查询列表 putAll 将查询列表的map 最近到新的map中
        map.putAll(searchList(searchMap));

        //2、分组查询 商品分类列表
        map.put("categoryList", searchCategoryList(searchMap));

        //3、查询品牌和规格列表
        //如果已经选择分类名称就按分类名称查询
        String categoryName = (String) searchMap.get("category");
        if (! categoryName.equals("")){
            Map brandAndSpec = searchBrandAndSpecList(categoryName);
            map.putAll(brandAndSpec);
        }else {
            //否则按第一个分类来查询
            if (searchCategoryList(searchMap).size() > 0) {
                Map brandAndSpec = searchBrandAndSpecList(searchCategoryList(searchMap).get(0));
                map.putAll(brandAndSpec);
            }
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

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        //1.1 根据关键字条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 添加商品分类过滤条件
        if(! "".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 添加品牌过滤条件
        if(! "".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 添加规格过滤条件
        if(searchMap.get("spec") != null){
            Map<String,String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {

                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5添加价格过滤条件
        if(! "".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");

            if (! prices[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            if (! prices[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo== null){
            //默认从第一页开始
            pageNo =1;
        }

        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null){
            //默认每页数据条数为20
            pageSize =20;
        }
        //设置从第几条数据查询
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //1.7 排序
        // 升序或降序
        String sortValue = (String) searchMap.get("sort");
        // 排序字段
        String sortField = (String) searchMap.get("sortField");
        if (sortValue != null && ! sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }




        //执行查询获取高亮结果集
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

        // 总页数和和总数据条数返回
        map.put("totalPages", page.getTotalPages());
        map.put("total", page.getTotalElements());

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

    /**
     * 导入数据
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }


    /**
     * 删除数据 (索引数据)
     *
     * @param goodsIds
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        System.out.println("删除商品id的索引"+goodsIds);
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
