package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.location.IotDeviceLocationSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceLocationDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceLocationMapper;
import com.sydigit.yzwater.module.iot.service.device.dto.IotDeviceLocationNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_LOCATION_EXISTS_CHILDREN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_LOCATION_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_LOCATION_PARENT_ERROR;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_LOCATION_PARENT_NOT_EXISTS;

/**
 * IoT 设备位置 Service 实现类
 */
@Service
@Validated
public class IotDeviceLocationServiceImpl implements IotDeviceLocationService {

    @Resource
    private IotDeviceLocationMapper locationMapper;

    @Override
    public List<IotDeviceLocationNode> getLocationTree() {
        List<IotDeviceLocationDO> all = locationMapper.selectAll();
        if (CollUtil.isEmpty(all)) {
            return List.of();
        }

        Map<Long, IotDeviceLocationNode> nodeMap = new HashMap<>(all.size());
        for (IotDeviceLocationDO location : all) {
            if (location == null || location.getId() == null) {
                continue;
            }
            IotDeviceLocationNode node = new IotDeviceLocationNode();
            node.setId(location.getId());
            node.setName(location.getName());
            node.setSort(location.getSort() == null ? 0 : location.getSort());
            node.setChildren(new ArrayList<>());
            nodeMap.put(location.getId(), node);
        }

        List<IotDeviceLocationNode> roots = new ArrayList<>();
        for (IotDeviceLocationDO location : all) {
            if (location == null || location.getId() == null) {
                continue;
            }
            Long parentId = location.getParentId();
            IotDeviceLocationNode node = nodeMap.get(location.getId());
            if (parentId == null || parentId == 0L || !nodeMap.containsKey(parentId)) {
                if (node != null) {
                    roots.add(node);
                }
                continue;
            }
            IotDeviceLocationNode parent = nodeMap.get(parentId);
            if (parent != null && node != null) {
                parent.getChildren().add(node);
            }
        }

        roots.sort(Comparator
                .comparingInt((IotDeviceLocationNode n) -> n.getSort() == null ? 0 : n.getSort())
                .thenComparing(IotDeviceLocationNode::getId, Comparator.nullsLast(Long::compareTo)));
        roots.forEach(this::sortTree);
        return roots;
    }

    @Override
    public Long createLocation(IotDeviceLocationSaveReqVO createReqVO) {
        IotDeviceLocationDO location = BeanUtils.toBean(createReqVO, IotDeviceLocationDO.class);
        normalizeLocation(location);
        validateParent(null, location.getParentId());
        locationMapper.insert(location);
        return location.getId();
    }

    @Override
    public void updateLocation(IotDeviceLocationSaveReqVO updateReqVO) {
        validateLocationExists(updateReqVO.getId());
        IotDeviceLocationDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceLocationDO.class);
        normalizeLocation(updateObj);
        validateParent(updateObj.getId(), updateObj.getParentId());
        locationMapper.updateById(updateObj);
    }

    @Override
    public void deleteLocation(Long id) {
        if (id == null) {
            return;
        }
        validateLocationExists(id);
        if (locationMapper.selectCountByParentId(id) > 0) {
            throw exception(DEVICE_LOCATION_EXISTS_CHILDREN);
        }
        locationMapper.deleteById(id);
    }

    @Override
    public IotDeviceLocationDO getLocation(Long id) {
        if (id == null) {
            return null;
        }
        return validateLocationExists(id);
    }

    @Override
    public IotDeviceLocationDO validateLocationExists(Long id) {
        IotDeviceLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(DEVICE_LOCATION_NOT_EXISTS);
        }
        return location;
    }

    private void validateParent(Long id, Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (Objects.equals(id, parentId)) {
            throw exception(DEVICE_LOCATION_PARENT_ERROR);
        }
        if (locationMapper.selectById(parentId) == null) {
            throw exception(DEVICE_LOCATION_PARENT_NOT_EXISTS);
        }
    }

    private void normalizeLocation(IotDeviceLocationDO location) {
        if (location.getParentId() == null) {
            location.setParentId(0L);
        }
        if (location.getSort() == null) {
            location.setSort(0);
        }
        location.setName(StrUtil.trimToEmpty(location.getName()));
    }

    private void sortTree(IotDeviceLocationNode node) {
        if (node == null || CollUtil.isEmpty(node.getChildren())) {
            return;
        }
        node.getChildren().sort(Comparator
                .comparingInt((IotDeviceLocationNode n) -> n.getSort() == null ? 0 : n.getSort())
                .thenComparing(IotDeviceLocationNode::getId, Comparator.nullsLast(Long::compareTo)));
        node.getChildren().forEach(this::sortTree);
    }

}
