import request from '@/config/axios'

/** 备件台账 VO */
export interface SpareVO {
  id?: number
  spareName: string
  spareSpec: string
  spareModel: string
  spareType?: string
  stationId?: string
  deviceId?: string | number
  manufacturer?: string
  stockQty: number
  minStock: number
  storageLocation?: string
  keeperName: string
  spareImages: string[]
  remark?: string
  warning?: boolean
  createTime?: string
}

/** 备件台账分页查询 */
export interface SparePageReqVO {
  pageNo?: number
  pageSize?: number
  spareName?: string
  spareSpec?: string
  spareModel?: string
  spareType?: string
  deviceType?: string
  deviceId?: string | number
  storageLocation?: string
  keeperName?: string
  warning?: boolean
  createTime?: string[]
}

// 备件台账 API
export const SpareApi = {
  // 查询备件台账分页
  getSparePage: async (params: SparePageReqVO) => {
    return await request.get({ url: '/iot/spare/page', params })
  },

  // 查询备件台账详情
  getSpare: async (id: number) => {
    return await request.get({ url: `/iot/spare/get?id=${id}` })
  },

  // 新增备件台账
  createSpare: async (data: SpareVO) => {
    return await request.post({ url: '/iot/spare/create', data })
  },

  // 修改备件台账
  updateSpare: async (data: SpareVO) => {
    return await request.put({ url: '/iot/spare/update', data })
  },

  // 删除备件台账
  deleteSpare: async (id: number) => {
    return await request.delete({ url: `/iot/spare/delete?id=${id}` })
  },

  // 导出备件台账
  exportSpareExcel: async (params: SparePageReqVO) => {
    return await request.download({ url: '/iot/spare/export-excel', params })
  },

  // 获取备件台账精简列表
  getSpareSimpleList: async () => {
    return await request.get({ url: '/iot/spare/simple-list' })
  }
}
