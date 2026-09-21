import request from '@/config/axios'

export interface DispatchPlanParamVO {
  id?: number
  paramSort?: number
  paramName: string
  paramValue: string
  paramUnit?: string
  remark?: string
}

export interface DispatchPlanObjectVO {
  id?: number
  objectSort?: number
  objectType: number
  objectName?: string
  deviceId?: number | string
  locationId?: number | string
  remark?: string
  params: DispatchPlanParamVO[]
}

export interface DispatchPlanVO {
  id?: number
  planNo?: string
  planName: string
  planType: string
  stationId?: string
  planTypeName?: string
  prepareUserId?: number
  prepareUserName: string
  prepareOrgName: string
  prepareTime?: string
  planStatus?: number
  planStatusName?: string
  coreTarget: string
  projectName: string
  expectedEffect?: string
  prepareDesc?: string
  attachments?: string[]
  objectCount?: number
  usedCount?: number
  objectNames?: string[]
  objects: DispatchPlanObjectVO[]
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface DispatchPlanPageReqVO {
  pageNo: number
  pageSize: number
  planNo?: string
  planName?: string
  planType?: string
  stationId?: string
  prepareUserName?: string
  planStatus?: number
  prepareTime?: string[]
}

export const DispatchPlanApi = {
  getDispatchPlanPage: async (params: DispatchPlanPageReqVO) => {
    return await request.get({ url: '/iot/dispatch-plan/page', params })
  },

  getDispatchPlan: async (id: number) => {
    return await request.get<DispatchPlanVO>({ url: `/iot/dispatch-plan/get?id=${id}` })
  },

  createDispatchPlan: async (data: DispatchPlanVO) => {
    return await request.post({ url: '/iot/dispatch-plan/create', data })
  },

  updateDispatchPlan: async (data: DispatchPlanVO) => {
    return await request.put({ url: '/iot/dispatch-plan/update', data })
  },

  deleteDispatchPlan: async (id: number) => {
    return await request.delete({ url: `/iot/dispatch-plan/delete?id=${id}` })
  },

  archiveDispatchPlan: async (id: number) => {
    return await request.put({ url: `/iot/dispatch-plan/archive?id=${id}` })
  },

  exportDispatchPlanExcel: async (params: DispatchPlanPageReqVO) => {
    return await request.download({ url: '/iot/dispatch-plan/export-excel', params })
  }
}
