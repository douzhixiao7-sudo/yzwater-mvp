package com.sydigit.yzwater.module.system.dal.redis.dict;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.system.dal.dataobject.dict.DictDataDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sydigit.yzwater.module.system.dal.redis.RedisKeyConstants.DICT_DATA_LIST;

/**
 * 字典数据列表的 RedisDAO
 *
 * 说明：
 * - 按 dictType 维度缓存字典数据列表，减少高频接口的数据库压力
 * - 有效期 1 天；当字典数据发生变更时，由业务侧同步刷新缓存
 */
@Repository
public class DictDataRedisDAO {

    /**
     * 缓存有效期：1 天
     */
    private static final long EXPIRES_IN_SECONDS = TimeUnit.DAYS.toSeconds(1);

    private static final TypeReference<List<DictDataDO>> LIST_TYPE = new TypeReference<>() {};

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public List<DictDataDO> getDictDataList(String dictType) {
        String type = StrUtil.trimToNull(dictType);
        if (type == null) {
            return null;
        }
        String redisKey = formatKey(type);
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObjectQuietly(json, LIST_TYPE);
    }

    public void setDictDataList(String dictType, List<DictDataDO> list) {
        String type = StrUtil.trimToNull(dictType);
        if (type == null) {
            return;
        }
        String redisKey = formatKey(type);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(list), EXPIRES_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void deleteDictDataList(String dictType) {
        String type = StrUtil.trimToNull(dictType);
        if (type == null) {
            return;
        }
        stringRedisTemplate.delete(formatKey(type));
    }

    private static String formatKey(String dictType) {
        return String.format(DICT_DATA_LIST, dictType);
    }

}

