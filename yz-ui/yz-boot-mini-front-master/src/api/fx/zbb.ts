import request from '@/config/axios'

export interface FxZbbItemRespVO {
  id: string
  weekDay: number
  dutyChiefName?: string
  dutyChiefMobile?: string
  sectionChiefName?: string
  sectionChiefMobile?: string
  dutyStaffName?: string
  dutyStaffMobile?: string
}

export interface FxZbbListRespVO {
  id: string
  startDate: string
  endDate: string
  description?: string
  items: FxZbbItemRespVO[]
}

export interface FxZbbSaveItemReqVO {
  id?: string
  weekDay: number
  dutyChiefName?: string
  dutyChiefMobile?: string
  sectionChiefName?: string
  sectionChiefMobile?: string
  dutyStaffName?: string
  dutyStaffMobile?: string
}

export interface FxZbbSaveReqVO {
  id?: string
  startDate: string
  endDate: string
  description?: string
  items: FxZbbSaveItemReqVO[]
}

export const getFxZbbList = () => {
  return request.get<FxZbbListRespVO[]>({
    url: '/fx-zbb/list'
  })
}

export const getFxZbbDetail = (id: string) => {
  return request.get<FxZbbSaveReqVO>({
    url: `/fx-zbb/${id}`
  })
}

export const createFxZbb = (data: FxZbbSaveReqVO) => {
  return request.post<string>({
    url: '/fx-zbb',
    data
  })
}

export const updateFxZbb = (data: FxZbbSaveReqVO) => {
  return request.put({
    url: '/fx-zbb',
    data
  })
}

export const deleteFxZbb = (id: string) => {
  return request.delete({
    url: `/fx-zbb/${id}`
  })
}

export const exportFxZbbExcel = (id?: string) => {
  return request.download({
    url: '/fx-zbb/export-excel',
    params: id ? { id } : undefined
  })
}
