package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;

	/**
	 * 查询所有品牌
	 */
	@Override
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
	}
	/**
	 * 品牌分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(),page.getResult());
	}

	/**
	 * 添加品牌
	 */
	@Override
	public void add(TbBrand brand) {

		brandMapper.insert(brand);

	}
	@Override
	public TbBrand findOne(long id) {

		return brandMapper.selectByPrimaryKey(id);
	}
	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);

	}
	@Override
	public void delete(long[] ids) {
		for(long id:ids) {
			brandMapper.deleteByPrimaryKey(id);

		}
			}
	@Override
	public PageResult findPage(TbBrand brand, int pageName, int pageSize) {
		PageHelper.startPage(pageName, pageSize);
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();
		if(brand!=null) {
			if(brand.getName()!=null&&brand.getName().length()>0) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar()!=null&&brand.getFirstChar().length()>0) {
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		return new PageResult(page.getTotal(),page.getResult());
	}
	@Override
	public List<Map> selectOptionList() {
		
		return brandMapper.selectOptionList();
	}
	
		


}
