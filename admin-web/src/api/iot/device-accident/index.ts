import request from '@/config/axios'

/** 设备事故登记 VO */
export interface DeviceAccidentVO {
  id?: number
  deviceId?: string | number
  accidentTime?: string
  accidentLocation?: string
  accidentType?: string
  accidentDesc?: string
  handleResult?: string
  lossAssessment?: string
  responsibleUserId?: number
  responsibleName?: string
  attachments?: string[]
  creator?: string
  creatorName?: string
  createTime?: string
  updater?: string
  updateTime?: string
}

/** 设备事故分页 Request VO */
export interface DeviceAccidentPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: string | number
  accidentType?: string
  responsibleName?: string
  accidentTime?: string[]
}

// 设备事故登记 API
export const DeviceAccidentApi = {
  // 分页查询设备事故
  getDeviceAccidentPage: async (params: DeviceAccidentPageReqVO) => {
    return await request.get({ url: '/iot/device-accident/page', params })
  },

  // 获取设备事故详情
  getDeviceAccident: async (id: number) => {
    return await request.get({ url: `/iot/device-accident/get?id=${id}` })
  },

  // 新增设备事故
  createDeviceAccident: async (data: DeviceAccidentVO) => {
    return await request.post({ url: '/iot/device-accident/create', data })
  },

  // 修改设备事故
  updateDeviceAccident: async (data: DeviceAccidentVO) => {
    return await request.put({ url: '/iot/device-accident/update', data })
  },

  // 删除设备事故
  deleteDeviceAccident: async (id: number) => {
    return await request.delete({ url: `/iot/device-accident/delete?id=${id}` })
  },

  // 导出设备事故 Excel
  exportDeviceAccidentExcel: async (params: DeviceAccidentPageReqVO) => {
    return await request.download({ url: '/iot/device-accident/export-excel', params })
  }
}