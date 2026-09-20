import request from '@/config/axios'

export interface ShiftScheduleVO {
  id?: number
  scheduleNo?: string
  scheduleDate?: string
  stationId?: string
  shiftId?: number
  shiftName?: string
  teamId?: number
  teamName?: string
  dutyUserId?: number
  dutyUserName?: string
  dutyLeaderName?: string
  dutyMobile?: string
  dutyPostName?: string
  dutyStartTime?: string | number
  dutyEndTime?: string | number
  dutyLog?: string
  status?: number
  remark?: string
  creator?: string
  createTime?: string | number
}

export interface ShiftSchedulePageReqVO {
  pageNo: number
  pageSize: number
  keyword?: string
  teamId?: number
  shiftId?: number
  dutyUserId?: number
  dutyUserName?: string
  status?: number
  scheduleDateRange?: string[]
}

export interface ShiftScheduleCalendarReqVO {
  month?: string
  keyword?: string
  teamId?: number
  shiftId?: number
  dutyUserId?: number
}

export interface ShiftScheduleShiftOptionVO {
  id: number
  shiftNo?: string
  shiftName?: string
  startTime?: string
  endTime?: string
  crossDay?: boolean
}

export interface ShiftScheduleTeamOptionVO {
  id: number
  teamNo?: string
  teamName?: string
}

export interface ShiftScheduleImportRespVO {
  successCount: number
  failureCount: number
  failureMessages: string[]
}

export const ShiftScheduleApi = {
  getPage: async (params: ShiftSchedulePageReqVO) => {
    return await request.get({ url: '/iot/shift-schedule/page', params })
  },

  getCalendar: async (params: ShiftScheduleCalendarReqVO) => {
    return await request.get<ShiftScheduleVO[]>({ url: '/iot/shift-schedule/calendar', params })
  },

  get: async (id: string | number) => {
    return await request.get<ShiftScheduleVO>({ url: `/iot/shift-schedule/get?id=${id}` })
  },

  create: async (data: ShiftScheduleVO) => {
    return await request.post({ url: '/iot/shift-schedule/create', data })
  },

  update: async (data: ShiftScheduleVO) => {
    return await request.put({ url: '/iot/shift-schedule/update', data })
  },

  remove: async (id: string | number) => {
    return await request.delete({ url: `/iot/shift-schedule/delete?id=${id}` })
  },

  exportExcel: async (params: ShiftSchedulePageReqVO) => {
    return await request.download({ url: '/iot/shift-schedule/export-excel', params })
  },

  getShiftOptions: async (stationId?: string) => {
    return await request.get<ShiftScheduleShiftOptionVO[]>({
      url: '/iot/shift-schedule/shift-options',
      params: { stationId }
    })
  },

  getTeamOptions: async (stationId?: string) => {
    return await request.get<ShiftScheduleTeamOptionVO[]>({
      url: '/iot/shift-schedule/team-options',
      params: { stationId }
    })
  },

  importExcel: async (data: FormData) => {
    return await request.post<ShiftScheduleImportRespVO>({
      url: '/iot/shift-schedule/import-excel',
      data,
      headersType: 'multipart/form-data'
    })
  },

  getImportTemplate: async () => {
    return await request.download({ url: '/iot/shift-schedule/get-import-template' })
  }
}
