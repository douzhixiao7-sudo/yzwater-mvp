import request from '@/config/axios'

export interface WarningBroadcastStationListReqVO {
  name?: string
  code?: string
  /** 行政区划ID（system_area.id） */
  adminDivision?: string
}

export interface WarningBroadcastStationListRespVO {
  id: string
  name: string
  longitude?: number
  latitude?: number
  /** 行政区划ID（system_area.id） */
  adminDivision?: string
  code: string
  quantity?: number
}

export interface WarningBroadcastStationSaveReqVO {
  id?: string
  name: string
  longitude?: number
  latitude?: number
  /** 行政区划ID（system_area.id） */
  adminDivision?: string
  code: string
  quantity?: number
  sort?: number
}

export const getWarningBroadcastStationList = (params?: WarningBroadcastStationListReqVO) => {
  return request.get<WarningBroadcastStationListRespVO[]>({
    url: '/warning-broadcast-station/list',
    params
  })
}

export const getWarningBroadcastStationDetail = (id: string) => {
  return request.get<WarningBroadcastStationSaveReqVO>({
    url: `/warning-broadcast-station/${id}`
  })
}

export const createWarningBroadcastStation = (data: WarningBroadcastStationSaveReqVO) => {
  return request.post<string>({
    url: '/warning-broadcast-station',
    data
  })
}

export const updateWarningBroadcastStation = (data: WarningBroadcastStationSaveReqVO) => {
  return request.put({
    url: '/warning-broadcast-station',
    data
  })
}

export const deleteWarningBroadcastStation = (id: string) => {
  return request.delete({
    url: `/warning-broadcast-station/${id}`
  })
}
