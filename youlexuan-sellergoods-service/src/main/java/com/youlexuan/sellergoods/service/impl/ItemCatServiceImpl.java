package com.youlexuan.sellergoods.service.impl;
import java.util.List;

import com.youlexuan.util.CONSTANT;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbItemCatMapper;
import com.youlexuan.pojo.TbItemCat;
import com.youlexuan.pojo.TbItemCatExample;
import com.youlexuan.pojo.TbItemCatExample.Criteria;
import com.youlexuan.sellergoods.service.ItemCatService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 商品类目服务实现层
 * @author Administrator
 *
 */
@Service(timeout = 100000)
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据上级id查询列表
	 * @param parentId
	 * @return
	 */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample exam = new TbItemCatExample();
		Criteria criteria = exam.createCriteria();
		criteria.andParentIdEqualTo(parentId);

		if (parentId.doubleValue()==0){
			findAll();
		}
		return itemCatMapper.selectByExample(exam);
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		List<TbItemCat> list = itemCatMapper.selectByExample(null);
		for (TbItemCat itemCat : list){
			redisTemplate.boundHashOps(CONSTANT.ITEMCAT_LIST_KEY).put(itemCat.getName(),itemCat.getTypeId());
		}
		return list;
	}

	/**
	 * 按分页查询
	 * 运营商后台已查询分类列表就将常用数据进行缓存
	 *   key		value
	 *   分类名---->模板ID
	 *   --------------------
	 *   模板ID---->品牌列表
	 *   模板ID---->规格列表
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
