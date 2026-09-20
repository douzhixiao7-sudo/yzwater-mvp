package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcySaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZzbcyDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZzbcyMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 防汛抗旱组织部成员服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxZzbcyService {

    private final YzFxZzbcyMapper zzbcyMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 列表查询（按岗位正序）
     */
    public List<FxZzbcyListRespVO> getList(FxZzbcyListReqVO reqVO) {
        String name = reqVO == null ? null : StrUtil.trimToNull(reqVO.getName());
        String title = reqVO == null ? null : StrUtil.trimToNull(reqVO.getTitle());
        List<YzFxZzbcyDO> list = zzbcyMapper.selectListOrderByPosition(name, title);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 导出列表（岗位转中文标签）
     */
    public List<FxZzbcyExportExcelVO> getExportList() {
        List<YzFxZzbcyDO> list = zzbcyMapper.selectListOrderByPosition();
        Map<String, String> positionMap = loadDictLabelMap(ZdConstants.ZD_ZZBGW);
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> buildExportExcelVO(item, positionMap))
                .toList();
    }

    /**
     * 导入成员 Excel
     */
    @Transactional(rollbackFor = Exception.class)
    public FxZzbcyImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<FxZzbcyImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), FxZzbcyImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "Excel 解析失败"));
        }

        FxZzbcyImportRespVO respVO = new FxZzbcyImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        Map<String, String> positionValueMap = loadDictValueMapByLabel(ZdConstants.ZD_ZZBGW);
        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            FxZzbcyImportExcelVO row = rows.get(i);
            int excelRowNo = i + 2;
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            try {
                String name = normalizeText(row.getName());
                if (StrUtil.isBlank(name)) {
                    respVO.addError("第" + excelRowNo + "行失败：姓名不能为空");
                    continue;
                }
                YzFxZzbcyDO insert = new YzFxZzbcyDO();
                insert.setId(generateId());
                insert.setName(name);
                insert.setTitle(normalizeText(row.getTitle()));
                // 根据字典中文标签反查 value，未命中置空字符串，不影响入库
                insert.setPosition(resolvePositionValue(row.getPositionLabel(), positionValueMap));
                zzbcyMapper.insert(insert);
                successCount++;
            } catch (Exception ex) {
                respVO.addError("第" + excelRowNo + "行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }
        respVO.setSuccessCount(successCount);
        return respVO;
    }

    /**
     * 详情
     */
    public FxZzbcySaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZzbcyDO exists = zzbcyMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "组织部成员不存在或已删除");
        }
        FxZzbcySaveReqVO vo = new FxZzbcySaveReqVO();
        vo.setId(exists.getId());
        vo.setPosition(exists.getPosition());
        vo.setName(exists.getName());
        vo.setTitle(exists.getTitle());
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxZzbcySaveReqVO reqVO) {
        String id = generateId();
        YzFxZzbcyDO insert = new YzFxZzbcyDO();
        insert.setId(id);
        fillFields(insert, reqVO);
        zzbcyMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxZzbcySaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZzbcyDO exists = zzbcyMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "组织部成员不存在或已删除");
        }
        YzFxZzbcyDO update = new YzFxZzbcyDO();
        update.setId(id);
        fillFields(update, reqVO);
        zzbcyMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("成员ID不能为空");
        }
        YzFxZzbcyDO exists = zzbcyMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "组织部成员不存在或已删除");
        }
        zzbcyMapper.deleteById(key);
    }

    private FxZzbcyListRespVO buildListResp(YzFxZzbcyDO item) {
        FxZzbcyListRespVO vo = new FxZzbcyListRespVO();
        vo.setId(item.getId());
        vo.setPosition(item.getPosition());
        vo.setName(item.getName());
        vo.setTitle(item.getTitle());
        return vo;
    }

    private FxZzbcyExportExcelVO buildExportExcelVO(YzFxZzbcyDO item, Map<String, String> positionMap) {
        FxZzbcyExportExcelVO vo = new FxZzbcyExportExcelVO();
        vo.setPositionLabel(resolveLabel(item.getPosition(), positionMap));
        vo.setName(item.getName());
        vo.setTitle(item.getTitle());
        return vo;
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private String resolvePositionValue(String rawLabel, Map<String, String> dictMap) {
        String label = normalizeText(rawLabel);
        if (StrUtil.isBlank(label)) {
            return "";
        }
        return StrUtil.blankToDefault(dictMap.get(label), "");
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            if (dict == null || StrUtil.isBlank(dict.getValue())) {
                continue;
            }
            result.put(dict.getValue(), StrUtil.blankToDefault(dict.getLabel(), dict.getValue()));
        }
        return result;
    }

    private Map<String, String> loadDictValueMapByLabel(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            if (dict == null) {
                continue;
            }
            String value = normalizeText(dict.getValue());
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String label = normalizeText(dict.getLabel());
            if (StrUtil.isNotBlank(label)) {
                result.putIfAbsent(label, value);
            }
            result.putIfAbsent(value, value);
        }
        return result;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private boolean isEmptyRow(FxZzbcyImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getName()),
                normalizeText(row.getTitle()),
                normalizeText(row.getPositionLabel())
        );
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        normalized = StrUtil.removePrefix(normalized, "\"");
        normalized = StrUtil.removeSuffix(normalized, "\"");
        normalized = StrUtil.removePrefix(normalized, "'");
        normalized = StrUtil.removeSuffix(normalized, "'");
        return StrUtil.trimToNull(normalized);
    }

    private void fillFields(YzFxZzbcyDO target, FxZzbcySaveReqVO source) {
        target.setPosition(StrUtil.trimToNull(source.getPosition()));
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setTitle(StrUtil.trimToNull(source.getTitle()));
        target.setSort(source.getSort());
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
