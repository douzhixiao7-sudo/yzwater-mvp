import request from '@/config/axios'

/** 技术资料 VO */
export interface DeviceDocVO {
  id?: number
  deviceId?: number | string
  deviceCode?: string
  deviceName?: string
  equipmentModel?: string
  deviceType?: string
  productCategory?: string
  docType?: string
  docName?: string
  fileId?: number
  fileUrl?: string
  fileFormat?: string
  remark?: string
  creator?: string
  createTime?: string | number
  updater?: string
  updateTime?: string | number
}

/** 技术资料分页 Request VO */
export interface DeviceDocPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number | string
  deviceCode?: string
  deviceName?: string
  deviceType?: string
  docType?: string
  docName?: string
  fileFormat?: string
  createTime?: string[]
}

/** 技术资料新增/修改 Request VO */
export interface DeviceDocSaveReqVO {
  id?: number
  deviceId: number | string
  docType: string
  docName: string
  fileUrl: string
  fileFormat?: string
  fileId?: number
  remark?: string
  createTime?: string | number
}

// 技术资料 API
export const DeviceDocApi = {
  // 获取技术资料分页
  getDeviceDocPage: async (params: DeviceDocPageReqVO) => {
    return await request.get({ url: '/iot/device-doc/page', params })
  },

  // 获取技术资料详情
  getDeviceDoc: async (id: number) => {
    return await request.get({ url: `/iot/device-doc/get?id=${id}` })
  },

  // 新增技术资料
  createDeviceDoc: async (data: DeviceDocSaveReqVO) => {
    return await request.post({ url: '/iot/device-doc/create', data })
  },

  // 修改技术资料
  updateDeviceDoc: async (data: DeviceDocSaveReqVO) => {
    return await request.put({ url: '/iot/device-doc/update', data })
  },

  // 删除技术资料
  deleteDeviceDoc: async (id: number) => {
    return await request.delete({ url: `/iot/device-doc/delete?id=${id}` })
  },

  // 导出技术资料 Excel
  exportDeviceDocExcel: async (params: DeviceDocPageReqVO) => {
    return await request.download({ url: '/iot/device-doc/export-excel', params })
  }
}
