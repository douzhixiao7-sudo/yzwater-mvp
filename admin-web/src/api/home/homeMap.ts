import request from '@/config/axios'

export interface WaterFacilityAreaCountRespVO {
  areaId: number
  count: number
}

export interface WaterFacilityMapItemRespVO {
  /** 主键 ID（可能为后端 Long，前端按字符串处理更安全） */
  id: string | number
  facilityCode: string
  facilityName: string
  facilityType: string
  adminRegionCode: string
  longitude?: number
  latitude?: number
  geomType?: string
}

export interface RiverAreaOverviewItemRespVO {
  /** 河道 ID（可能为后端 Long，前端按字符串处理更安全） */
  riverId: string | number
  /** 设施基础 ID（可能为后端 Long，前端按字符串处理更安全） */
  facilityBaseId: string | number
  riverName: string
  riverLevelLabel: string
  lengthKm?: number
  longitude?: number
  latitude?: number
}

export interface RiverAreaOverviewRespVO {
  totalCount: number
  totalActualIrrigableArea?: number
  totalBasicFarmlandAreaKm2?: number
  totalMainCanalLengthM?: number
  materialTypes?: string
  totalLengthM?: number
  totalStandardLengthM?: number
  totalSelfFlow?: number
  totalInstalledFlow?: number
  totalPumpingFlow?: number
  problemTotalCount?: number
  totalCatchmentKm2?: number
  totalLengthKm?: number
  riverLevelCountMap?: Record<string, number>
  list: RiverAreaOverviewItemRespVO[]
}

export interface ReservoirAreaOverviewItemRespVO {
  /** 水库 ID（可能为后端 Long，前端按字符串处理更安全） */
  reservoirId: string | number
  /** 设施基础 ID（可能为后端 Long，前端按字符串处理更安全） */
  facilityBaseId: string | number
  reservoirName: string
  /** 水库规模（字典：zd_skgm.value） */
  reservoirScale?: string
  /** 总库容（m3） */
  totalCapacity?: number
  totalDamTopLength?: number
  totalActiveCapacity?: number
  longitude?: number
  latitude?: number
  /** GeoJSON 字符串（仅 geometry，用于地图展示与点击） */
  geometryGeoJson?: string
}

export interface ReservoirAreaOverviewRespVO {
  totalCount: number
  /** 总库容（m3） */
  totalCapacity?: number
  /** 总坝顶长度（m） */
  totalDamTopLength?: number
  /** 总兴利库容（m3） */
  totalActiveCapacity?: number
  /** 水库规模数量统计（key 为 zd_skgm.value，value 为数量） */
  reservoirScaleCountMap?: Record<string, number>
  list: ReservoirAreaOverviewItemRespVO[]
}

export interface SignboardAreaOverviewItemRespVO {
  /** 公示牌 ID（可能为后端 Long，前端按字符串处理更安全） */
  signboardId: string | number
  signboardName: string
  signboardType?: string
  signboardTypeLabel?: string
  referenceType?: string
  referenceTypeLabel?: string
  referenceId?: string | number
  referenceName?: string
  specificLocation?: string
  maintenanceUnit?: string
  longitude?: number
  latitude?: number
}

export interface SignboardAreaOverviewRespVO {
  riverCount: number
  reservoirCount: number
  totalCount: number
  problemTotalCount?: number
  list: SignboardAreaOverviewItemRespVO[]
}

export interface SignboardReferenceDetailRespVO {
  referenceType?: string
  referenceId?: string | number
  name?: string
  geomWkt?: string
}

export interface PumpStationAreaOverviewItemRespVO {
  /** 泵站 ID（可能为后端 Long，前端按字符串处理更安全） */
  pumpStationId: string | number
  /** 设施基础 ID（可能为后端 Long，前端按字符串处理更安全） */
  facilityBaseId: string | number
  pumpStationName: string
  pumpStationType?: string
  engineeringGrade?: string
  longitude?: number
  latitude?: number
}

export interface PumpStationAreaOverviewRespVO {
  totalCount: number
  totalSelfFlow?: number
  totalInstalledFlow?: number
  totalPumpingFlow?: number
  pumpStationTypeCountMap?: Record<string, number>
  list: PumpStationAreaOverviewItemRespVO[]
}

export interface EmbankmentAreaOverviewItemRespVO {
  /** 提防 ID（可能为后端 Long，前端按字符串处理更安全） */
  embankmentId: string | number
  /** 设施基础 ID（可能为后端 Long，前端按字符串处理更安全） */
  facilityBaseId: string | number
  embankmentName: string
  embankmentForm?: string
  longitude?: number
  latitude?: number
}

export interface EmbankmentAreaOverviewRespVO {
  totalCount: number
  totalLengthM?: number
  totalStandardLengthM?: number
  list: EmbankmentAreaOverviewItemRespVO[]
}

export interface FloodMaterialWarehouseAreaOverviewItemRespVO {
  /** 仓库 ID（可能为后端 Long，前端按字符串处理更安全） */
  warehouseId: string | number
  warehouseName: string
  belongUnit?: string
  longitude?: number
  latitude?: number
}

export interface FloodMaterialWarehouseAreaOverviewRespVO {
  totalCount: number
  materialTypes?: string
  list: FloodMaterialWarehouseAreaOverviewItemRespVO[]
}

export interface IrrigationDistrictAreaOverviewItemRespVO {
  /** 灌区 ID（可能为后端 Long，前端按字符串处理更安全） */
  irrigationDistrictId: string | number
  /** 设施基础 ID（可能为后端 Long，前端按字符串处理更安全） */
  facilityBaseId: string | number
  irrigationDistrictName: string
  actualIrrigableArea?: number
  basicFarmlandAreaKm2?: number
  /** GeoJSON 字符串（仅 geometry，用于地图展示与点击） */
  geometryGeoJson?: string
}

export interface IrrigationDistrictAreaOverviewRespVO {
  totalCount: number
  totalActualIrrigableArea?: number
  totalBasicFarmlandAreaKm2?: number
  totalMainCanalLengthM?: number
  list: IrrigationDistrictAreaOverviewItemRespVO[]
}

export interface WaterPondAreaOverviewItemRespVO {
  pondId?: string | number
  facilityBaseId?: string | number
  resourceName?: string
  resourceCode?: string
  ownershipType?: string
  resourceType?: string
  usageStatus?: string
  areaSqm?: number
  areaMu?: number
  longitude?: number
  latitude?: number
  geometryGeoJson?: string
}

export interface WaterPondAreaOverviewRespVO {
  totalCount?: number
  totalAreaSqm?: number
  totalAreaMu?: number
  list: WaterPondAreaOverviewItemRespVO[]
}

export interface WaterPondMapOverviewReqVO {
  areaId?: number
  resourceName?: string
  resourceCode?: string
  ownershipType?: string
  resourceType?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  villageName?: string
  locationDesc?: string
  ownerUnit?: string
  areaSqmMin?: number
  areaSqmMax?: number
}

// 按行政区划统计某类设施数量（不分页）
export const getWaterFacilityAreaCount = (facilityType: string) => {
  return request.get<WaterFacilityAreaCountRespVO[]>({
    url: '/water/facility/area-count',
    params: { facilityType }
  })
}

// 按行政区划获取某类设施列表（用于右侧弹窗与地图定位）
export const getWaterFacilityListByArea = (facilityType: string, areaId: number) => {
  return request.get<WaterFacilityMapItemRespVO[]>({
    url: '/water/facility/list-by-area',
    params: { facilityType, areaId }
  })
}

// 按名称模糊查询某类设施列表（用于首页搜索）
export const getWaterFacilityListByName = (facilityType: string, name: string, limit = 100) => {
  return request.get<WaterFacilityMapItemRespVO[]>({
    url: '/water/facility/list-by-name',
    params: { facilityType, name, limit }
  })
}

// 河道总览（不分页，统计全量河道）
export const getRiverAreaOverview = () => {
  return request.get<RiverAreaOverviewRespVO>({
    url: '/screen/statistics/river-area-overview',
  })
}

// 河道总览（不分页，按行政区划筛选，包含子级）
export const getRiverAreaOverviewByArea = (areaId: number) => {
  return request.get<RiverAreaOverviewRespVO>({
    url: '/screen/statistics/river-area-overview-by-area',
    params: { areaId }
  })
}

// 水库总览（不分页，按行政区划筛选，包含子级）
export const getReservoirAreaOverviewByArea = (areaId: number) => {
  return request.get<ReservoirAreaOverviewRespVO>({
    url: '/screen/statistics/reservoir-area-overview-by-area',
    params: { areaId }
  })
}

// 公示牌总览（不分页，按行政区划筛选，包含子级）
export const getSignboardAreaOverviewByArea = (areaId: number) => {
  return request.get<SignboardAreaOverviewRespVO>({
    url: '/screen/statistics/signboard-area-overview-by-area',
    params: { areaId }
  })
}

export const getSignboardReferenceDetail = (referenceType: string, referenceId: string | number) => {
  return request.get<SignboardReferenceDetailRespVO>({
    url: '/screen/statistics/signboard-reference-detail',
    params: { referenceType, referenceId }
  })
}

// 泵站总览（不分页，按行政区划筛选，包含子级）
export const getPumpStationAreaOverviewByArea = (areaId: number) => {
  return request.get<PumpStationAreaOverviewRespVO>({
    url: '/screen/statistics/pump-station-area-overview-by-area',
    params: { areaId }
  })
}

// 提防总览（不分页，按行政区划筛选，包含子级）
export const getEmbankmentAreaOverviewByArea = (areaId: number) => {
  return request.get<EmbankmentAreaOverviewRespVO>({
    url: '/screen/statistics/embankment-area-overview-by-area',
    params: { areaId }
  })
}

// 防汛物资仓库总览（不分页，按行政区划筛选，包含子级）
export const getFloodMaterialWarehouseAreaOverviewByArea = (areaId: number) => {
  return request.get<FloodMaterialWarehouseAreaOverviewRespVO>({
    url: '/screen/statistics/flood-material-warehouse-area-overview-by-area',
    params: { areaId }
  })
}

// 灌区总览（不分页，按行政区划筛选，包含子级）
export const getIrrigationDistrictAreaOverviewByArea = (areaId: number) => {
  return request.get<IrrigationDistrictAreaOverviewRespVO>({
    url: '/screen/statistics/irrigation-area-overview-by-area',
    params: { areaId }
  })
}

// 坑塘总览（不分页，按行政区划 + 业务筛选；返回面几何 GeoJSON）
export const getWaterPondAreaOverviewByArea = (params: WaterPondMapOverviewReqVO) => {
  return request.get<WaterPondAreaOverviewRespVO>({
    url: '/screen/statistics/pond-area-overview-by-area',
    params
  })
}
