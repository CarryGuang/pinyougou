package com.pinyougou.page.service;


public interface ItemPageService {
	
	/**
	* 商品详细页接口
	* @author Administrator
	*
	*/
	public boolean genItemHtml(Long goodsId);
	
	/**
	 * 删除商品详细页
	 * @param goodsIds
	 * @return
	 */
	public boolean deleteItemHtml(Long[] goodsIds);
}
