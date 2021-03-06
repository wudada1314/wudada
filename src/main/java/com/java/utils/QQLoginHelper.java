package com.java.utils;

import java.util.HashMap;
import java.util.Map;


import com.alibaba.fastjson.JSONObject;
import com.java.entity.ThirdPartyUser;

/**
 * 第三方登录辅助类
 * 
 * @author ShenHuaJie
 * @version 2016年5月20日 下午3:44:45
 */
public final class QQLoginHelper {


	/**
	 * 获取QQ的认证token和用户OpenID
	 * 
	 * @param code
	 * @param
	 * @return openId,accessToken
	 */
	public static final Map<String, String> getQQTokenAndOpenid(
			String tokenUrl,String grantType,String clientId,String clientSecret,
																String code,
			String redirectUrl,String openIdUrl) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		// 获取令牌
		Map<String,String> params = new HashMap<>();
		params.put("grant_type",grantType);
		params.put("client_id",clientId);
		params.put("client_secret",clientSecret);
		params.put("code",code);
		params.put("redirect_uri",redirectUrl);
		String tokenRes = HttpUtils.get(tokenUrl,params);
		if (tokenRes != null && tokenRes.indexOf("access_token") > -1) {
			Map<String, String> tokenMap = toMap(tokenRes);
			map.put("accessToken", tokenMap.get("access_token"));
			System.out.println("accessToken:"+tokenMap.get("access_token"));
			// 获取QQ用户的唯一标识openID
			openIdUrl = openIdUrl + "?access_token=" + tokenMap.get("access_token");
			String openIdRes = HttpUtils.get(openIdUrl);
			int i = openIdRes.indexOf("(");
			int j = openIdRes.indexOf(")");
			openIdRes = openIdRes.substring(i + 1, j);
			JSONObject openidObj = JSONObject.parseObject(openIdRes);
			map.put("openId", openidObj.getString("openid"));
			System.out.println("-------openid:"+openidObj.getString("openid"));
		} else {
			throw new IllegalArgumentException("获取token失败");
		}
		return map;
	}

	/**
	 * 获取QQ用户信息
	 *
	 * @param token
	 * @param openid
	 */
	public static final ThirdPartyUser getQQUserinfo(String getUserInfoUrl,String openid,String token,String clientId) throws Exception {
		ThirdPartyUser user = new ThirdPartyUser();
		user.setToken(token);
		user.setOpenid(openid);
		Map<String,String> params = new HashMap<>();
		params.put("openid",openid);
		params.put("access_token",token);
		params.put("oauth_consumer_key",clientId);
		/*String url = "https://graph.qq.com/user/get_user_info";
		url = url + "?format=json&access_token=" + token + "&oauth_consumer_key="
				+ "101432824" + "&openid=" + openid;*/
		String res = HttpUtils.get(getUserInfoUrl,params);
		JSONObject json = JSONObject.parseObject(res);
		if (json.getIntValue("ret") == 0) {
			user.setUserName(json.getString("nickname"));
			String img = json.getString("figureurl_qq_2");
			if (img == null || "".equals(img)) {
				img = json.getString("figureurl_qq_1");
			}
			user.setAvatarUrl(img);
			String sex = json.getString("gender");
			if ("女".equals(sex)) {
				user.setGender("0");
			} else {
				user.setGender("1");
			}
		} else {
			throw new IllegalArgumentException(json.getString("msg"));
		}
		return user;
	}



	/**
	 * 将格式为s1&s2&s3...的字符串转化成Map集合
	 * 
	 * @param str
	 * @return
	 */
	private static final Map<String, String> toMap(String str) {
		Map<String, String> map = new HashMap<String, String>();
		String[] strs = str.split("&");
		for (int i = 0; i < strs.length; i++) {
			String[] ss = strs[i].split("=");
			map.put(ss[0], ss[1]);
		}
		return map;
	}
}
