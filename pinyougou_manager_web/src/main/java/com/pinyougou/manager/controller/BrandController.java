package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 *
 * @author hongbin
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    /**
     * 品牌远程service注入
     */
    @Reference
    private BrandService brandService;

    /**
     * 查询所有
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param rows 当前页的记录
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page,int rows){
        return brandService.fandPage(page, rows);
    }

    /**
     * 新增
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true, "添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败！");
        }

    }

    /**
     * 删除
     * @param ids Long数组
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败！");
        }
    }


    /**
     * 更新
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true, "更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败！");
        }
    }

    /**
     * 修改回显
     * @param id
     * @return
     */
    @RequestMapping("/selectOne")
    public TbBrand selectOne(Long id){
        return brandService.getById(id);
    }


    /**
     * 分页条件查询
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int page,int rows){
        return brandService.search(brand,page, rows);
    }

    /**
     * 品牌列表数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
