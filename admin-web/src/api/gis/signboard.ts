import request from '@/config/axios'

export interface SignboardPageReqVO {
  pageNo: number
  pageSize: number
  signboardName?: string
  signboardCode?: string
  riverKeyword?: string
  maintenanceUnit?: string
  signboardLevel?: string[]
  isScreenDisplay?: number
}

export interface SignboardPageRespVO {
  id: string
  signboardCode: string
  signboardName: string
  signboardLevel?: string
  signboardLevelLabel?: string
  isScreenDisplay?: number
  referenceType?: string
  riverChannelName?: string
  riverSectionName?: string
  riverName?: string
  waterReservoirName?: string
  specificLocation?: string
  adminRegion?: string
  maintenanceUnit?: string
  responsiblePerson?: string
  managementUnit?: string
  ownershipUnit?: string
}

export interface SignboardSaveReqVO {
  id?: string
  signboardCode: string
  signboardName: string
  signboardType?: string
  signboardLevel?: string
  isScreenDisplay: number
  riverChannelId?: string
  riverSectionId?: string
  /** 关联水库ID（可选） */
  waterReservoirId?: string
  /** 关联对象ID（river：河道；river_section：河段；reservoir：水库） */
  referenceId?: string
  /** 关联对象类型（river：河道；river_section：河段；reservoir：水库） */
  referenceType?: string
  /** 关联水库名称（详情回显用） */
  waterReservoirName?: string
  /** 二维码标识字段（详情接口返回） */
  qrCode?: string
  longitude?: number
  latitude?: number
  specificLocation?: string
  adminRegion?: string
  maintenanceUnit?: string[]
  responsiblePerson?: string
  managementUnit?: string[]
  ownershipUnit?: string[]
  signboardImages?: string[]
  content?: string
  remarks?: string
}

export interface SignboardImportRespVO {
  totalCount?: number
  successCount?: number
  skipCount?: number
  failureCount?: number
  errors?: string[]
}

export const getSignboardPage = (params: SignboardPageReqVO) => {
  return request.get<{ list: SignboardPageRespVO[]; total: number }>({
    url: '/river/signboard/page',
    params
  })
}

export const createSignboard = (data: SignboardSaveReqVO) => {
  return request.post<string>({
    url: '/river/signboard',
    data
  })
}

export const getSignboardDetail = (id: string | number) => {
  return request.get<SignboardSaveReqVO>({
    url: `/river/signboard/${id}`
  })
}

export const updateSignboard = (data: SignboardSaveReqVO) => {
  return request.put({
    url: '/river/signboard',
    data
  })
}

export const deleteSignboard = (id: string | number) => {
  return request.delete({
    url: `/river/signboard/${id}`
  })
}

// 导出公示牌 Excel
export const exportSignboardExcel = (params: SignboardPageReqVO) => {
  return request.download({
    url: '/river/signboard/export-excel',
    params
  })
}

// 导入公示牌 Excel
export const importSignboardExcel = (data: FormData) => {
  return request.post<SignboardImportRespVO>({
    url: '/river/signboard/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}

// 下载公示牌导入模板
export const getSignboardImportTemplate = () => {
  return request.download({
    url: '/river/signboard/get-import-template'
  })
}
