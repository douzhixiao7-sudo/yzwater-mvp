import request from '@/config/axios'

/** 养护计划备件消耗 VO */
export interface MaintenancePlanSpareUsageVO {
  spareId?: number
  qty?: number
}

/** 养护计划 VO */
export interface MaintenancePlanVO {
  id?: number
  deviceId?: number | string
  deviceName?: string
  deviceType?: string
  stationId?: number | string
  planDate?: string
  maintainType?: string
  maintainItems?: string
  spareUsages?: MaintenancePlanSpareUsageVO[]
  maintainerName?: string
  finishTime?: number | string
  status?: string
  remark?: string
  sourceType?: string
  createTime?: string | number
  updateTime?: string | number
}

/** 养护计划分页 Request VO */
export interface MaintenancePlanPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number | string
  deviceName?: string
  deviceType?: string
  stationId?: number | string
  status?: string
  maintainType?: string
  planDate?: string[]
}

/** 养护计划提交 Request VO */
export interface MaintenancePlanSubmitReqVO {
  id: number
  maintainType: string
  maintainItems?: string
  spareUsages?: MaintenancePlanSpareUsageVO[]
  maintainerName?: string
  finishTime?: number | string
  remark: string
}

/** 养护计划创建 Request VO */
export interface MaintenancePlanCreateReqVO {
  deviceId: number | string
  planDate: string
  maintainType?: string
  maintainItems?: string
  remark?: string
}

// 养护计划 API
export const MaintenancePlanApi = {
  // 获取养护计划分页
  getMaintenancePlanPage: async (params: MaintenancePlanPageReqVO) => {
    return await request.get({ url: '/iot/maintenance-plan/page', params })
  },

  // 同步设备自动养护计划
  syncAutoMaintenancePlanByDevice: async (deviceId: number | string) => {
    return await request.post({ url: '/iot/maintenance-plan/sync-by-device', params: { deviceId } })
  },

  // 创建养护计划
  createMaintenancePlan: async (data: MaintenancePlanCreateReqVO) => {
    return await request.post({ url: '/iot/maintenance-plan/create', data })
  },

  // 获取养护计划详情
  getMaintenancePlan: async (id: number) => {
    return await request.get({ url: `/iot/maintenance-plan/get?id=${id}` })
  },

  // 提交养护信息
  submitMaintenancePlan: async (data: MaintenancePlanSubmitReqVO) => {
    return await request.post({ url: '/iot/maintenance-plan/submit', data })
  }
}
