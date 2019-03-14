package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 9000)
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map search(Map searchMap) {
		//关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		if (searchMap.get("keywords").equals("")) {
			return null;
		}
		Map map = new HashMap();
		/*
		 * Query query=new SimpleQuery("*:*"); //添加查询条件 Criteria criteria=new
		 * Criteria("item_keywords").is(searchMap.get("keywords"));
		 * 
		 * query.addCriteria(criteria); ScoredPage<TbItem> page
		 * =solrTemplate.queryForPage(query, TbItem.class);
		 * map.put("rows",page.getContent());
		 */

		// 1.查询列表
		map.putAll(searchMap(searchMap));
		// 2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		// 3.查询品牌和规格列表
		String categoryName = (String) searchMap.get("category");
		if (!"".equals(categoryName)) {// 如果有分类名称

			map.putAll(searchBrandAndSpecList(categoryName));
		} else {// 如果没有分类名称，按照第一个查询
			if (categoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		return map;
	}

	/**
	 * 查询品牌和规格列表
	 * 
	 * @param category
	 *            分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);// 获取模板 ID
		if (typeId != null) {
			// 根据模板 ID 查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);// 返回值添加品牌列表
			// 根据模板 ID 查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);

		}
		return map;
	}

	private Map searchMap(Map searchMap) {
		Map map = new HashMap();
		// 高亮显示
		HighlightQuery query = new SimpleHighlightQuery();
		// 设置高亮域
		HighlightOptions options = new HighlightOptions().addField("item_title");
		options.setSimplePrefix("<em style='color:red'>");// 设置前缀
		options.setSimplePostfix("</em>");// 设置后缀

		query.setHighlightOptions(options);// 设置高亮选项
		// 1.1 按关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 1.2按分类筛选
		if (!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.3按品牌筛选
		if (!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.4过滤规格
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		//1.5按价格筛选
		if(!"".equals(searchMap.get("price"))) {
			String[] price = ((String) searchMap.get("price")).split("-");
			if (!price[0].equals("0")) {//如果区间起点不为0
				Criteria filterCruteria = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCruteria );
				query.addFilterQuery(filterQuery);
			}
			
			if (!price[1].equals("*")) {//如果区间终点不为*
				Criteria filterCruteria = new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCruteria );
				query.addFilterQuery(filterQuery);
			}	
		}
		
		
		//1.6分页查询
		Integer pageNO = (Integer) searchMap.get("pageNo");//提取页码
		if (pageNO==null) {
			pageNO=1;//默认第一页
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
		if(pageSize==null) {
			pageSize=20;//每页记录数默认为20
		}
		query.setOffset((pageNO-1)*pageSize);//从第几条记录查询
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue = (String) searchMap.get("sort");//ASC DESC
		String sortField= (String) searchMap.get("sortField");//排序字段
		if(sortValue!=null && !sortValue.equals("")) {
			if(sortValue.equals("ASC")) {
				Sort sort= new Sort(Sort.Direction.ASC,"item_"+sortField);//升序
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				Sort sort= new Sort(Sort.Direction.DESC,"item_"+sortField);//降序
				query.addSort(sort);
			}
			
		}
		
		// 高亮显示处理
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		for (HighlightEntry<TbItem> h : page.getHighlighted()) {// 循环高亮入口集合
			TbItem item = h.getEntity();// 获取原实体类
			if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));// 设置高亮的结果
			}

		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
		return map;
	}

	/**
	 * 查询分类列表
	 * 
	 * @param searchMap
	 * @return
	 */
	public List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery("*:*");
		// 按关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 设置分组选项
		GroupOptions options = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(options);
		// 得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());// 将分组的结果名称封装到返回值中
		}
		return list;
	}
	
	/**
	 * 加入索引库
	 */
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	
	/**
	 * 从索引库中删除
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品 ID"+goodsIdList);
		Query query=new SimpleQuery(); 
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
