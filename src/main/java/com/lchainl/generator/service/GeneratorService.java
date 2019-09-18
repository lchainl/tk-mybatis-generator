/**
 * 
 */
package com.lchainl.generator.service;

import java.util.List;
import java.util.Map;

/**
 * @author lchainl@163.com
 * @Time 2018年9月6日
 * @description
 * 
 */
public interface GeneratorService {
	List<Map<String, Object>> list();

	byte[] generatorCode(String[] tableNames);
}
