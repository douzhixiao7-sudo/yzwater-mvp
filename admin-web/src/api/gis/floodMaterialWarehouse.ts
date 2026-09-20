import request from '@/config/axios'

export interface FloodMaterialWarehousePageReqVO {
  pageNo: number
  pageSize: number
  warehouseName?: string
  belongUnit?: string
  divisionCode?: string
}

export interface FloodMaterialWarehousePageRespVO {
  id: string | number
  warehouseName: string
  specificLocation?: string
  belongUnit?: string
  leaderName?: string
  leaderPhone?: string
  isDelegateStorage?: number
  divisionCode?: string[]
}

export interface FloodMaterialWarehouseMapPointRespVO {
  id: string | number
  warehouseName: string
  longitude?: number
  latitude?: number
}

export interface FloodMaterialWarehouseSaveReqVO {
  id?: string | number
  warehouseName: string
  specificLocation?: string
  longitude?: number
  latitude?: number
  belongUnit?: string
  leaderName?: string
  leaderPhone?: string
  materialType?: string
  isDelegateStorage?: number
  warehouseImages?: string[]
  remarks?: string
  divisionCode?: string[]
}

export const getFloodMaterialWarehousePage = (params: FloodMaterialWarehousePageReqVO) => {
  return request.get<{ list: FloodMaterialWarehousePageRespVO[]; total: number }>({
    url: '/flood-prevention-material/page',
    params
  })
}

export const getFloodMaterialWarehouseMapPoints = () => {
  return request.get<FloodMaterialWarehouseMapPointRespVO[]>({
    url: '/flood-prevention-material/warehouse-map-points'
  })
}

export const getFloodMaterialWarehouseDetail = (id: string | number) => {
  return request.get<FloodMaterialWarehouseSaveReqVO>({
    url: `/flood-prevention-material/${id}`
  })
}

export const createFloodMaterialWarehouse = (data: FloodMaterialWarehouseSaveReqVO) => {
  return request.post<string>({
    url: '/flood-prevention-material',
    data
  })
}

export const updateFloodMaterialWarehouse = (data: FloodMaterialWarehouseSaveReqVO) => {
  return request.put({
    url: '/flood-prevention-material',
    data
  })
}

export const deleteFloodMaterialWarehouse = (id: string | number) => {
  return request.delete({
    url: `/flood-prevention-material/${id}`
  })
}

export const exportFloodMaterialWarehouseExcel = (params: FloodMaterialWarehousePageReqVO) => {
  return request.download({
    url: '/flood-prevention-material/export-excel',
    params
  })
}
