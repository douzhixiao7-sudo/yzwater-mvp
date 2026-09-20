import request from '@/config/axios'

export interface FxQxdwListRespVO {
  id: string
  unitName: string
  teamName?: string
  planCount?: number
  contactName?: string
  contactPhone?: string
  remark?: string
}

export interface FxQxdwSaveReqVO {
  id?: string
  unitName: string
  teamName?: string
  planCount?: number
  contactName?: string
  contactPhone?: string
  remark?: string
  sort?: number
}

export const getFxQxdwList = () => {
  return request.get<FxQxdwListRespVO[]>({
    url: '/fx-qxdw/list'
  })
}

export const getFxQxdwDetail = (id: string) => {
  return request.get<FxQxdwSaveReqVO>({
    url: `/fx-qxdw/${id}`
  })
}

export const createFxQxdw = (data: FxQxdwSaveReqVO) => {
  return request.post<string>({
    url: '/fx-qxdw',
    data
  })
}

export const updateFxQxdw = (data: FxQxdwSaveReqVO) => {
  return request.put({
    url: '/fx-qxdw',
    data
  })
}

export const deleteFxQxdw = (id: string) => {
  return request.delete({
    url: `/fx-qxdw/${id}`
  })
}

export const exportFxQxdwExcel = () => {
  return request.download({
    url: '/fx-qxdw/export-excel'
  })
}
