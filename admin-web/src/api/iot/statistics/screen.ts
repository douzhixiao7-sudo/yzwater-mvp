import request from '@/config/axios'

export interface IotScreenStationOptionRespVO {
  label: string
  value: string
}

export interface IotScreenDeviceOptionRespVO {
  label: string
  value: string
  deviceType?: number
  deviceTypeName?: string
}

export interface IotScreenStreamWaterListReqVO {
  stationId: string
  deviceNames?: string[]
  startTime?: string
  endTime?: string
  timeRange?: '24h' | '48h' | '72h'
}

export interface IotScreenStreamWaterRespVO {
  time: string
  waterLevel: unknown
  stationId: string
  stationName: string
  deviceId: number
  deviceName: string
}

export interface IotScreenWorkConditionRespVO {
  stationId: string
  stationName: string
  deviceId: number
  deviceName: string
  deviceType?: number
  deviceTypeName?: string
  time?: string
  activePowerKw?: unknown
  reactivePowerKvar?: unknown
  powerFactor?: unknown
  frequency?: unknown
  abVoltage?: unknown
  bcVoltage?: unknown
  caVoltage?: unknown
  aCurrent?: unknown
  bCurrent?: unknown
  cCurrent?: unknown
  aStatorTemp1?: unknown
  bStatorTemp1?: unknown
  cStatorTemp1?: unknown
  aStatorTemp2?: unknown
  bStatorTemp2?: unknown
  cStatorTemp2?: unknown
  isGateOpenAll?: unknown
  isGateCloseAll?: unknown
  isGateUp?: unknown
  isGateDown?: unknown
  isGateFailure?: unknown
  isGatePowerOn?: unknown
  floodgateOpening?: unknown
  waterLevel?: unknown
  pressValue?: unknown
}

export interface IotScreenWorkConditionListReqVO {
  stationId: string
  deviceType?: number
  deviceName?: string
  startTime?: string
  endTime?: string
}

export interface IotScreenPondItemVO {
  /** 字符串，避免雪花 id 精度丢失 */
  id: string
  facilityId?: string
  title?: string
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
  longitude?: number
  latitude?: number
  centerLon?: number
  centerLat?: number
  /** 标签，如权属/类型/状态 */
  tags?: string[]
  /** 面积统计块 */
  stats?: IotScreenPondDisplayField[]
  /** 详情字段列表（含中文 label） */
  fields?: IotScreenPondDisplayField[]
  /** 四至 */
  boundary?: IotScreenPondBoundaryItem[]
  /** 中文字段（页面可直接用） */
  [zhKey: string]: unknown
}

export interface IotScreenPondDisplayField {
  label: string
  value?: unknown
  unit?: string
  key?: string
}

export interface IotScreenPondBoundaryItem {
  dir: string
  label: string
  value?: unknown
  key?: string
}

/** 坑塘视口 bbox */
export interface IotScreenPondMapBounds {
  minLon: number
  minLat: number
  maxLon: number
  maxLat: number
}

/** 坑塘分页请求（value=14 专用；字段对齐台账筛选面板） */
export interface IotScreenPondPageReqVO extends Partial<IotScreenPondMapBounds> {
  id?: string | number
  pageNo?: number
  pageSize?: number
  areaId?: string | number
  villageCode?: string
  includeGeometry?: boolean
  resourceName?: string
  name?: string
  keyword?: string
  resourceCode?: string
  locationDesc?: string
  ownerUnit?: string
  ownershipType?: string
  resourceType?: string
  landType?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  villageName?: string
  remark?: string
  surveyor?: string
  eastTo?: string
  southTo?: string
  westTo?: string
  northTo?: string
  areaMuMin?: number
  areaMuMax?: number
  areaSqmMin?: number
  areaSqmMax?: number
  occupyFarmAreaMin?: number
  occupyFarmAreaMax?: number
}

/** 坑塘分页响应 */
export interface IotScreenPondPageRespVO {
  list: IotScreenPondItemVO[]
  total: number
  pageNo: number
  pageSize: number
  hasMore: boolean
}

export const IOT_SCREEN_POND_DEFAULT_PAGE_SIZE = 500
export const IOT_SCREEN_POND_MAX_PAGE_SIZE = 1000

/** 仪征市 id（加载市界、默认 rootId） */
export const IOT_SCREEN_AREA_DEFAULT_ROOT_ID = '321081'

/** 坑塘 MVT 矢量瓦片图层名（与后端 ST_AsMVT 一致） */
export const IOT_SCREEN_POND_MVT_LAYER = 'ponds'

/** 大屏行政区划树节点（仅结构，不含面） */
export interface IotScreenAreaNodeRespVO {
  id: string
  name: string
  /** 与 system_area.type 一致 */
  type?: number
  children?: IotScreenAreaNodeRespVO[]
}

/** 大屏行政区划详情（与 /system/area/get 一致，含面） */
export interface IotScreenAreaRespVO {
  id: string
  parentId?: string
  name: string
  type?: number
  sort?: number
  gemo?: string
  gemoGeoJson?: string
}

/** 坑塘 MVT 多维过滤（与台账筛选对齐） */
export interface IotScreenPondMvtFilter {
  areaId?: string | number
  villageCode?: string
  resourceName?: string
  name?: string
  keyword?: string
  resourceCode?: string
  locationDesc?: string
  ownerUnit?: string
  ownershipType?: string
  resourceType?: string
  landType?: string
  usageStatus?: string
  resourceNature?: string
  occupationStatus?: string
  villageName?: string
  remark?: string
  surveyor?: string
  eastTo?: string
  southTo?: string
  westTo?: string
  northTo?: string
  areaMuMin?: number
  areaMuMax?: number
  areaSqmMin?: number
  areaSqmMax?: number
  occupyFarmAreaMin?: number
  occupyFarmAreaMax?: number
  centerLon?: number
  centerLat?: number
  bufferRadiusM?: number
}

/** 坑塘筛选下拉选项 */
export interface IotScreenPondFilterOptionsRespVO {
  ownershipTypeList?: string[]
  resourceTypeList?: string[]
  usageStatusList?: string[]
  resourceNatureList?: string[]
  occupationStatusList?: string[]
}

/**
 * 坑塘 MVT 瓦片 URL 模板（给 Cesium / MapLibre / Leaflet.VectorGrid 用）
 * 占位符：{z} {x} {y}
 * 例：`/admin-api/iot/statistics/screen/pond-tiles/{z}/{x}/{y}?areaId=321081104&ownerUnit=xxx`
 */
export function buildPondMvtTileUrlTemplate(
  options?: IotScreenPondMvtFilter & {
    /** 是否带 /admin-api 前缀，默认 true */
    withAdminApiPrefix?: boolean
  }
): string {
  const prefix = options?.withAdminApiPrefix === false ? '' : '/admin-api'
  const base = `${prefix}/iot/statistics/screen/pond-tiles/{z}/{x}/{y}`
  const params = new URLSearchParams()
  const append = (key: string, value: unknown) => {
    if (value == null || value === '') return
    params.set(key, String(value))
  }
  if (options) {
    append('areaId', options.areaId)
    append('villageCode', options.villageCode)
    append('resourceName', options.resourceName || options.name || options.keyword)
    append('resourceCode', options.resourceCode)
    append('locationDesc', options.locationDesc)
    append('ownerUnit', options.ownerUnit)
    append('ownershipType', options.ownershipType)
    append('resourceType', options.resourceType)
    append('landType', options.landType)
    append('usageStatus', options.usageStatus)
    append('resourceNature', options.resourceNature)
    append('occupationStatus', options.occupationStatus)
    append('villageName', options.villageName)
    append('remark', options.remark)
    append('surveyor', options.surveyor)
    append('eastTo', options.eastTo)
    append('southTo', options.southTo)
    append('westTo', options.westTo)
    append('northTo', options.northTo)
    append('areaMuMin', options.areaMuMin)
    append('areaMuMax', options.areaMuMax)
    append('areaSqmMin', options.areaSqmMin)
    append('areaSqmMax', options.areaSqmMax)
    append('occupyFarmAreaMin', options.occupyFarmAreaMin)
    append('occupyFarmAreaMax', options.occupyFarmAreaMax)
    append('centerLon', options.centerLon)
    append('centerLat', options.centerLat)
    append('bufferRadiusM', options.bufferRadiusM)
  }
  const qs = params.toString()
  return qs ? `${base}?${qs}` : base
}

export const IotScreenStatisticsApi = {
  /** 行政区划树（仅结构）。对应一张图 getAreaTree */
  getAreaTree: async (rootId?: string | number) => {
    return await request.get<IotScreenAreaNodeRespVO[]>({
      url: '/iot/statistics/screen/area-tree',
      params: rootId == null || rootId === '' ? undefined : { rootId }
    })
  },

  /** 行政区划详情（含 gemoGeoJson 面）。对应一张图 getArea，点击区划后按需调用 */
  getArea: async (id: string | number) => {
    return await request.get<IotScreenAreaRespVO>({
      url: '/iot/statistics/screen/area-get',
      params: { id: String(id) }
    })
  },

  /** 坑塘筛选下拉（土地权属/资源类型/使用状态/资源性质/占用情况） */
  getPondFilterOptions: async () => {
    return await request.get<IotScreenPondFilterOptionsRespVO>({
      url: '/iot/statistics/screen/pond-filter-options'
    })
  },

  getStationOptions: async () => {
    return await request.get<IotScreenStationOptionRespVO[]>({
      url: '/iot/statistics/screen/station-options'
    })
  },

  getDeviceOptions: async (stationId: string, deviceType?: number) => {
    return await request.get<IotScreenDeviceOptionRespVO[]>({
      url: '/iot/statistics/screen/device-options',
      params: { stationId, deviceType }
    })
  },

  getStreamWaterList: async (params: IotScreenStreamWaterListReqVO) => {
    return await request.get<IotScreenStreamWaterRespVO[]>({
      url: '/iot/statistics/screen/stream-water-list',
      params
    })
  },

  getWorkConditionList: async (params: IotScreenWorkConditionListReqVO) => {
    return await request.get<IotScreenWorkConditionRespVO[]>({
      url: '/iot/statistics/screen/work-condition-list',
      params
    })
  },

  /**
   * 坑塘分页（value=14）。
   * 列表：pageNo / pageSize 必传；bbox 可选。
   * 详情：传 id 即可（可不传分页），也可直接用 getPondDetail。
   */
  getPondPage: async (params: IotScreenPondPageReqVO, config?: { signal?: AbortSignal }) => {
    return await request.get<IotScreenPondPageRespVO>({
      url: '/iot/statistics/screen/facility-by-slss',
      params: { value: 14, ...params },
      signal: config?.signal
    })
  },

  /**
   * 坑塘详情（value=14&id=xxx）。
   * 返回中文字段 + tags/stats/fields/boundary，便于弹窗渲染。
   */
  getPondDetail: async (
    id: string,
    options?: { includeGeometry?: boolean },
    config?: { signal?: AbortSignal }
  ) => {
    return await request.get<IotScreenPondItemVO>({
      url: '/iot/statistics/screen/facility-by-slss',
      params: { value: 14, id: String(id), includeGeometry: options?.includeGeometry },
      signal: config?.signal
    })
  },

  /** 非坑塘设施：单值返回数组；多值返回 { label: [] } */
  getFacilityBySlss: async (
    value: string | string[] | number | number[],
    areaId?: string | number
  ) => {
    return await request.get<IotScreenPondItemVO[] | Record<string, IotScreenPondItemVO[]>>({
      url: '/iot/statistics/screen/facility-by-slss',
      params: { value, areaId }
    })
  }
}
