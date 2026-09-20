import request from '@/config/axios'

export interface DispatchReceiveVO {
  id: string | number
  instructionNo?: string
  instructionName?: string
  issueOrgName?: string
  issueUserName?: string
  stationId?: string
  instructionContent?: string
  planNames?: string[]
  issueTime?: string | number
  plannedFinishTime?: string | number
  receiverDeptName?: string
  receiverUserId?: string | number
  receiverUserName?: string
  executorUserId?: string | number
  executorUserName?: string
  receiveStatus?: number
  receiveTime?: string | number
  executionStatus?: number
  executionStatusName?: string
  executeFlag?: number
  finishTime?: string | number
  attachments?: string[]
  remark?: string
  submitUserName?: string
  runLogIds?: Array<string | number>
  createTime?: string | number
}

export interface DispatchReceivePageReqVO {
  pageNo: number
  pageSize: number
  instructionNo?: string
  instructionName?: string
  issueOrgName?: string
  planName?: string
  stationId?: string
  executionStatus?: number
  issueTime?: string[]
}

export interface DispatchReceiveSubmitReqVO {
  id: string | number
  executeFlag: number
  finishTime: string
  attachments: string[]
  remark?: string
  runLogIds?: Array<string | number>
}

export interface DispatchReceiveRunLogOptionVO {
  id: string | number
  title: string
}

export const DispatchReceiveApi = {
  getPage: async (params: DispatchReceivePageReqVO) => {
    return await request.get({ url: '/iot/dispatch-receive/page', params })
  },

  get: async (id: string | number) => {
    return await request.get<DispatchReceiveVO>({ url: `/iot/dispatch-receive/get?id=${id}` })
  },

  accept: async (id: string | number) => {
    return await request.post({ url: `/iot/dispatch-receive/accept?id=${id}` })
  },

  submitResult: async (data: DispatchReceiveSubmitReqVO) => {
    return await request.post({ url: '/iot/dispatch-receive/submit-result', data })
  },

  getRunLogOptions: async (stationId?: string) => {
    return await request.get<DispatchReceiveRunLogOptionVO[]>({
      url: '/iot/dispatch-receive/run-log-options',
      params: { stationId }
    })
  },

  exportExcel: async (params: DispatchReceivePageReqVO) => {
    return await request.download({ url: '/iot/dispatch-receive/export-excel', params })
  }
}
