import request from '@/config/axios'

export interface DispatchManagePlanOptionVO {
  id: number
  planNo: string
  planName: string
  planType?: string
}

export interface DispatchManageReceiverUserVO {
  id: number
  nickname: string
  deptId: number
}

export interface DispatchManageVO {
  id?: number
  stationId?: string
  instructionNo?: string
  instructionName?: string
  issueOrgName?: string
  issueUserId?: number
  issueUserName?: string
  instructionContent: string
  planIds: number[]
  planNames?: string[]
  plannedFinishTime: string | number
  receiverDeptId: number | undefined
  receiverDeptName?: string
  receiverUserId: number | undefined
  receiverUserName?: string
  executorUserId?: number | undefined
  executorUserName?: string
  status?: number
  statusName?: string
  runLogCount?: number
  operationTicketUrl?: string
  feedbackContent?: string
  feedbackRemark?: string
  feedbackSubmitUserName?: string
  feedbackSubmitTime?: string
  remark?: string
  creator?: string
  createTime?: string
  updateTime?: string
}

export interface DispatchManagePageReqVO {
  pageNo: number
  pageSize: number
  stationId?: string
  instructionNo?: string
  issueOrgName?: string
  planName?: string
  status?: number
  plannedFinishTime?: string[]
}

export interface DispatchManageSubmitResultReqVO {
  id: number
  feedbackContent: string
  feedbackRemark?: string
}

export const DispatchManageApi = {
  getPage: async (params: DispatchManagePageReqVO) => {
    return await request.get({ url: '/iot/dispatch-manage/page', params })
  },

  get: async (id: number) => {
    return await request.get<DispatchManageVO>({ url: `/iot/dispatch-manage/get?id=${id}` })
  },

  create: async (data: DispatchManageVO) => {
    return await request.post({ url: '/iot/dispatch-manage/create', data })
  },

  update: async (data: DispatchManageVO) => {
    return await request.put({ url: '/iot/dispatch-manage/update', data })
  },

  remove: async (id: number) => {
    return await request.delete({ url: `/iot/dispatch-manage/delete?id=${id}` })
  },

  submitResult: async (data: DispatchManageSubmitResultReqVO) => {
    return await request.post({ url: '/iot/dispatch-manage/submit-result', data })
  },

  exportExcel: async (params: DispatchManagePageReqVO) => {
    return await request.download({ url: '/iot/dispatch-manage/export-excel', params })
  },

  getPlanOptions: async (planStatus?: number, stationId?: string) => {
    return await request.get<DispatchManagePlanOptionVO[]>({
      url: '/iot/dispatch-manage/plan-options',
      params: { planStatus, stationId }
    })
  },

  getReceiverUserList: async (deptId: number) => {
    return await request.get<DispatchManageReceiverUserVO[]>({
      url: `/iot/dispatch-manage/receiver-user-list?deptId=${deptId}`
    })
  },

  getExecutorUserList: async (deptId?: number) => {
    return await request.get<DispatchManageReceiverUserVO[]>({
      url: '/iot/dispatch-manage/executor-user-list',
      params: { deptId: deptId || undefined }
    })
  }
}
