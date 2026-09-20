import request from '@/config/axios'

export interface IrrigationDistrictPageReqVO {
  pageNo: number
  pageSize: number
  /** 关联基础表ID */
  facilityId?: number
  /** 灌区名称（模糊查询） */
  irrigationDistrictName?: string
  /** 所在流域 */
  basinCode?: string
  /** 行政区划（单个节点筛选，数组中包含该值即可命中） */
  divisionCode?: string
  /** 是否生态红线(0 否 1 是) */
  isEcologicalRedLine?: number
  /** 是否开发边界(0 否 1 是) */
  isDevelopmentBoundary?: number
  /** 负责人（模糊查询） */
  leaderName?: string
  /** 联系电话（模糊查询） */
  leaderPhone?: string
  /** 灌区类型（字典：zd_gqlx） */
  irrigationDistrictType?: string
}

export interface IrrigationDistrictPageRespVO {
  id: number
  facilityId?: number
  irrigationDistrictName?: string
  basinCode?: string
  divisionCode?: string[]
  designIrrigationArea?: number
  actualIrrigableArea?: number
  basicFarmlandAreaKm2?: number
  isEcologicalRedLine?: number
  isDevelopmentBoundary?: number
  mainCanalLengthM?: number
  leaderName?: string
  leaderPhone?: string
  managementUnit?: string[]
  irrigationDistrictType?: string
}

export interface IrrigationDistrictSaveReqVO {
  id?: number
  /** 关联基础表ID（系统自动维护） */
  facilityId?: number
  /** GIS几何数据（GeoJSON，仅 geometry，不包含 Feature） */
  geometryGeoJson?: string
  /** GIS坐标系 SRID（为空默认 4490） */
  srid?: number
  /** 灌区名称 */
  irrigationDistrictName: string
  /** 灌区图片（text[]） */
  irrigationDistrictImages?: string[]
  /** 备注 */
  remarks?: string
  /** 所在流域 */
  basinCode?: string
  /** 行政区划（text[]） */
  divisionCode?: string[]
  /** 设计灌溉面积（万亩） */
  designIrrigationArea?: number
  /** 实际可灌面积 */
  actualIrrigableArea?: number
  /** 实际基本农田面积(k㎡) */
  basicFarmlandAreaKm2?: number
  /** 是否生态红线(0 否 1 是) */
  isEcologicalRedLine?: number
  /** 是否开发边界(0 否 1 是) */
  isDevelopmentBoundary?: number
  /** 干渠长度(单位 m) */
  mainCanalLengthM?: number
  /** 负责人 */
  leaderName?: string
  /** 联系电话 */
  leaderPhone?: string
  /** 管理单位（text[]，字典：zd_gldw） */
  managementUnit?: string[]
  /** 灌区类型（字典：zd_gqlx） */
  irrigationDistrictType?: string
}

export const getIrrigationDistrictPage = (params: IrrigationDistrictPageReqVO) => {
  return request.get<{ list: IrrigationDistrictPageRespVO[]; total: number }>({
    url: '/irrigation-district/page',
    params
  })
}

export const getIrrigationDistrictDetail = (id: string | number) => {
  return request.get<IrrigationDistrictSaveReqVO>({
    url: `/irrigation-district/${id}`
  })
}

export const getIrrigationDistrictDetailByFacility = (facilityId: string | number) => {
  return request.get<IrrigationDistrictSaveReqVO>({
    url: `/irrigation-district/facility/${facilityId}`
  })
}

export const createIrrigationDistrict = (data: IrrigationDistrictSaveReqVO) => {
  return request.post<number>({
    url: '/irrigation-district',
    data
  })
}

export const updateIrrigationDistrict = (data: IrrigationDistrictSaveReqVO) => {
  return request.put({
    url: '/irrigation-district',
    data
  })
}

export const deleteIrrigationDistrict = (id: string | number) => {
  return request.delete({
    url: `/irrigation-district/${id}`
  })
}

export const exportIrrigationDistrictExcel = (params: IrrigationDistrictPageReqVO) => {
  return request.download({
    url: '/irrigation-district/export-excel',
    params
  })
}
