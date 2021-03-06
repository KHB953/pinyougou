package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import util.Constant;

import java.util.*;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 * Goods 有三个属性
	 */
	@Override
	public void add(Goods goods) {
		//设置成为申请状态
		goods.getGoods().setAuditStatus(Constant.GOODS_STATUS_NOT_APPL);
		//插入商品表
		goodsMapper.insert(goods.getGoods());

		//设置商品描述表的商品id
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//插入商品描述表
		goodsDescMapper.insert(goods.getGoodsDesc());
		//插入商品SKU列表
		saveItemList(goods);


	}

	/**
	 * 插入SKU列表数据
	 * 方法抽取
	 * @param goods
	 */
	private void saveItemList(Goods goods) {
		if (Constant.GOODS_ISENABLESPEC.equals(goods.getGoods().getIsEnableSpec())){

			//itemList  [{"spec":{"网络":"电信2G","机身内存":"16G"},"price":0,"num":99999,"status":"0","isDefault":"0"}]
			List<TbItem> itemList = goods.getItemList();
			for (TbItem item : itemList) {
				// 创建标题 SPU名称+规格选项值
				String title =goods.getGoods().getGoodsName();
				Map<String,Object> map= JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title +=" "+map.get(key);
				}
				item.setTitle(title);


				setItemValues(goods, item);
				itemMapper.insert(item);
			}
		} else {
			TbItem item = new TbItem();
			//设置标题、价格、状态、默认 库存数、规格
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice(goods.getGoods().getPrice());
			item.setStatus(Constant.ITEM_STATUS_UP);
			item.setIsDefault("1");
			item.setNum(99999);
			item.setSpec("{}");

			setItemValues(goods, item);
			itemMapper.insert(item);

		}
	}

	/**
	 * 保存item  通用部分抽取
	 * @param goods
	 * @param item
	 */
	private void setItemValues(Goods goods, TbItem item) {
		//设置商品分类 三级分类ID
		item.setCategoryid(goods.getGoods().getCategory3Id());
		// 创建日期 更新日期
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		//商品id  商家id
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());
		//分类名称 通过分类id获取
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(tbItemCat.getName());
		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		// 商家名称 nickname
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSellerId(seller.getNickName());



		//设置图片名称 设置第一张图片的名称
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size()>0){
			item.setImage((String)imageList.get(0).get("url"));
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//如果修改了商品 状态需要重新设置为 未申请 状态
		goods.getGoods().setAuditStatus(Constant.GOODS_STATUS_NOT_APPL);

		//保存商品列表
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		// 保存商品扩展表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		// 原有SKU列表先删除;再添加
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		// 添加新的SKU列表
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体 goods goodsDesc itemList
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

		goods.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量 逻辑删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete(Constant.GOODS_ISDELETE);
			goodsMapper.updateByPrimaryKey(tbGoods);

		}		
	}
	
	
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//根据商家id精确查找
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){

				//未删除状态为"0" 删除状态为"1"
				criteria.andIsDeleteEqualTo("0");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	/**
	 * 批量更新状态
	 *
	 * @param ids
	 * @param status
	 */
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	/**
	 * 通过商品id查和状态查询item列表
	 *
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		return tbItems;
	}
}
