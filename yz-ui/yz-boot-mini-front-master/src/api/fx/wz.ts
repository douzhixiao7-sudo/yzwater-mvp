import request from '@/config/axios'

export interface FxWzWarehouseOptionRespVO {
  id: string
  warehouseName: string
}

export interface FxWzListRespVO {
  id: string
  materialName: string
  quantity: number
  unit: string
  materialType?: string
  storageUnit?: string
  isDelegateStorage?: number
  warehouseId?: string
  remark?: string
  warehouseAddress?: string
  longitude?: number
  latitude?: number
  sort?: number
  contactPerson?: string
  contactInfo?: string
}

export interface FxWzSaveReqVO {
  id?: string
  materialName: string
  quantity: number | null
  unit: string
  materialType?: string
  isDelegateStorage: number
  warehouseId: string
  remark?: string
  warehouseAddress?: string
  longitude?: number
  latitude?: number
  sort?: number
  contactPerson?: string
  contactInfo?: string
}

export interface FxWzMaterialMapPointRespVO {
  id: string
  materialName: string
  storageUnit?: string
  warehouseAddress?: string
  longitude?: number
  latitude?: number
}

export interface FxWzListReqVO {
  materialName?: string
  unit?: string
}

export const getFxWzList = (params?: FxWzListReqVO) => {
  return request.get<FxWzListRespVO[]>({
    url: '/fx-wz/list',
    params
  })
}

export const getFxWzDetail = (id: string) => {
  return request.get<FxWzSaveReqVO>({
    url: `/fx-wz/${id}`
  })
}

export const getFxWzWarehouseOptions = () => {
  return request.get<FxWzWarehouseOptionRespVO[]>({
    url: '/fx-wz/warehouse-options'
  })
}

export const getFxWzMaterialMapPoints = () => {
  return request.get<FxWzMaterialMapPointRespVO[]>({
    url: '/fx-wz/material-map-points'
  })
}

export const createFxWz = (data: FxWzSaveReqVO) => {
  return request.post<string>({
    url: '/fx-wz',
    data
  })
}

export const updateFxWz = (data: FxWzSaveReqVO) => {
  return request.put({
    url: '/fx-wz',
    data
  })
}

export const deleteFxWz = (id: string) => {
  return request.delete({
    url: `/fx-wz/${id}`
  })
}
