package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌服务层接口
 * @author hongbin
 */
public interface BrandService {

    /**
     * 返回全部列表
     * @return
     */
    public List<TbBrand> findAll();


    /**
     * 查询分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult fandPage(int pageNum,int pageSize);

    /**
     * 增加
     * @param brand
     */
    void add(TbBrand brand);

    /**
     * 删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 更新
     * @param brand
     */
    void update(TbBrand brand);

    /**
     * 根据Id获取
     * @param id
     */
    TbBrand getById(Long id);

    /**
     * 分页条件查询
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult search(TbBrand brand,int pageNum,int pageSize);

    /**
     * 获取品牌列表
     * @return
     */
    List<Map> selectOptionList();
}
