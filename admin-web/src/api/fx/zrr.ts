import request from '@/config/axios'

export interface FxZrrListRespVO {
  id: string
  type: string
  divisionCode?: string
  administrativeName?: string
  administrativeTitle?: string
  technicalName?: string
  technicalTitle?: string
}

export interface FxZrrSaveReqVO {
  id?: string
  type: string
  divisionCode?: string
  administrativeName?: string
  administrativeTitle?: string
  technicalName?: string
  technicalTitle?: string
  sort?: number
}

export interface FxZrrBatchSaveReqVO {
  type: string
  items: Array<{
    id?: string
    divisionCode?: string
    administrativeName?: string
    administrativeTitle?: string
    technicalName?: string
    technicalTitle?: string
    sort?: number
  }>
}

export const getFxZrrList = (type: string) => {
  return request.get<FxZrrListRespVO[]>({
    url: '/fx-zrr/list',
    params: { type }
  })
}

export const getFxZrrDetail = (id: string) => {
  return request.get<FxZrrSaveReqVO>({
    url: `/fx-zrr/${id}`
  })
}

export const createFxZrr = (data: FxZrrSaveReqVO) => {
  return request.post<string>({
    url: '/fx-zrr',
    data
  })
}

export const updateFxZrr = (data: FxZrrSaveReqVO) => {
  return request.put({
    url: '/fx-zrr',
    data
  })
}

export const batchSaveFxZrr = (data: FxZrrBatchSaveReqVO) => {
  return request.post({
    url: '/fx-zrr/batch-save',
    data
  })
}

export const deleteFxZrr = (id: string) => {
  return request.delete({
    url: `/fx-zrr/${id}`
  })
}

export const exportFxZrrExcel = (type: string) => {
  return request.download({
    url: '/fx-zrr/export-excel',
    params: { type }
  })
}
