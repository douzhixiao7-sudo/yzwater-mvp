import request from '@/config/axios'

/** 备件出入库记录 VO */
export interface SpareIoVO {
  id?: number
  spareId?: number
  spareName?: string
  spareSpec?: string
  spareModel?: string
  stockQty?: number
  ioType?: string
  ioTime?: string
  ioQty?: number
  usageType?: string
  usageId?: number
  operatorUserId?: number
  operatorName?: string
  auditStatus?: string
  auditUserId?: number
  auditUserName?: string
  auditTime?: string
  auditRemark?: string
  remark?: string
  createTime?: string
}

/** 出入库审核请求 */
export interface SpareIoAuditReqVO {
  id: number
  auditStatus: string
  auditRemark?: string
}

/** 出入库分页查询 */
export interface SpareIoPageReqVO {
  pageNo?: number
  pageSize?: number
  spareId?: number
  ioType?: string
  auditStatus?: string
  usageType?: string
  deviceType?: string
  deviceId?: string | number
  operatorName?: string
  ioTime?: string[]
  createTime?: string[]
}

// 备件出入库 API
export const SpareIoApi = {
  // 查询出入库分页
  getSpareIoPage: async (params: SpareIoPageReqVO) => {
    return await request.get({ url: '/iot/spare-io/page', params })
  },

  // 查询出入库详情
  getSpareIo: async (id: number) => {
    return await request.get({ url: `/iot/spare-io/get?id=${id}` })
  },

  // 新增出入库记录
  createSpareIo: async (data: SpareIoVO) => {
    return await request.post({ url: '/iot/spare-io/create', data })
  },

  // 修改出入库记录
  updateSpareIo: async (data: SpareIoVO) => {
    return await request.put({ url: '/iot/spare-io/update', data })
  },

  // 删除出入库记录
  deleteSpareIo: async (id: number) => {
    return await request.delete({ url: `/iot/spare-io/delete?id=${id}` })
  },

  // 审核出入库记录
  auditSpareIo: async (data: SpareIoAuditReqVO) => {
    return await request.put({ url: '/iot/spare-io/audit', data })
  },

  // 导出出入库记录
  exportSpareIoExcel: async (params: SpareIoPageReqVO) => {
    return await request.download({ url: '/iot/spare-io/export-excel', params })
  }
}
