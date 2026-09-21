import request from '@/config/axios'

export interface WaterPondPageReqVO {
  pageNo: number
  pageSize: number
  resourceName?: string
  resourceCode?: string
  villageCode?: string
  villageName?: string
  locationDesc?: string
  ownerUnit?: string
  ownershipType?: string
  resourceType?: string
  landType?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  remark?: string
  surveyor?: string
  areaMuMin?: number
  areaMuMax?: number
  areaSqmMin?: number
  areaSqmMax?: number
  occupyFarmAreaMin?: number
  occupyFarmAreaMax?: number
}

export interface WaterPondPageRespVO {
  id: string
  facilityId?: string
  ownershipType?: string
  occupyFarmArea?: number
  villageCode?: string
  remark?: string
  resourceType?: string
  eastTo?: string
  southTo?: string
  westTo?: string
  northTo?: string
  resourceName?: string
  locationDesc?: string
  villageName?: string
  areaSqm?: number
  resourceCode?: string
  ownerUnit?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  surveyor?: string
  surveyorPhone?: string
  centerLon?: number
  centerLat?: number
  areaMu?: number
  landType?: string
}

export interface WaterPondFilterOptionsRespVO {
  usageStatusList?: string[]
  resourceNatureList?: string[]
  ownershipTypeList?: string[]
  occupationStatusList?: string[]
  resourceTypeList?: string[]
}

export interface WaterPondSaveReqVO {
  id?: string
  facilityId?: string
  resourceCode?: string
  resourceName?: string
  locationDesc?: string
  villageName?: string
  villageCode?: string
  ownerUnit?: string
  ownerPerson?: string
  ownershipType?: string
  landType?: string
  areaSqm?: number
  areaMu?: number
  occupyFarmArea?: number
  eastTo?: string
  southTo?: string
  westTo?: string
  northTo?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  surveyor?: string
  surveyorPhone?: string
  remark?: string
  resourceType?: string
  centerLon?: number
  centerLat?: number
  geometryGeoJson?: string
}

export interface WaterPondImportRespVO {
  totalCount: number
  facilityCount: number
  pondCount: number
  insertedCount?: number
  updatedCount?: number
  geometryCount: number
  skippedCount: number
  message: string
}

export interface WaterPondStatsNameCountItem {
  code?: string
  name?: string
  count?: number
  areaSqm?: number
}

export interface WaterPondStatsRespVO {
  totalCount?: number
  totalAreaSqm?: number
  totalAreaMu?: number
  townCount?: number
  villageCount?: number
  townStats?: WaterPondStatsNameCountItem[]
  villageStats?: WaterPondStatsNameCountItem[]
  ownershipStats?: WaterPondStatsNameCountItem[]
  usageStatusStats?: WaterPondStatsNameCountItem[]
  resourceTypeStats?: WaterPondStatsNameCountItem[]
}

export const getWaterPondPage = (params: WaterPondPageReqVO) => {
  return request.get({ url: '/water-pond/page', params })
}

export const getWaterPondFilterOptions = () => {
  return request.get({ url: '/water-pond/filter-options' }) as Promise<WaterPondFilterOptionsRespVO>
}

export const getWaterPondStats = (params?: Omit<WaterPondPageReqVO, 'pageNo' | 'pageSize'> & Partial<Pick<WaterPondPageReqVO, 'pageNo' | 'pageSize'>>) => {
  return request.get({ url: '/water-pond/stats', params }) as Promise<WaterPondStatsRespVO>
}

export const getWaterPondDetail = (id: string) => {
  return request.get({ url: `/water-pond/${id}` })
}

export const createWaterPond = (data: WaterPondSaveReqVO) => {
  return request.post({ url: '/water-pond', data })
}

export const updateWaterPond = (data: WaterPondSaveReqVO) => {
  return request.put({ url: '/water-pond', data })
}

export const updateWaterPondGeometry = (id: string, geometryGeoJson: string) => {
  return request.put({
    url: `/water-pond/${id}/geometry`,
    data: { geometryGeoJson }
  })
}

export const deleteWaterPond = (id: string) => {
  return request.delete({ url: `/water-pond/${id}` })
}

export const importWaterPondGeoJson = (data: FormData) => {
  return request.post({
    url: '/water-pond/import',
    data,
    headersType: 'multipart/form-data',
    timeout: 600000
  })
}
