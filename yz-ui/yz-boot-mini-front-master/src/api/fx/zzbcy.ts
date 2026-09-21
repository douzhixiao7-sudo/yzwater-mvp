import request from '@/config/axios'

export interface FxZzbcyListRespVO {
  id: string
  position: string
  name: string
  title?: string
}

export interface FxZzbcySaveReqVO {
  id?: string
  position: string
  name: string
  title?: string
  sort?: number
}

export interface FxZzbcyListReqVO {
  name?: string
  title?: string
}

export interface FxZzbcyImportRespVO {
  totalCount?: number
  successCount?: number
  failureCount?: number
  errors?: string[]
}

export const getFxZzbcyList = (params?: FxZzbcyListReqVO) => {
  return request.get<FxZzbcyListRespVO[]>({
    url: '/fx-zzbcy/list',
    params
  })
}

export const getFxZzbcyDetail = (id: string) => {
  return request.get<FxZzbcySaveReqVO>({
    url: `/fx-zzbcy/${id}`
  })
}

export const createFxZzbcy = (data: FxZzbcySaveReqVO) => {
  return request.post<string>({
    url: '/fx-zzbcy',
    data
  })
}

export const updateFxZzbcy = (data: FxZzbcySaveReqVO) => {
  return request.put({
    url: '/fx-zzbcy',
    data
  })
}

export const deleteFxZzbcy = (id: string) => {
  return request.delete({
    url: `/fx-zzbcy/${id}`
  })
}

export const exportFxZzbcyExcel = () => {
  return request.download({
    url: '/fx-zzbcy/export-excel'
  })
}

export const getFxZzbcyImportTemplate = () => {
  return request.download({
    url: '/fx-zzbcy/get-import-template'
  })
}

export const importFxZzbcyExcel = (data: FormData) => {
  return request.post<FxZzbcyImportRespVO>({
    url: '/fx-zzbcy/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}
