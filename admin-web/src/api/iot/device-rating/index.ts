import request from '@/config/axios'

/** 设备评级 VO */
export interface DeviceRatingVO {
  id?: number
  deviceId?: number | string
  ratingTime?: string
  ratingUserId?: number
  ratingUserName?: string
  ratingBasis?: string
  ratingResult?: string
  rectifyAdvice?: string
  rectifyDeadline?: string
  attachments?: string[]
  createTime?: string
}

/** 设备评级分页查询 */
export interface DeviceRatingPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number | string
  ratingResult?: string
  ratingUserName?: string
  ratingTime?: string[]
}

// 设备评级 API
export const DeviceRatingApi = {
  // 查询设备评级分页
  getDeviceRatingPage: async (params: DeviceRatingPageReqVO) => {
    return await request.get({ url: '/iot/device-rating/page', params })
  },

  // 查询设备评级详情
  getDeviceRating: async (id: number) => {
    return await request.get({ url: `/iot/device-rating/get?id=${id}` })
  },

  // 创建设备评级
  createDeviceRating: async (data: DeviceRatingVO) => {
    return await request.post({ url: '/iot/device-rating/create', data })
  },

  // 更新设备评级
  updateDeviceRating: async (data: DeviceRatingVO) => {
    return await request.put({ url: '/iot/device-rating/update', data })
  },

  // 删除设备评级
  deleteDeviceRating: async (id: number) => {
    return await request.delete({ url: `/iot/device-rating/delete?id=${id}` })
  },

  // 导出设备评级
  exportDeviceRatingExcel: async (params: DeviceRatingPageReqVO) => {
    return await request.download({ url: '/iot/device-rating/export-excel', params })
  }
}
