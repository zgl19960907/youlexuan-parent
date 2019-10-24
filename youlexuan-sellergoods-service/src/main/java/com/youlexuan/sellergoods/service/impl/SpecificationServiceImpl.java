package com.youlexuan.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.youlexuan.mapper.TbSpecificationOptionMapper;
import com.youlexuan.pojo.TbSpecificationOption;
import com.youlexuan.pojo.TbSpecificationOptionExample;
import com.youlexuan.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbSpecificationMapper;
import com.youlexuan.pojo.TbSpecification;
import com.youlexuan.pojo.TbSpecificationExample;
import com.youlexuan.pojo.TbSpecificationExample.Criteria;
import com.youlexuan.sellergoods.service.SpecificationService;

import com.youlexuan.entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specOptMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}


	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//规格基本信息
		TbSpecification tbSpec = specification.getSpecification();
		specificationMapper.insert(tbSpec);
		//增加规格项信息
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptList();
		//加工外键,外键是mysql自增得到的
		for(TbSpecificationOption specOpt:specificationOptionList){
			specOpt.setSpecId(tbSpec.getId());
			specOptMapper.insertSelective(specOpt);
		}
	}

	
	/**
	 * 修改
	 * 1.修改规格表信息
	 * 2.将规格项表中关联的所有字段删除
	 * 3.将新增的关联规格项加到数据库
	 */
	@Override
	public void update(Specification specification){

		TbSpecification tbSpec = specification.getSpecification();
		//1
		specificationMapper.updateByPrimaryKeySelective(tbSpec);
		//2
		TbSpecificationOptionExample exam = new TbSpecificationOptionExample();
		exam.createCriteria().andSpecIdEqualTo(tbSpec.getId());
		specOptMapper.deleteByExample(exam);
		List<TbSpecificationOption> newSpecOptList = specification.getSpecificationOptList();
		//3
		for(TbSpecificationOption specOpt : newSpecOptList){
			specOpt.setSpecId(tbSpec.getId());//设置外键
			specOptMapper.insertSelective(specOpt);
		}
	}	
	
	/**
	 * 根据ID获取包装后的实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		TbSpecificationOptionExample exam = new TbSpecificationOptionExample();
		exam.createCriteria().andSpecIdEqualTo(id);
		List<TbSpecificationOption> tbSpecificationOptions = specOptMapper.selectByExample(exam);
		return new Specification(tbSpecification,tbSpecificationOptions);
	}

	/**
	 * 批量删除
	 *
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//删除原有的规格项
			TbSpecificationOptionExample exam = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = exam.createCriteria();
			criteria.andSpecIdEqualTo(id);//指定规格ID为条件
			specOptMapper.deleteByExample(exam);//删除
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}
