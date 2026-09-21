import request from '@/config/axios'

export interface ShiftHandoverVO {
  id?: number
  handoverNo?: string
  scheduleId?: number
  handoverTime?: string
  shiftId?: number
  shiftName?: string
  teamId?: number
  teamName?: string
  handoverUserId?: number
  handoverUserName?: string
  takeoverUserId?: number
  takeoverUserName?: string
  dutyLog?: string
  pendingItems?: string
  dispatchInstructionId?: string | number
  dispatchInstructionNo?: string
  dispatchInstructionName?: string
  defectTicketFlag?: string
  status?: number
  remark?: string
  creator?: string
  createTime?: string
}

export interface ShiftHandoverPageReqVO {
  pageNo: number
  pageSize: number
  keyword?: string
  teamId?: number
  shiftId?: number
  handoverUserId?: number
  takeoverUserId?: number
  status?: number
  handoverDateRange?: string[]
}

export interface ShiftHandoverDefaultRespVO {
  scheduleId?: number
  shiftId?: number
  shiftName?: string
  teamId?: number
  teamName?: string
  handoverUserId?: number
  handoverUserName?: string
  takeoverUserId?: number
  takeoverUserName?: string
  handoverTime?: string
  nextShiftStartTime?: string
}

export interface ShiftHandoverReminderRespVO {
  needRemind?: boolean
  message?: string
  handoverDefault?: ShiftHandoverDefaultRespVO
}

export const ShiftHandoverApi = {
  getPage: async (params: ShiftHandoverPageReqVO) => {
    return await request.get({ url: '/iot/shift-handover/page', params })
  },

  get: async (id: string | number) => {
    return await request.get<ShiftHandoverVO>({ url: `/iot/shift-handover/get?id=${id}` })
  },

  create: async (data: ShiftHandoverVO) => {
    return await request.post({ url: '/iot/shift-handover/create', data })
  },

  update: async (data: ShiftHandoverVO) => {
    return await request.put({ url: '/iot/shift-handover/update', data })
  },

  remove: async (id: string | number) => {
    return await request.delete({ url: `/iot/shift-handover/delete?id=${id}` })
  },

  getDefaults: async () => {
    return await request.get<ShiftHandoverDefaultRespVO>({ url: '/iot/shift-handover/defaults' })
  },

  getReminder: async () => {
    return await request.get<ShiftHandoverReminderRespVO>({ url: '/iot/shift-handover/reminder' })
  },

  exportExcel: async (params: ShiftHandoverPageReqVO) => {
    return await request.download({ url: '/iot/shift-handover/export-excel', params })
  }
}

