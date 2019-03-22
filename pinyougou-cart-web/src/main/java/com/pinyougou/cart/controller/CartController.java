package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;

import entity.Result;
import pojogroup.Cart;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Reference(timeout=9000)
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	/**
	 * 购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//得到登陆人账号,判断当前是否有人登陆
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//从cookie中获取json对象
		String carListString = util.CookieUtil.getCookieValue(request, "cartList","UTF-8");
		if (carListString==null||carListString.equals("")) {
			carListString="[]";
		}
		//将json对象转换为购物车对象
		List<Cart> cartList_cookie = JSON.parseArray(carListString,Cart.class);
		if (username.equals("anonymousUser")) {//如果没有登录
			System.out.println("从cookie中提取购物车");
			return cartList_cookie;
		}else {//如果登录
			//从redis中提取
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if (cartList_cookie.size()>0) {//判断当本地购物车中存在数据
				//合并购物车
				List<Cart> cartList=cartService.mergeCartList(cartList_redis, cartList_cookie);
				//清楚本地cookie数据
				util.CookieUtil.deleteCookie(request, response, "cartList");
				//将合并的数据存入redis
				cartService.saveCartListToRedis(username, cartList);
				System.out.println("执行了合并购物车的逻辑");
				return cartList;
			}
			return cartList_redis;
		}
		
	
	}
	
	
	@RequestMapping("/addGoodsToCartList")
	//@CrossOrigin(origins="http://localhost:9105")//跨域请求设置
	public Result addGoodsToCartList(Long itemId,Integer num) {
		//跨域请求设置
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		//得到登陆人账号,判断当前是否有人登陆
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录用户"+username);
		try {
			//获取购物车的列表
			List<Cart> cartList = findCartList();
			cartList=cartService.addGoodsToCartList(cartList, itemId, num);
			if (username.equals("anonymousUser")) {//未登录
				//保存到cookie
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600*24, "UTF-8");getClass();
				System.out.println("向cookie中存入数据");
			}else {//如果已经登录,保存到redis
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
	
	
}
