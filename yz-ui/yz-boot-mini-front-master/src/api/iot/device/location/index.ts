import request from '@/config/axios'

// IoT 设备位置树节点 VO
export interface DeviceLocationNodeRespVO {
  id: string | number // 位置编号
  name: string // 位置名称
  sort?: number // 排序
  children?: DeviceLocationNodeRespVO[] // 子节点
}

// IoT 设备位置详情 VO
export interface DeviceLocationRespVO {
  id: string | number // 位置编号
  parentId: string | number // 父级编号
  name: string // 位置名称
  sort?: number // 排序
  createTime?: string // 创建时间
}

// IoT 设备位置新增/修改 VO
export interface DeviceLocationSaveReqVO {
  id?: string | number // 位置编号
  parentId: string | number // 父级编号
  name: string // 位置名称
  sort?: number // 排序
}

// IoT 设备位置 API
export const DeviceLocationApi = {
  // 获得位置树
  getDeviceLocationTree: async () => {
    return await request.get<DeviceLocationNodeRespVO[]>({ url: `/iot/device-location/tree` })
  },

  // 获得位置详情
  getDeviceLocation: async (id: string | number) => {
    return await request.get<DeviceLocationRespVO>({ url: `/iot/device-location/get?id=` + id })
  },

  // 创建位置
  createDeviceLocation: async (data: DeviceLocationSaveReqVO) => {
    return await request.post({ url: `/iot/device-location/create`, data })
  },

  // 更新位置
  updateDeviceLocation: async (data: DeviceLocationSaveReqVO) => {
    return await request.put({ url: `/iot/device-location/update`, data })
  },

  // 删除位置
  deleteDeviceLocation: async (id: string | number) => {
    return await request.delete({ url: `/iot/device-location/delete?id=` + id })
  }
}
