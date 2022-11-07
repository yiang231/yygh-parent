package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.excelListener.DictReadListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
	//需要声明泛型
//	@Autowired
//	private RedisTemplate redisTemplate;
	@Autowired
	private DictReadListener dictReadListener;
	@Autowired
	private DictMapper dictMapper;

	@Cacheable(value = "cmn_dict", key = "'cmn_dict_cache_'+#id")
	public List<Dict> findChildData(Long id) {
		//序列化：将对象的状态转成可存储的或者可以传输的状态
		//redis中获取数据,数据格式存取一致
		/*List<Dict> list = (List<Dict>) redisTemplate.boundValueOps("dict_cache_" + id).get();
		if (!StringUtils.isEmpty(list)) {
			return list;
		}*/
		QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
		dictQueryWrapper.eq("parent_id", id);

//		list = this.list(dictQueryWrapper);
		List<Dict> list = this.list(dictQueryWrapper);
		list.forEach(this::hasChildDict);

		//redis中存数据
		//redisTemplate.boundValueOps("dict_cache_" + id).set(list, 10, TimeUnit.MINUTES);
//		redisTemplate.opsForValue().set("k", "v");
//		redisTemplate.boundHashOps("dict-cache").put(id, list);//Hash结构HashMap<Key,map<key,value>>
//		redisTemplate.boundValueOps("dict_cache").expire(5, TimeUnit.MINUTES);//需要分开设置过期时间
		return list;
	}

	public void hasChildDict(Dict dict) {
		QueryWrapper<Dict> wrapper = new QueryWrapper<>();
		wrapper.eq("parent_id", dict.getId());
		Integer count = baseMapper.selectCount(wrapper);
		//判断当前数据字典对象是否存在子节点
		dict.setHasChildren(count > 0);
	}

	@CacheEvict(value = "cmn_dict", key = "#id", allEntries = true, beforeInvocation = true)
	@Override
	public void importDictData(MultipartFile file) {
		//向MySQL中导入数据前需要将之前的数据缓存进行清空
		//Set<String> keys = redisTemplate.keys("*dict_cache_*");
		//redisTemplate.delete(keys);
		try {
			InputStream is = file.getInputStream();
			//不写sheet默认从第一个表开始读取
			EasyExcel.read(is, DictEeVo.class, dictReadListener).sheet().doRead();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//导出数据字典信息
	@Override
	public void exportDictData(HttpServletResponse response) {
		try {
			//1、文件下载必须要设置的响应头
			response.setContentType("application/vnd.ms-excel");//text/html  ;   json
			response.setCharacterEncoding("utf-8");
			String fileName = URLEncoder.encode("数据字典", "UTF-8");//中文乱码
			response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");//附件形式下载

			//2、查询所有的数据字典
			List<Dict> dictList = dictMapper.selectList(null);

			//3、将Dict转化为DictEeVo
			// 一、大量的遍历的方式
			/*List<DictEeVo> dictEeVoArrayList = new ArrayList<>();
			dictList.forEach(dict -> {
				DictEeVo dictEeVo = new DictEeVo();
				BeanUtils.copyProperties(dict, dictEeVo);
				dictEeVoArrayList.add(dictEeVo);
			});*/
			// 二、采用json转换的方式来进行转化，提高性能
			String jsonString = JSONObject.toJSONString(dictList);//转化Dict数据为json
			List<DictEeVo> dictEeVoList = JSONObject.parseArray(jsonString, DictEeVo.class);//拷贝给DictEeVo
			//4、使用EasyExcel进行文件导出
			EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet().doWrite(dictEeVoList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
