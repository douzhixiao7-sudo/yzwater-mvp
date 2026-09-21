import request from '@/config/axios'

export interface RiverChiefManagementPageReqVO {
  pageNo: number
  pageSize: number
  /** 河长名称（支持模糊查询） */
  headName?: string
  /** 河长级别（字典：zd_hzjb） */
  headLevel?: string
  /** 行政区划（可为空，多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
  /** ???????river/river_section/reservoir? */
  referenceType?: string
  /** ???????????? */
  referenceName?: string
}

export interface RiverChiefManagementPageRespVO {
  id: string
  headName?: string
  headLevel?: string
  /** 关联设施类型（river：河道；river_section：河段；reservoir：水库） */
  referenceType?: string
  /** 关联设施类型名称 */
  referenceTypeLabel?: string
  /** 关联设施名称 */
  referenceName?: string
  /** 生效时间 */
  effectiveFrom?: string
  /** 河长联系电话 */
  headContact?: string
  /** 行政区划（可为空，多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
}

export interface RiverChiefManagementDetailRespVO {
  id: string
  riverChannelId?: string
  riverSectionId?: string
  waterReservoirId?: string
  referenceId?: string
  referenceType?: string
  referenceTypeLabel?: string
  referenceName?: string
  headName?: string
  headLevel?: string
  headPosition?: string
  headUnit?: string
  headContact?: string
  responsibilities?: string
  /** 行政区划（可多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
  remarks?: string
  effectiveFrom?: string
}

export interface RiverChiefManagementSaveReqVO {
  id?: string
  referenceType: string
  referenceId: string
  headName: string
  headLevel?: string
  headPosition?: string
  headUnit?: string
  responsibilities?: string
  /** 行政区划（可多选，来源：/system/area/tree 的 id） */
  administrativeRegion?: string[]
  remarks?: string
}

// 分页查询河长（仅当前有效）
export const getRiverChiefManagementPage = (params: RiverChiefManagementPageReqVO) => {
  return request.get<{ list: RiverChiefManagementPageRespVO[]; total: number }>({
    url: '/river/chief-management/page',
    params
  })
}

// 查询河长详情
export const getRiverChiefManagementDetail = (id: string | number) => {
  return request.get<RiverChiefManagementDetailRespVO>({
    url: `/river/chief-management/${id}`
  })
}

// 新增河长
export const createRiverChiefManagement = (data: RiverChiefManagementSaveReqVO) => {
  return request.post<string>({
    url: '/river/chief-management',
    data
  })
}

// 编辑河长
export const updateRiverChiefManagement = (data: RiverChiefManagementSaveReqVO) => {
  return request.put({
    url: '/river/chief-management',
    data
  })
}

// 删除河长
export const deleteRiverChiefManagement = (id: string | number) => {
  return request.delete({
    url: `/river/chief-management/${id}`
  })
}

// 导出河长 Excel
export const exportRiverChiefManagementExcel = (params: RiverChiefManagementPageReqVO) => {
  return request.download({
    url: '/river/chief-management/export-excel',
    params
  })
}
