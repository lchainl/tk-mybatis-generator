package com.lchainl.generator.controller;

import com.alibaba.fastjson.JSON;
import com.lchainl.generator.service.GeneratorService;
import com.lchainl.generator.vo.R;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口
 *
 * @author ethan
 * @email xuyangit@163.com
 * @date
 */
@RequestMapping("/generator")
@Controller
public class GeneratorController {

	@Autowired
	GeneratorService generatorService;

	/**
	 * 获取所有表名
	 *
	 * @return
	 */
	@ResponseBody
	@GetMapping("/list")
	R list() {
		List<Map<String, Object>> list = generatorService.list();
		return R.ok(list);
	};

	/**
	 * 根据单个表名生成代码
	 *
	 * @param response
	 * @param tableName 表名称
	 * @throws IOException
	 */
	@RequestMapping("/{tableName}")
	public void code(HttpServletResponse response,
                     @PathVariable("tableName") String tableName) throws IOException {
		String[] tableNames = new String[] { tableName };
		byte[] data = generatorService.generatorCode(tableNames);
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=\"lchainl_code.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");

		IOUtils.write(data, response.getOutputStream());
	}

	/**
	 * 批量生成代码
	 *
	 * @param response
	 * @param tables 多个表名
	 * @throws IOException
	 */
	@RequestMapping("/batchCode")
	public void batchCode(HttpServletResponse response, String tables) throws IOException {

		String[] tableNames = new String[] {};
		tableNames = JSON.parseArray(tables).toArray(tableNames);
		byte[] data = generatorService.generatorCode(tableNames);
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=\"lchainl_code.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");

		IOUtils.write(data, response.getOutputStream());
	}

	/**
	 * 批量生成所有表
	 *
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/allTables")
	public void allTables(HttpServletResponse response) throws IOException {
		List<Map<String, Object>> list = generatorService.list();

		List<String> tableList = list.stream().map(table -> table.get("tableName").toString()).collect(Collectors.toList());

		byte[] data = generatorService.generatorCode(tableList.toArray(new String[list.size()]));
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=\"lchainl_code.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");

		IOUtils.write(data, response.getOutputStream());
	}

}
