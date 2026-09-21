import request from '@/config/axios'

export interface RunLogVO {
  id?: number
  logNo?: string
  taskName?: string
  dutyTeamId?: number | string
  dutyTeamName?: string
  recorderUserId?: number
  recorderUserName?: string
  recordTime?: string | number
  runStartTime?: string | number
  runEndTime?: string | number
  checkPeriod?: string
  stationId?: string | number
  stationName?: string
  inspectionType?: string
  inspectionStandardId?: string | number
  inspectionStandardName?: string
  inspectionResultItems?: RunLogInspectionResultItemVO[]
  autoCreateFaultRecords?: boolean
  deviceName?: string
  deviceNames?: string[]
  runParamsText?: string
  eventDesc?: string
  dispatchInstructionId?: string | number
  dispatchInstructionNo?: string
  dispatchInstructionName?: string
  attachments?: string[]
  remark?: string
  createTime?: string | number
}

export interface RunLogInspectionResultRecordVO {
  attrName?: string
  attrUnit?: string
  standardValue?: string
  actualValue?: string
}

export interface RunLogInspectionResultItemVO {
  itemId?: number
  itemName?: string
  targetType?: string
  targetId?: string | number
  targetName?: string
  checkResult?: string
  checkRemark?: string
  records?: RunLogInspectionResultRecordVO[]
}

export interface RunLogPageReqVO {
  pageNo: number
  pageSize: number
  logNo?: string
  dutyTeamName?: string
  checkPeriod?: string
  stationId?: string | number
  deviceName?: string
  dispatchInstructionId?: string | number
  dispatchKeyword?: string
  runTime?: string[]
}

export interface RunLogDefaultVO {
  dutyTeamId?: number | string
  dutyTeamName?: string
  recorderUserId?: number
  recorderUserName?: string
  recordTime?: string | number
}

export interface RunLogDispatchOptionVO {
  id: number
  instructionNo?: string
  instructionName?: string
  title: string
}

export const RunLogApi = {
  getPage: async (params: RunLogPageReqVO) => {
    return await request.get({ url: '/iot/run-log/page', params })
  },

  get: async (id: string | number) => {
    return await request.get<RunLogVO>({ url: `/iot/run-log/get?id=${id}` })
  },

  create: async (data: RunLogVO) => {
    return await request.post({ url: '/iot/run-log/create', data })
  },

  update: async (data: RunLogVO) => {
    return await request.put({ url: '/iot/run-log/update', data })
  },

  remove: async (id: string | number) => {
    return await request.delete({ url: `/iot/run-log/delete?id=${id}` })
  },

  exportExcel: async (params: RunLogPageReqVO) => {
    return await request.download({ url: '/iot/run-log/export-excel', params })
  },

  getDefaults: async () => {
    return await request.get<RunLogDefaultVO>({ url: '/iot/run-log/defaults' })
  },

  getDispatchOptions: async (keyword?: string, stationId?: string | number) => {
    return await request.get<RunLogDispatchOptionVO[]>({
      url: '/iot/run-log/dispatch-options',
      params: {
        keyword: keyword || undefined,
        stationId: stationId || undefined
      }
    })
  }
}
