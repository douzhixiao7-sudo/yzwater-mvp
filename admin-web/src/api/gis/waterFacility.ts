import request from '@/config/axios'

export interface WaterFacilityPageReqVO {
  pageNo: number
  pageSize: number
  facilityName?: string
  facilityType?: string
  ecoType?: string
}

export interface WaterFacilityPageItemRespVO {
  id: string
  facilityCode: string
  facilityName: string
  facilityType: string
  adminRegion: string
  adminRegionCode: string
  manageUnit: string
  sourceType: string
  createTime: string
}

export interface WaterFacilityDetailRespVO extends WaterFacilityPageItemRespVO {
  attributes: Record<string, any>
  geometryGeoJson?: string
  geomType?: string
  srid?: number
  facilityTypeValue?: string
}

export interface DictDataItemRespVO {
  label: string
  value: string
}

export interface WaterFacilityUpdateReqVO {
  id: string
  facilityName: string
  facilityType: string
  adminRegion?: string
  adminRegionCode?: string
  manageUnit?: string
  sourceType?: string
  attributes?: Record<string, any>
}

export interface WaterFacilityGeometryUpdateReqVO {
  /** GeoJSON 几何字符串（仅 geometry，不包含 Feature） */
  geometryGeoJson?: string
  /** SRID（为空则默认 4490） */
  srid?: number
}

export interface WaterFacilityCustomizeCreateReqVO {
  facilityName: string
  adminRegion?: string
  adminRegionCode?: string
  manageUnit?: string
  attributes?: Record<string, any>
  /** GeoJSON 几何字符串（仅 geometry，不包含 Feature） */
  geometryGeoJson?: string
}

// 分页查询
export const getWaterFacilityPage = (params: WaterFacilityPageReqVO) => {
  return request.get<{ list: WaterFacilityPageItemRespVO[]; total: number }>({
    url: '/water/facility/page',
    params
  })
}

// 详情
export const getWaterFacilityDetail = (id: string) => {
  return request.get<WaterFacilityDetailRespVO>({
    url: `/water/facility/${id}`
  })
}

// 删除
export const deleteWaterFacility = (id: string) => {
  return request.delete({
    url: `/water/facility/${id}`
  })
}

// 修改
export const updateWaterFacility = (data: WaterFacilityUpdateReqVO) => {
  return request.put({
    url: '/water/facility',
    data
  })
}

// 更新设施几何（仅基础表 geom）
export const updateWaterFacilityGeometry = (id: string, data: WaterFacilityGeometryUpdateReqVO) => {
  return request.put({
    url: `/water/facility/${id}/geometry`,
    data
  })
}

// 新增自定义图层
export const createCustomizeWaterFacility = (data: WaterFacilityCustomizeCreateReqVO) => {
  return request.post<number>({
    url: '/water/facility/customize',
    data
  })
}

// 字典数据
export const getWaterFacilityDict = (dictType: string) => {
  return request.get<DictDataItemRespVO[]>({
    url: '/water/facility/dict',
    params: { dictType }
  })
}

// 字典数据（不做前端过滤，用于类型展示转换）
export const getWaterFacilityDictForLabel = (dictType: string) => {
  return request.get<DictDataItemRespVO[]>({
    url: '/water/facility/dict',
    params: { dictType }
  })
}
