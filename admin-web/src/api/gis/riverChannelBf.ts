import request from '@/config/axios'

export interface RiverChannelPageReqVO {
  pageNo: number
  pageSize: number
  riverCode?: string
  riverName?: string
  riverLevel?: string[]
  ecologyType?: string
  isProvincialBackbone?: number
}

export interface RiverChannelPageRespVO {
  id: string
  riverCode: string
  riverName: string
  isProvincialBackbone?: number
  riverLevelLabel?: string
  riverTypeLabel?: string
  basinTypeLabel?: string
  startEndLocation?: string
  lengthKm?: number
  catchmentKm2?: number
  managementUnit?: string
}

export interface RiverChannelSimpleRespVO {
  id: string
  riverCode: string
  riverName: string
  updateTime?: string
}

export interface RiverChannelDetailRespVO {
  id?: string
  facilityId?: string
  geometryGeoJson?: string
  geomType?: string
  srid?: number
  riverCode?: string
  riverName?: string
  lengthKm?: number
  catchmentKm2?: number
  averageSlope?: number
  basinType?: string
  ecologyType?: string
  transboundaryType?: string
  floodStandard?: string
  embankmentLevel?: string
  embankmentLength?: number
  centroidLongitude?: number
  centroidLatitude?: number
  riverEndLongitude?: number
  riverEndLatitude?: number
  riverSourceLongitude?: number
  riverSourceLatitude?: number
  flowAreas?: string[]
  historicalMaxWaterLevel?: number
  maxWaterLevelDate?: string
  lowestWaterLevelDate?: string
  historicalMinWaterLevel?: number
  averageAnnualRunoff?: number
  sourceMountainRange?: string
  riverTerminus?: string
  riverLevel?: string
  isProvincialBackbone?: number
  riverEntrance?: string
  riverOrigin?: string
  startPoint?: string
  endPoint?: string
  riverType?: string[]
  riverPhotos?: string[]
  waterQualityStatus?: string
  associatedFacilities?: string
  riverSectionCount?: number
  town?: string[]
  managementUnit?: string
  responsibilities?: string
  remarks?: string
  sections?: RiverSectionSaveReqVO[] | RiverSectionDetailRespVO[]
}

export interface DictDataItemRespVO {
  label: string
  value: string
  status?: number
}

export interface RiverSectionSaveReqVO {
  id?: string
  facilityId?: string
  sectionName?: string
  startPoint?: string
  endPoint?: string
  startLongitude?: number
  startLatitude?: number
  endLongitude?: number
  endLatitude?: number
  remarks?: string
}

export interface RiverSectionDetailRespVO {
  id?: string
  riverChannelId?: string
  facilityId?: string
  sectionName?: string
  startPoint?: string
  endPoint?: string
  startLongitude?: number
  startLatitude?: number
  endLongitude?: number
  endLatitude?: number
  remarks?: string
}

export interface RiverSectionSimpleRespVO {
  id: string
  sectionName: string
}

export interface RiverSectionWithChannelSimpleRespVO {
  id: string
  riverChannelId?: string
  riverName?: string
  sectionName?: string
  updateTime?: string
}

export interface RiverChannelSupervisionDetailVO {
  id?: string
  riverSectionId?: string
  supervisionUnit?: string
  supervisionContact?: string
}

export interface RiverHeadSupervisionItemReqVO {
  supervisionUnit?: string
  supervisionContact?: string
}

export interface RiverHeadItemReqVO {
  id?: string
  riverSectionId?: string
  sectionName?: string
  headLevel?: string
  headPosition?: string
  headName?: string
  responsibilities?: string
  /** 行政区划（可多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
}

export interface RiverHeadSectionItemReqVO {
  sectionId?: string
  /** 关联对象ID（河道为 riverChannelId；河段为 sectionId） */
  referenceId?: string
  /** 关联对象类型（river：河道；river_section：河段） */
  referenceType?: string
  sectionName?: string
  heads: RiverHeadItemReqVO[]
  supervisions?: RiverHeadSupervisionItemReqVO[]
}

export interface RiverHeadBatchSaveReqVO {
  riverChannelId: string
  sections: RiverHeadSectionItemReqVO[]
}

export interface RiverManagementSectionDetailVO {
  sectionId?: string
  sectionName?: string
  heads: RiverHeadItemReqVO[]
  supervisions: RiverChannelSupervisionDetailVO[]
}

export interface RiverChiefOverviewChiefVO {
  id?: string
  headName?: string
  headLevel?: string
  headLevelLabel?: string
  headPosition?: string
  headContact?: string
  effectiveFrom?: string
}

export interface RiverChiefOverviewSectionVO {
  sectionId?: string
  sectionName?: string
  chiefs: RiverChiefOverviewChiefVO[]
}

export interface RiverChiefOverviewRespVO {
  riverId?: string
  riverName?: string
  riverChiefs: RiverChiefOverviewChiefVO[]
  sectionChiefGroups: RiverChiefOverviewSectionVO[]
  totalCount?: number
}

export const getRiverChannelPage = (params: RiverChannelPageReqVO) => {
  return request.get<{ list: RiverChannelPageRespVO[]; total: number }>({
    url: '/river/channel-bf/page',
    params
  })
}

export const getRiverChannelSimpleList = () => {
  return request.get<RiverChannelSimpleRespVO[]>({
    url: '/river/channel-bf/simple-list'
  })
}

export const getRiverChannelDetail = (id: string | number) => {
  return request.get<RiverChannelDetailRespVO>({
    url: `/river/channel-bf/${id}`
  })
}

export const getRiverChannelDetailByFacility = (facilityId: string | number) => {
  return request.get<RiverChannelDetailRespVO>({
    url: `/river/channel-bf/facility/${facilityId}`
  })
}

export const getRiverSectionDetailByFacility = (facilityId: string | number) => {
  return request.get<RiverSectionDetailRespVO>({
    url: `/river/channel-bf/section/facility/${facilityId}`
  })
}

export const createRiverChannel = (data: RiverChannelDetailRespVO) => {
  return request.post<string>({
    url: '/river/channel-bf',
    data
  })
}

export const updateRiverChannel = (data: RiverChannelDetailRespVO) => {
  return request.put({
    url: '/river/channel-bf',
    data
  })
}

export const updateRiverChannelGeometry = (
  id: string | number,
  data: { geometryGeoJson?: string; srid?: number }
) => {
  return request.put<string>({
    url: `/river/channel-bf/${id}/geometry`,
    data
  })
}

export const deleteRiverChannel = (id: string | number) => {
  return request.delete({
    url: `/river/channel-bf/${id}`
  })
}

export const getRiverDict = (dictType: string) => {
  return request.get<DictDataItemRespVO[]>({
    url: '/river/channel-bf/dict',
    params: { dictType }
  })
}

export const getRiverSections = (channelId: string | number) => {
  return request.get<RiverSectionSimpleRespVO[]>({
    url: `/river/channel-bf/${channelId}/sections`
  })
}

export const getRiverSectionWithChannelSimpleList = () => {
  return request.get<RiverSectionWithChannelSimpleRespVO[]>({
    url: '/river/channel-bf/section-simple-list'
  })
}

export const saveRiverManagement = (data: RiverHeadBatchSaveReqVO) => {
  return request.post({
    url: '/river/channel-bf/management/save',
    data
  })
}

export const getRiverManagement = (channelId: string | number) => {
  return request.get<RiverManagementSectionDetailVO[]>({
    url: `/river/channel-bf/${channelId}/management`
  })
}

export const getRiverChiefOverview = (channelId: string | number) => {
  return request.get<RiverChiefOverviewRespVO>({
    url: `/river/channel-bf/${channelId}/chief-overview`
  })
}

export const syncRiverManagementResponsibilities = (channelId: string | number, responsibilities?: string) => {
  return request.put({
    url: `/river/channel-bf/${channelId}/management/responsibilities`,
    data: { responsibilities }
  })
}

// 校验河道编码是否已存在（true：已存在，false：不存在）
export const checkRiverCodeExists = (riverCode: string, excludeId?: string | number) => {
  return request.get<boolean>({
    url: '/river/channel-bf/check-code',
    params: { riverCode, excludeId }
  })
}

// 导出河道 Excel
export const exportRiverChannelExcel = (params: RiverChannelPageReqVO) => {
  return request.download({
    url: '/river/channel-bf/export-excel',
    params
  })
}

// 导入河道 Excel（默认不划分河段）
export const importRiverChannelExcel = (data: FormData) => {
  return request.post({
    url: '/river/channel-bf/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}

// 下载河道导入模板
export const getRiverChannelImportTemplate = () => {
  return request.download({
    url: '/river/channel-bf/get-import-template'
  })
}
