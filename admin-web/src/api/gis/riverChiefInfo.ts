import request from '@/config/axios'

export interface RiverChiefInfoPageReqVO {
  pageNo: number
  pageSize: number
  /** 河长姓名（支持模糊查询） */
  headName?: string
  /** 河长级别（字典：zd_hzjb） */
  headLevel?: string[]
  /** 关联设施类型（river/river_section/reservoir） */
  referenceType?: string
  /** 关联设施名称（模糊查询） */
  referenceName?: string
}

export interface RiverChiefInfoPageRespVO {
  id: string
  headName?: string
  headLevel?: string
  headPosition?: string
  headUnit?: string
  facilitySummary?: string
  administrativeRegion?: string[]
  effectiveFrom?: string
  /** 合并展示维度下全部成员记录 ID（姓名+级别+职务相同的多条；与后端 Long 序列化一致，多为 string） */
  memberIds?: string[]
}

export interface RiverChiefInfoFacilityRespVO {
  referenceType?: string
  referenceTypeLabel?: string
  referenceId?: string
  referenceName?: string
}

export interface RiverChiefInfoFacilitySaveReqVO {
  referenceType: string
  referenceId: string
}

export interface RiverChiefInfoDetailRespVO {
  id: string
  headName?: string
  headLevel?: string
  headPosition?: string
  headUnit?: string
  headContact?: string
  responsibilities?: string
  remarks?: string
  administrativeRegion?: string[]
  effectiveFrom?: string
  facilities?: RiverChiefInfoFacilityRespVO[]
  memberIds?: string[]
}

export interface RiverChiefInfoCreateReqVO {
  headName: string
  headLevel: string
  headPosition?: string
  headUnit?: string
  responsibilities?: string
  remarks?: string
  facilities: RiverChiefInfoFacilitySaveReqVO[]
}

export interface RiverChiefInfoUpdateReqVO {
  id: string
  headName: string
  headLevel: string
  headPosition?: string
  headUnit?: string
  responsibilities?: string
  remarks?: string
  facilities?: RiverChiefInfoFacilitySaveReqVO[]
}

export interface RiverChiefInfoImportRespVO {
  totalCount: number
  successCount: number
  skipCount: number
  failureCount: number
  errors: string[]
}

export interface RiverChiefInfoTotalChiefRespVO {
  id: string
  headName?: string
  headLevel?: string
  headPosition?: string
}

export interface RiverChiefInfoTotalChiefSaveReqVO {
  id?: string
  headName: string
  headLevel: string
  headPosition?: string
}

// 分页查询河长信息（按河长维度聚合）
export const getRiverChiefInfoPage = (params: RiverChiefInfoPageReqVO) => {
  return request.get<{ list: RiverChiefInfoPageRespVO[]; total: number }>({
    url: '/river/chief-info/page',
    params
  })
}

// 查询河长详情（包含关联设施列表）
export const getRiverChiefInfoDetail = (id: string | number) => {
  return request.get<RiverChiefInfoDetailRespVO>({
    url: `/river/chief-info/${id}`
  })
}

// 查询总河长列表
export const getRiverChiefTotalChiefList = () => {
  return request.get<RiverChiefInfoTotalChiefRespVO[]>({
    url: '/river/chief-info/total-chief/list'
  })
}

// 新增河长（支持多设施关联）
export const createRiverChiefInfo = (data: RiverChiefInfoCreateReqVO) => {
  return request.post({
    url: '/river/chief-info',
    data
  })
}

// 新增总河长
export const createRiverChiefTotalChief = (data: RiverChiefInfoTotalChiefSaveReqVO) => {
  return request.post({
    url: '/river/chief-info/total-chief',
    data
  })
}

// 编辑河长（按河长维度同步更新全部关联设施）
export const updateRiverChiefInfo = (data: RiverChiefInfoUpdateReqVO) => {
  return request.put({
    url: '/river/chief-info',
    data
  })
}

// 编辑总河长
export const updateRiverChiefTotalChief = (data: RiverChiefInfoTotalChiefSaveReqVO) => {
  return request.put({
    url: '/river/chief-info/total-chief',
    data
  })
}

// 删除河长（按河长维度删除全部关联设施）
export const deleteRiverChiefInfo = (id: string | number) => {
  return request.delete({
    url: `/river/chief-info/${id}`
  })
}

// 删除总河长
export const deleteRiverChiefTotalChief = (id: string | number) => {
  return request.delete({
    url: `/river/chief-info/total-chief/${id}`
  })
}

// 批量导入河长信息 Excel
export const importRiverChiefInfoExcel = (data: FormData) => {
  return request.post<RiverChiefInfoImportRespVO>({
    url: '/river/chief-info/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}
