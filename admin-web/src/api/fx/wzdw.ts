import request from '@/config/axios'

export interface FxWzDwListRespVO {
  id: string
  unitName: string
  address?: string
}

export interface FxWzDwSaveReqVO {
  id?: string
  unitName: string
  address?: string
  geometryGeoJson: string
  sort?: number
}

export const getFxWzDwList = () => {
  return request.get<FxWzDwListRespVO[]>({
    url: '/fx-wz-dw/list'
  })
}

export const getFxWzDwDetail = (id: string) => {
  return request.get<FxWzDwSaveReqVO>({
    url: `/fx-wz-dw/${id}`
  })
}

export const createFxWzDw = (data: FxWzDwSaveReqVO) => {
  return request.post<string>({
    url: '/fx-wz-dw',
    data
  })
}

export const updateFxWzDw = (data: FxWzDwSaveReqVO) => {
  return request.put({
    url: '/fx-wz-dw',
    data
  })
}

export const deleteFxWzDw = (id: string) => {
  return request.delete({
    url: `/fx-wz-dw/${id}`
  })
}
