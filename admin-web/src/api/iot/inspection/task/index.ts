import request from '@/config/axios'

export interface InspectionTaskTargetVO {
  id?: number
  targetSort?: number
  targetType?: number
  targetId: string | number
  deviceId?: string | number
  locationId?: string | number
  targetName?: string
  stationId?: string
}

export interface InspectionTaskVO {
  id?: number
  taskNo?: string
  taskName: string
  stationId: string
  inspectionType: string
  objectType: number
  standardId: string | number
  standardName?: string
  lineId?: string | number
  lineName?: string
  planId?: number
  sourceType?: number
  planStartTime: string
  planEndTime: string
  executorUserId: string | number
  executorName?: string
  taskStatus?: number
  workflowStatus?: number
  abnormalCount?: number
  submitTime?: string
  processInstanceId?: string
  processDefinitionKey?: string
  processStartTime?: string
  processEndTime?: string
  taskDesc?: string
  remark?: string
  items?: InspectionTaskSubmitResultItemReqVO[]
  targetCount?: number
  targetIds?: Array<string | number>
  targetNames?: string[]
  targets: InspectionTaskTargetVO[]
  createTime?: string | number
}

export interface InspectionTaskPageReqVO {
  pageNo: number
  pageSize: number
  taskNo?: string
  taskName?: string
  inspectionType?: string
  taskStatus?: number
  sourceType?: number
  workflowStatus?: number
  stationId?: string
  executorUserId?: number
  planStartTime?: string[]
  planEndTime?: string[]
}

export interface InspectionTaskSubmitResultReqVO {
  id: number
  abnormalCount?: number
  remark?: string
  autoCreateFaultRecords?: boolean
  items?: InspectionTaskSubmitResultItemReqVO[]
}

export interface InspectionTaskSubmitResultItemReqVO {
  itemId?: number
  itemName?: string
  targetId?: string | number
  targetName?: string
  checkResult?: string
  checkRemark?: string
  attachments?: string[]
  records?: InspectionTaskSubmitResultRecordReqVO[]
}

export interface InspectionTaskSubmitResultRecordReqVO {
  attrName?: string
  attrUnit?: string
  standardValue?: string
  actualValue?: string
}

export const InspectionTaskApi = {
  getInspectionTaskPage: async (params: InspectionTaskPageReqVO) => {
    return await request.get({ url: '/iot/inspection-task/page', params })
  },

  getInspectionTask: async (id: number) => {
    return await request.get<InspectionTaskVO>({ url: `/iot/inspection-task/get?id=${id}` })
  },

  createInspectionTask: async (data: InspectionTaskVO) => {
    return await request.post({ url: '/iot/inspection-task/create', data })
  },

  updateInspectionTask: async (data: InspectionTaskVO) => {
    return await request.put({ url: '/iot/inspection-task/update', data })
  },

  submitInspectionTaskResult: async (data: InspectionTaskSubmitResultReqVO) => {
    return await request.post({ url: '/iot/inspection-task/submit-result', data })
  },

  deleteInspectionTask: async (id: number) => {
    return await request.delete({ url: `/iot/inspection-task/delete?id=${id}` })
  }
}
