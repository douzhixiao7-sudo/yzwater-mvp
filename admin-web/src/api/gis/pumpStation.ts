import request from '@/config/axios'

export interface PumpStationPageReqVO {
  pageNo: number
  pageSize: number
  /** 泵站名称，支持模糊查询 */
  pumpStationName?: string
  /** 泵站类型（字典：zd_bzlx） */
  pumpStationType?: string
  /** 区划代码（来源：/system/area/tree 的 id） */
  divisionCode?: string
}

export interface PumpStationPageRespVO {
  id: string
  pumpStationCode?: string
  pumpStationName?: string
  /** 机组数量 */
  unitCount?: number
  /** 自排流量(m3/s) */
  selfFlow?: number
  /** 抽引流量(m3/s) */
  installedFlow?: number
  /** 抽排流量(m3/s) */
  pumpingFlow?: number
  /** 常水位(m) */
  normalWaterLevel?: number
  /** 防办预降水位(m) */
  preDropWaterLevel?: number
  /** 最低运行水位(m) */
  minimumOperatingWaterLevel?: number
  /** 单机组功率(KW) */
  singleUnitPower?: number
  pumpStationTypeLabel?: string
  divisionCode?: string[]
  longitude?: number
  latitude?: number
  installedCapacityKw?: number
  capacityFlow?: number
  pumpStationOverview?: string
}

export interface PumpStationSaveReqVO {
  id?: string
  /** 泵站代码 */
  pumpStationCode?: string
  /** 泵站名称 */
  pumpStationName: string
  /** 区划代码（来源：/system/area/tree 的 id，可多选） */
  divisionCode?: string[]
  /** 泵站类型（字典：zd_bzlx） */
  pumpStationType?: string
  /** 具体位置 */
  pumpStationPosition?: string
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
  /** 工程等别（字典：zd_gcdb） */
  engineeringGrade?: string
  /** 闸站规模 */
  engineeringScale?: string
  /** 防洪设计标准 */
  floodControlDesignStandard?: string
  /** 装机功率(KW) */
  installedCapacityKw?: number
  /** 装机流量(m3/s) */
  capacityFlow?: number
  /** 机组数量 */
  unitCount?: number
  /** 常水位(m) */
  normalWaterLevel?: number
  /** 防办预降水位(m) */
  preDropWaterLevel?: number
  /** 最低运行水位(m) */
  minimumOperatingWaterLevel?: number
  /** 单机组功率(KW) */
  singleUnitPower?: number
  /** 自排流量(m3/s) */
  selfFlow?: number
  /** 抽引流量(m3/s) */
  installedFlow?: number
  /** 抽排流量(m3/s) */
  pumpingFlow?: number
  /** 建设时间（yyyy-MM-dd） */
  constructionTime?: string
  /** 归口管理部门（字典：zd_gldw，多选） */
  managementDepartment?: string[]
  /** 泵站图片（最多 5 张） */
  pumpStationImages?: string[]
  /** 泵站概览 */
  pumpStationOverview?: string
}

export const getPumpStationPage = (params: PumpStationPageReqVO) => {
  return request.get<{ list: PumpStationPageRespVO[]; total: number }>({
    url: '/pump-station/page',
    params
  })
}

// 查询泵站详情
export const getPumpStationDetail = (id: string | number) => {
  return request.get<PumpStationSaveReqVO>({
    url: `/pump-station/${id}`
  })
}

// 根据设施ID查询泵站详情
export const getPumpStationDetailByFacility = (facilityId: string | number) => {
  return request.get<PumpStationSaveReqVO>({
    url: `/pump-station/facility/${facilityId}`
  })
}

// 新增泵站
export const createPumpStation = (data: PumpStationSaveReqVO) => {
  return request.post<string>({
    url: '/pump-station',
    data
  })
}

// 编辑泵站
export const updatePumpStation = (data: PumpStationSaveReqVO) => {
  return request.put({
    url: '/pump-station',
    data
  })
}

// 删除泵站
export const deletePumpStation = (id: string | number) => {
  return request.delete({
    url: `/pump-station/${id}`
  })
}

// 导出泵站 Excel
export const exportPumpStationExcel = (params: PumpStationPageReqVO) => {
  return request.download({
    url: '/pump-station/export-excel',
    params
  })
}
