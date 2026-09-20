import request from '@/config/axios'

/** 备件盘点记录 VO */
export interface SpareCheckVO {
  id?: number
  spareId?: number
  spareName?: string
  spareSpec?: string
  spareModel?: string
  checkTime?: string
  systemQty?: number
  actualQty: number
  diffQty?: number
  resultStatus?: string
  checkerUserName?: string
  applied?: boolean
  applyUserName?: string
  applyTime?: string
  remark?: string
  createTime?: string
}

/** 盘点新增请求 */
export interface SpareCheckSaveReqVO {
  id?: number
  spareId?: number
  checkTime?: number | string
  actualQty?: number
  applyResult?: boolean
  remark?: string
}

/** 盘点分页查询 */
export interface SpareCheckPageReqVO {
  pageNo?: number
  pageSize?: number
  spareId?: number
  resultStatus?: string
  applied?: boolean
  checkTime?: string[]
  createTime?: string[]
}

// 备件盘点 API
export const SpareCheckApi = {
  // 查询盘点分页
  getSpareCheckPage: async (params: SpareCheckPageReqVO) => {
    return await request.get({ url: '/iot/spare-check/page', params })
  },

  // 查询盘点详情
  getSpareCheck: async (id: number) => {
    return await request.get({ url: `/iot/spare-check/get?id=${id}` })
  },

  // 新增盘点记录
  createSpareCheck: async (data: SpareCheckSaveReqVO) => {
    return await request.post({ url: '/iot/spare-check/create', data })
  },

  // 修改盘点记录
  updateSpareCheck: async (data: SpareCheckSaveReqVO) => {
    return await request.put({ url: '/iot/spare-check/update', data })
  },

  // 删除盘点记录
  deleteSpareCheck: async (id: number) => {
    return await request.delete({ url: `/iot/spare-check/delete?id=${id}` })
  },

  // 反馈盘点结果
  applySpareCheck: async (id: number) => {
    return await request.put({ url: `/iot/spare-check/apply?id=${id}` })
  },

  // 反审批盘点结果
  reverseApplySpareCheck: async (id: number) => {
    return await request.put({ url: `/iot/spare-check/reverse-apply?id=${id}` })
  }
}
