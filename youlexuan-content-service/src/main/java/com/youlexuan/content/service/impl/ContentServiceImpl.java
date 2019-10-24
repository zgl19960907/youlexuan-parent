package com.youlexuan.content.service.impl;
import java.util.List;

import com.sun.tools.classfile.ConstantPool;
import com.youlexuan.util.CONSTANT;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbContentMapper;
import com.youlexuan.pojo.TbContent;
import com.youlexuan.pojo.TbContentExample;
import com.youlexuan.pojo.TbContentExample.Criteria;
import com.youlexuan.content.service.ContentService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 * redis和mysql的数据同步
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insertSelective(content);
		//清除缓存
		redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//查询修改之前的分类id
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).delete(categoryId);
		contentMapper.updateByPrimaryKey(content);
		//如果分类id发生了修改,清除修改后的id的缓存
		if (categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类id
			redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 先从Redis中查询列表
	 * 如果Redis存在,那么返回
	 * 如果Redis中不存在,则从数据库中查询,并存放到Redis一份
	 * redis中存放数据的格式:hash类型  Redis大KEY固定值 contentList   fileId categoryId fileValue  List<TbContent></>
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).get(categoryId);

		if (contentList==null){
			System.out.println("从数据库读取数据放入缓存");
			//根据广告分类id查询广告列表
			TbContentExample exam = new TbContentExample();
			exam.setOrderByClause("sort_order");//排序
			Criteria criteria = exam.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");//开启状态
			contentList =  contentMapper.selectByExample(exam);
			//存入缓存
			redisTemplate.boundHashOps(CONSTANT.CONTENT_LIST_KEY).put(categoryId,contentList);
		}else{
			System.out.println("从缓存中读取数据");
		}
		return contentList;
	}

}
