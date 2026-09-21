import request from '@/config/axios'

export interface GisBufferQueryCreateReqVO {
  centerLongitude: number
  centerLatitude: number
  radiusMeters: number
  facilityTypes?: string[]
  selectAll?: boolean
}

export interface GisBufferQueryAreaRespVO {
  id?: number
  name?: string
}

export interface GisBufferQueryFacilityRespVO {
  facilityId: string | number
  facilityType: string
  facilityName: string
  adminRegionCode?: string
  geomType?: string
  longitude?: number
  latitude?: number
}

export interface GisBufferQueryRespVO {
  id: string | number
  centerLongitude?: number
  centerLatitude?: number
  radiusMeters: number
  bufferAreaM2?: number
  bufferGeoJson?: string
  facilityTypes?: string[]
  facilityCount?: number
  facilityTypeCount?: Record<string, number>
  adminAreas?: GisBufferQueryAreaRespVO[]
  statsTime?: string
  facilities?: GisBufferQueryFacilityRespVO[]
}

export interface GisBufferQueryPageReqVO {
  pageNo: number
  pageSize: number
}

export interface GisBufferQueryPageItemRespVO {
  id: string | number
  radiusMeters: number
  bufferAreaM2?: number
  facilityCount?: number
  statsTime?: string
  adminAreaNames?: string[]
  facilityTypes?: string[]
}

export const createBufferQuery = (data: GisBufferQueryCreateReqVO) => {
  return request.post<GisBufferQueryRespVO>({
    url: '/gis/buffer-query',
    data
  })
}

export const getBufferQueryPage = (params: GisBufferQueryPageReqVO) => {
  return request.get<{ list: GisBufferQueryPageItemRespVO[]; total: number }>({
    url: '/gis/buffer-query/page',
    params
  })
}

export const getBufferQueryDetail = (id: string | number) => {
  return request.get<GisBufferQueryRespVO>({
    url: `/gis/buffer-query/${id}`
  })
}
