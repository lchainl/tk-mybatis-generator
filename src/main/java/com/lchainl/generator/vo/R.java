package com.lchainl.generator.vo;

import java.util.HashMap;

/**
 * 页面响应
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */
public class R extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public R() {
		put("code", 0);
		put("msg", "ok");
		put("data", null);
	}

	public static R error() {
		return error(500, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(500, msg);
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		r.put("data", null);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		r.put("data", null);
		return r;
	}

	public static R ok(Object object) {
		R r = new R();
		r.put("data", object);
		r.put("msg", "ok");
		return r;
	}

	public static R ok(String msg, Object object) {
		R r = new R();
		r.put("data", object);
		r.put("msg", msg);
		return r;
	}

	public static R ok() {
		return new R();
	}

	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}