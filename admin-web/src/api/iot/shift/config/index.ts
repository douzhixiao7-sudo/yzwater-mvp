import request from '@/config/axios'

export interface ShiftConfigVO {
  id?: number
  shiftNo?: string
  shiftName?: string
  stationId?: string
  startTime?: string
  endTime?: string
  crossDay?: boolean
  remark?: string
  creator?: string
  createTime?: string | number
}

export interface ShiftConfigPageReqVO {
  pageNo: number
  pageSize: number
  stationId?: string
  shiftName?: string
  timeRange?: string[]
}

export const ShiftConfigApi = {
  getPage: async (params: ShiftConfigPageReqVO) => {
    return await request.get({ url: '/iot/shift-config/page', params })
  },

  get: async (id: string | number) => {
    return await request.get<ShiftConfigVO>({ url: `/iot/shift-config/get?id=${id}` })
  },

  create: async (data: ShiftConfigVO) => {
    return await request.post({ url: '/iot/shift-config/create', data })
  },

  update: async (data: ShiftConfigVO) => {
    return await request.put({ url: '/iot/shift-config/update', data })
  },

  remove: async (id: string | number) => {
    return await request.delete({ url: `/iot/shift-config/delete?id=${id}` })
  },

  exportExcel: async (params: ShiftConfigPageReqVO) => {
    return await request.download({ url: '/iot/shift-config/export-excel', params })
  }
}
