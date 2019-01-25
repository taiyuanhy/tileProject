package com.uinv.gis.tileProject;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 拦截器,主要用于处理跨域和返回头所允许的内容
 * @author HY
 *
 */
public class MyInterceptor implements HandlerInterceptor {
	/**
	 * response添加头,解决跨域问题,允许请求头中有token
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials","true");
        response.addHeader("Access-Control-Allow-Headers", "Authentication,Origin,X-Requested-With,Content-Type,Accept,token,UserName,headimgurl,OpenId");
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
}
