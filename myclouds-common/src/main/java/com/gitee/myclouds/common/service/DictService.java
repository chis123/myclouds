package com.gitee.myclouds.common.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson.JSON;
import com.gitee.myclouds.common.util.MyCons;
import com.gitee.myclouds.common.util.MyListUtil;
import com.gitee.myclouds.common.util.MyUtil;
import com.gitee.myclouds.common.vo.DictVO;
import com.gitee.myclouds.common.wrapper.Dtos;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据字典公用服务类	
 * 
 * <p>为了不污染依赖工程，此处不使用@Service注解。由依赖工程的启动类决定是否去@import这个资源。
 * 
 * @author xiongchun
 *
 */
@Slf4j
public class DictService {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	/**
	 * 获取枚举元素列表
	 * 
	 * @param elementKey
	 * @return
	 */
	public List<DictVO> getEnum(String enumKey){
		String key = MyCons.CacheKeyOrPrefix.MyEnum.getValue() + ":" + enumKey;
		List<DictVO> dictVOs = Lists.newArrayList();
		try {
			List<Object> enumObjs = stringRedisTemplate.opsForHash().values(key);
			for (Object obj : enumObjs) {
				dictVOs.add(JSON.parseObject(String.valueOf(obj), DictVO.class));
			}
			//解决不同环境下下拉排序的bug。
			String jql = "SELECT * FROM :MyList ORDER BY sort_no ASC";
			dictVOs = MyListUtil.list(dictVOs, DictVO.class, jql, Dtos.newDto());
		} catch (Exception e) {
			log.error("获取枚举类型：【{}】时反生错误", enumKey);
			e.printStackTrace();
		}
		return dictVOs;
	}
	
	/**
	 * 获取枚举元素对象
	 * 
	 * @param enumKey
	 * @param elementKey
	 * @return
	 */
	public DictVO getDictVO(String enumKey, String elementKey) {
		List<DictVO> myEnumEntities = getEnum(enumKey);
		String jql = "SELECT * FROM :MyList WHERE element_key = :elementKey";
		DictVO dictVO = (DictVO)MyListUtil.selectOne(myEnumEntities, DictVO.class, jql, Dtos.newDto("elementKey", elementKey));
		return dictVO;
	}
	
	/**
	 * 获取枚举元素值
	 * 
	 * @param enumKey
	 * @param elementKey
	 * @return
	 */
	public String getEnumElementValue(String enumKey, String elementKey) {
		DictVO dictVO = getDictVO(enumKey, elementKey);
		return MyUtil.isEmpty(dictVO) ? StringUtils.EMPTY : dictVO.getDict_value();
	}
	
	/**
	 * 获取枚举元素值
	 * 
	 * @param enumKey
	 * @param elementKey
	 * @param defaultValue
	 * @return
	 */
	public String getEnumElementValue(String enumKey, String elementKey, String defaultValue) {
		String value = getEnumElementValue(enumKey, elementKey);
		return MyUtil.isEmpty(value) ? defaultValue : value;
	}
	
}
