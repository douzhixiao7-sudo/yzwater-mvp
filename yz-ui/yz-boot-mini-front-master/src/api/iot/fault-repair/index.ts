import request from '@/config/axios'

/** ???????? VO */
export interface FaultRepairSpareUsageVO {
  spareId?: number
  qty?: number
}

/** ?????? VO */
export interface FaultRepairVO {
  id?: number
  orderNo?: string
  deviceId?: number | string
  deviceName: string
  deviceType: string
  faultType?: string
  faultTime?: number | string
  faultSymptom: string
  reporterUserId?: number
  reporterName?: string
  repairUserId?: number
  repairName?: string
  status?: string
  finishTime?: number | string
  planFinishTime?: number | string
  faultImages: string[]
  remark?: string
  spareUsages?: FaultRepairSpareUsageVO[]
  createTime?: number | string
}

/** ?????????? */
export interface FaultRepairPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number | string
  repairUserId?: number
  deviceName?: string
  deviceType?: string
  reporterName?: string
  repairName?: string
  status?: string
  faultTime?: string[]
  createTime?: string[]
}

/** ?????? */
export interface FaultRepairAuditAssignReqVO {
  id: number
  repairUserId?: number
  repairName: string
  planFinishTime?: number | string
  remark?: string
}

/** ???????? */
export interface FaultRepairResultReqVO {
  id: number
  finishTime: number | string
  status: string
  spareUsages?: FaultRepairSpareUsageVO[]
  remark?: string
  repairName?: string
}

// ?????? API
export const FaultRepairApi = {
  // ??????????
  getFaultRepairPage: async (params: FaultRepairPageReqVO) => {
    return await request.get({ url: '/iot/fault-repair/page', params })
  },

  // ??????????
  getFaultRepair: async (id: number) => {
    return await request.get({ url: `/iot/fault-repair/get?id=${id}` })
  },

  // ????????
  createFaultRepair: async (data: FaultRepairVO) => {
    return await request.post({ url: '/iot/fault-repair/create', data })
  },

  // ????????
  updateFaultRepair: async (data: FaultRepairVO) => {
    return await request.put({ url: '/iot/fault-repair/update', data })
  },

  // ????????
  deleteFaultRepair: async (id: number) => {
    return await request.delete({ url: `/iot/fault-repair/delete?id=${id}` })
  },

  // ????????
  exportFaultRepairExcel: async (params: FaultRepairPageReqVO) => {
    return await request.download({ url: '/iot/fault-repair/export-excel', params })
  },

  // ????
  auditAssignFaultRepair: async (data: FaultRepairAuditAssignReqVO) => {
    return await request.post({ url: '/iot/fault-repair/audit-assign', data })
  },

  // ??????
  submitFaultRepairResult: async (data: FaultRepairResultReqVO) => {
    return await request.post({ url: '/iot/fault-repair/submit-result', data })
  }
}
