import request from '@/config/axios'

export interface InspectionPlanTargetVO {
  id?: number
  targetSort?: number
  targetType?: number
  targetId: string | number
  deviceId?: string | number
  locationId?: string | number
  targetName?: string
  stationId?: string
}

export interface InspectionPlanVO {
  id?: number
  planName: string
  inspectionType: string
  objectType: number
  standardId?: number
  standardName?: string
  lineId?: number
  lineName?: string
  planStartDate: string | number[]
  planEndDate: string | number[]
  cycleUnit: string
  cycleValue?: number
  planStatus?: number
  executorUserId: string | number
  executorName?: string
  executeDeptId?: number
  executeDeptName?: string
  stationId?: string
  targetCount?: number
  targetIds?: Array<string | number>
  targetNames?: string[]
  targets: InspectionPlanTargetVO[]
  remark?: string
  createTime?: string | number
}

export interface InspectionPlanPageReqVO {
  pageNo: number
  pageSize: number
  planName?: string
  inspectionType?: string
  objectType?: number
  cycleUnit?: string
  planStatus?: number
  cycleMonth?: string
}

export interface InspectionPlanTargetOptionVO {
  id: string | number
  name: string
  objectType: number
  stationId?: string
}

export interface InspectionPlanStandardOptionVO {
  id: number
  name: string
  inspectionType: string
  suggestCycleUnit?: string
}

export interface InspectionPlanLineOptionVO {
  id: number
  name: string
  stationId?: string
  inspectionType?: string
}

export const InspectionPlanApi = {
  getInspectionPlanPage: async (params: InspectionPlanPageReqVO) => {
    return await request.get({ url: '/iot/inspection-plan/page', params })
  },

  getInspectionPlan: async (id: number) => {
    return await request.get<InspectionPlanVO>({ url: `/iot/inspection-plan/get?id=${id}` })
  },

  createInspectionPlan: async (data: InspectionPlanVO) => {
    return await request.post({ url: '/iot/inspection-plan/create', data })
  },

  updateInspectionPlan: async (data: InspectionPlanVO) => {
    return await request.put({ url: '/iot/inspection-plan/update', data })
  },

  deleteInspectionPlan: async (id: number) => {
    return await request.delete({ url: `/iot/inspection-plan/delete?id=${id}` })
  },

  getTargetOptions: async (params: {
    objectType: number
    stationId?: string
    keyword?: string
    limit?: number
  }) => {
    return await request.get<InspectionPlanTargetOptionVO[]>({
      url: '/iot/inspection-plan/target-options',
      params
    })
  },

  getStandardOptions: async (params: {
    stationId?: string
    inspectionType?: string
    keyword?: string
    limit?: number
  }) => {
    return await request.get<InspectionPlanStandardOptionVO[]>({
      url: '/iot/inspection-plan/standard-options',
      params
    })
  },

  getLineOptions: async (params: {
    stationId?: string
    inspectionType?: string
    keyword?: string
    limit?: number
  }) => {
    return await request.get<InspectionPlanLineOptionVO[]>({
      url: '/iot/inspection-plan/line-options',
      params
    })
  }
}
