package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 * @author hongbin
 */
public interface ItemSearchService {

    /**
     *  搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);


    /**
     * 导入数据 (索引数据)
     * @param list
     */
    public void importList(List list);

    /**
     * 删除数据 (索引数据)
     * @param
     */
    public void deleteByGoodsIds(List goodsIds);

}
