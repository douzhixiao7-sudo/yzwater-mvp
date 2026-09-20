import request from '@/config/axios'

export interface InspectionLinePointVO {
  id?: number
  pointSort?: number
  pointName?: string
  pointType: number
  deviceId?: number
  locationId?: number
  longitude?: number
  latitude?: number
  remark?: string
}

export interface InspectionLineVO {
  id?: number
  stationId: string
  lineName: string
  inspectionType: string
  areaType: string
  lineDesc?: string
  startLongitude: number
  startLatitude: number
  endLongitude: number
  endLatitude: number
  totalLengthMeter?: number
  pointCount?: number
  useCount?: number
  status: number
  remark?: string
  createTime?: string | number
  points: InspectionLinePointVO[]
}

export interface InspectionLinePointOptionVO {
  deviceId: number
  pointName: string
  deviceName?: string
  stationId?: string
  longitude?: number
  latitude?: number
  address?: string
}

export interface InspectionLinePageReqVO {
  pageNo: number
  pageSize: number
  stationId?: string
  lineName?: string
  inspectionType?: string
  areaType?: string
  minPointCount?: number
  maxPointCount?: number
  minUseCount?: number
  maxUseCount?: number
  status?: number
  createTime?: string[]
}

export const InspectionLineApi = {
  getInspectionLinePage: async (params: InspectionLinePageReqVO) => {
    return await request.get({ url: '/iot/inspection-line/page', params })
  },

  getInspectionLine: async (id: string | number) => {
    return await request.get<InspectionLineVO>({ url: '/iot/inspection-line/get', params: { id } })
  },

  createInspectionLine: async (data: InspectionLineVO) => {
    return await request.post({ url: '/iot/inspection-line/create', data })
  },

  updateInspectionLine: async (data: InspectionLineVO) => {
    return await request.put({ url: '/iot/inspection-line/update', data })
  },

  deleteInspectionLine: async (id: number) => {
    return await request.delete({ url: `/iot/inspection-line/delete?id=${id}` })
  },

  getPointOptions: async (params: { stationId: string; keyword?: string; limit?: number }) => {
    return await request.get<InspectionLinePointOptionVO[]>({
      url: '/iot/inspection-line/point-options',
      params
    })
  }
}
