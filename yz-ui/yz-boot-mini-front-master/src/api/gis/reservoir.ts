import request from '@/config/axios'

export interface ReservoirPageReqVO {
  pageNo: number
  pageSize: number
  /** 水库编码，支持模糊查询 */
  reservoirCode?: string
  /** 水库名称，支持模糊查询 */
  reservoirName?: string
  /** 规模（字典：zd_skgm） */
  reservoirScale?: string
  /** 管理单位（字典：zd_gldw） */
  managementUnit?: string
}

export interface ReservoirPageRespVO {
  id: string
  reservoirCode?: string
  reservoirName?: string
  reservoirScale?: string
  managementUnit?: string[] | string
  township?: string[] | string
  townshipName?: string
  reservoirNature?: string
  catchmentArea?: number
  totalCapacity?: number
  activeCapacity?: number
  normalOperatingLevel?: number
  floodLimitLevel?: number
  designFloodLevel?: number
  verifiedFloodLevel?: number
  damCrestElevation?: string
  maxDamHeight?: number
  damTopLength?: string
  deadLevel?: number
  /** 跳转链接 */
  jumpUrl?: string
}

export interface ReservoirSimpleRespVO {
  id: string
  reservoirName?: string
}

export interface ReservoirSaveReqVO {
  id?: string
  /** 设施基础表ID（返回值字段） */
  facilityId?: string
  /** GIS几何数据（GeoJSON，仅geometry，不包含Feature） */
  geometryGeoJson?: string
  /** GIS几何类型（POINT/LINESTRING/POLYGON等，返回值字段） */
  geomType?: string
  /** GIS坐标系SRID（为空默认4490） */
  srid?: number
  reservoirCode?: string
  reservoirName: string
  /** 规模（字典：zd_skgm） */
  reservoirScale?: string
  /** 所在乡镇（行政区划编码，来源：/system/area/tree 的 id） */
  township?: string[]
  /** 所在乡镇名称（来源：/system/area/tree 的 name，用于同步写入基础表 adminRegion） */
  townshipName?: string
  /** 所在地点（可到村/组/坐标等） */
  location?: string
  /** 管理单位（字典：zd_gldw，多选） */
  managementUnit?: string[]
  /** 水库照片 URL 列表 */
  reservoirPhotos?: string[]
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
  /** 主管部门 */
  supervisingDepartment?: string
  /** 水库性质（字典：zd_skxz） */
  reservoirNature?: string
  /** 灌溉面积（亩） */
  irrigationArea?: number
  /** 设计灌溉面积（亩） */
  designIrrigationArea?: number
  /** 实际灌溉面积（亩） */
  actualIrrigationArea?: string
  /** 保护面积（亩） */
  protectionArea?: number
  /** 下游主要设施 */
  downstreamFacilities?: string
  /** 供水对象（城镇/农村/工业等） */
  waterSupplyTarget?: string
  /** 集水面积（平方公里） */
  catchmentArea?: number
  /** 高程基准面（黄海/国家85等） */
  elevationDatum?: string
  /** 设计（复核）抗震烈度 */
  seismicIntensity?: string
  /** 竣工日期（yyyy-MM-dd） */
  completionDate?: string
  /** 除险加固开工年月（yyyy-MM-dd） */
  reinforcementStartDate?: string
  /** 除险加固竣工年月（yyyy-MM-dd） */
  reinforcementEndDate?: string
  /** 除险加固日期（yyyy-MM-dd HH:mm:ss） */
  reinforcementDate?: string
  /** 设计洪水标准（文字/等级） */
  designFloodStandard?: string
  /** 校核洪水标准（文字/等级） */
  verifiedFloodStandard?: string
  /** 重现期设计（年） */
  designReturnPeriod?: number
  /** 重现期校核（年） */
  checkReturnPeriod?: number
  /** 总库容（m3） */
  totalCapacity?: number
  /** 兴利库容（m3） */
  activeCapacity?: number
  /** 调洪库容（m3） */
  floodControlCapacity?: number
  /** 死库容（m3） */
  deadCapacity?: number
  /** 校核水位/校核洪水位（m） */
  verifiedFloodLevel?: number
  /** 设计水位/设计洪水位（m） */
  designFloodLevel?: number
  /** 兴利水位（m） */
  normalOperatingLevel?: number
  /** 汛限水位（m） */
  floodLimitLevel?: number
  /** 死水位（m） */
  deadLevel?: number
  /** 坝顶高程（m） */
  damCrestElevation?: string
  /** 坝顶宽度（m） */
  damTopWidth?: string
  /** 坝顶高度/坝顶相对高（m） */
  damTopHeight?: number
  /** 最大坝高（m） */
  maxDamHeight?: number
  /** 坝顶长度（m） */
  damTopLength?: string
  /** 挡浪墙顶高程（m） */
  waveWallCrestElevation?: number
  /** 坝顶路面结构型式 */
  damRoadSurfaceType?: string
  /** 防渗处理结构型式 */
  seepageControlType?: string
  /** 防渗处理起止桩号 */
  seepagePileRange?: string
  /** 防渗处理起止高程 */
  seepageElevRange?: string
  /** 迎水坡型式 */
  upstreamSlopeType?: string
  /** 迎水坡起止高程 */
  upstreamSlopeElevation?: string
  /** 迎水坡坡比 */
  upstreamSlopeRatio?: string
  /** 背水坡坡比 */
  downstreamSlopeRatio?: string
  /** 护坡结构型式 */
  slopeProtectionType?: string
  /** 护坡起止高程 */
  slopeProtectionElevRange?: string
  /** 背水坡护坝地高程（m） */
  downstreamSlopeElevation?: number
  /** 背水坡护坝地宽（m） */
  downstreamSlopeWidth?: number
  /** 溢洪道型式 */
  spillwayType?: string
  /** 溢洪道控制方式（有闸/开敞式） */
  spillwayControlType?: string
  /** 溢洪道有无交通桥 */
  spillwayHasBridge?: boolean
  /** 溢洪道堰顶高程（m） */
  spillwayCrestElevation?: number
  /** 溢洪道底高程（m） */
  spillwayBottomElevation?: number
  /** 溢洪道底宽（孔*宽）（m） */
  spillwayBottomWidth?: string
  /** 溢洪道最大流量（m3/s） */
  spillwayMaxDischarge?: number
  /** 排洪河道名称 */
  floodChannelName?: string
  /** 排洪河道安全泄量（m3/s） */
  floodChannelSafeDischarge?: string
  /** 灌溉/放水涵洞结构型式 */
  culvertType?: string
  /** 灌溉涵洞断面尺寸（宽*高）（m） */
  culvertSectionSize?: string
  /** 灌溉涵洞闸门型式 */
  culvertGateType?: string
  /** 灌溉涵洞设计流量（m3/s） */
  culvertDesignDischarge?: number
  /** 涵洞出口底高程（m） */
  culvertExitElevation?: number
  /** 涵洞直径（m） */
  culvertDiameter?: number
  /** 涵洞高度（m） */
  culvertHeight?: number
  /** 年供水量（万m3） */
  annualWaterSupply?: number
  /** 宜鱼面积（亩） */
  fisheryArea?: number
  /** 是否水源地（饮用水源地） */
  waterSource?: boolean
  /** 河长职责 */
  responsibilities?: string
  /** 备注 */
  remarks?: string
  /** 跳转链接 */
  jumpUrl?: string
}

export interface ReservoirHeadItemReqVO {
  /** 河长级别(字典: zd_hzjb) */
  headLevel?: string
  /** 河长职务 */
  headPosition?: string
  /** 河长工作单位 */
  headUnit?: string
  /** 河长姓名 */
  headName?: string
  /** 河长职责 */
  responsibilities?: string
  /** 行政区划（可多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
  /** 备注 */
  remarks?: string
}

export interface ReservoirHeadBatchSaveReqVO {
  waterReservoirId: string
  /** 关联对象ID（默认等于 waterReservoirId） */
  referenceId?: string
  /** 关联对象类型（reservoir：水库） */
  referenceType?: string
  heads: ReservoirHeadItemReqVO[]
}

export interface ReservoirChiefOverviewChiefVO {
  id?: string
  headName?: string
  headLevel?: string
  headLevelLabel?: string
  headPosition?: string
  headContact?: string
  effectiveFrom?: string | number
}

export interface ReservoirChiefOverviewRespVO {
  reservoirId?: string
  reservoirName?: string
  reservoirChiefs: ReservoirChiefOverviewChiefVO[]
  totalCount?: number
}

// 分页查询水库
export const getReservoirPage = (params: ReservoirPageReqVO) => {
  return request.get<{ list: ReservoirPageRespVO[]; total: number }>({
    url: '/reservoir/page',
    params
  })
}

// 查询水库详情
export const getReservoirDetail = (id: string | number) => {
  return request.get<ReservoirSaveReqVO>({
    url: `/reservoir/${id}`
  })
}

export const getReservoirDetailByFacility = (facilityId: string | number) => {
  return request.get<ReservoirSaveReqVO>({
    url: `/reservoir/facility/${facilityId}`
  })
}

// 新增水库
export const createReservoir = (data: ReservoirSaveReqVO) => {
  return request.post<number>({
    url: '/reservoir',
    data
  })
}

// 编辑水库
export const updateReservoir = (data: ReservoirSaveReqVO) => {
  return request.put({
    url: '/reservoir',
    data
  })
}

// 删除水库
export const deleteReservoir = (id: string | number) => {
  return request.delete({
    url: `/reservoir/${id}`
  })
}

// 导出水库 Excel
export const exportReservoirExcel = (params: ReservoirPageReqVO) => {
  return request.download({
    url: '/reservoir/export-excel',
    params
  })
}

// 导入水库 Excel
export const importReservoirExcel = (data: FormData) => {
  return request.post({
    url: '/reservoir/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}

// 下载水库导入模板
export const getReservoirImportTemplate = () => {
  return request.download({
    url: '/reservoir/get-import-template'
  })
}

// 查询全部水库（简要信息）
export const getReservoirSimpleList = () => {
  return request.get<ReservoirSimpleRespVO[]>({
    url: '/reservoir/simple-list'
  })
}

// 查询水库河长信息
export const getReservoirManagement = (reservoirId: string | number) => {
  return request.get<ReservoirHeadItemReqVO[]>({
    url: `/reservoir/${reservoirId}/management`
  })
}

// 查询水库页河长概览
export const getReservoirChiefOverview = (reservoirId: string | number) => {
  return request.get<ReservoirChiefOverviewRespVO>({
    url: `/reservoir/${reservoirId}/chief-overview`
  })
}

// 校验水库编码是否已存在（true：已存在；false：不存在）
export const checkReservoirCodeExists = (reservoirCode: string, excludeId?: string | number) => {
  return request.get<boolean>({
    url: '/reservoir/check-code',
    params: { reservoirCode, excludeId }
  })
}

// 保存水库河长信息（覆盖式）
export const saveReservoirManagement = (data: ReservoirHeadBatchSaveReqVO) => {
  return request.post({
    url: '/reservoir/management/save',
    data
  })
}

export const syncReservoirManagementResponsibilities = (reservoirId: string | number, responsibilities?: string) => {
  return request.put({
    url: `/reservoir/${reservoirId}/management/responsibilities`,
    data: { responsibilities }
  })
}
