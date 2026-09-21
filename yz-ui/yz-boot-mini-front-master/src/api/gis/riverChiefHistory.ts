import request from '@/config/axios'

export interface RiverChiefHistoryPageReqVO {
  pageNo: number
  pageSize: number
  /** 关联设施类型（river：河道；river_section：河段；reservoir：水库） */
  referenceType?: string
  /** 关联设施名称关键字（按设施名称模糊匹配） */
  referenceName?: string
  /** 河长级别（字典：zd_hzjb，仅过滤“当前河长”） */
  headLevel?: string
  /** 河长姓名（模糊匹配，仅过滤“当前河长”） */
  headName?: string
}

export interface RiverChiefHistoryPageRespVO {
  referenceType?: string
  referenceTypeLabel?: string
  referenceId?: string | number
  referenceName?: string
  currentHeadNames?: string
  headLevel?: string
  headLevelLabel?: string
  effectiveFrom?: string
  administrativeRegion?: string[]
}

export interface RiverChiefHistoryTimelineItemRespVO {
  id: string | number
  headName?: string
  headLevel?: string
  headLevelLabel?: string
  headPosition?: string
  headUnit?: string
  headContact?: string
  effectiveFrom?: string
  effectiveTo?: string
  referenceType?: string
  referenceTypeLabel?: string
  referenceId?: string | number
  referenceName?: string
  administrativeRegion?: string[]
}

// 分页查询（按设施聚合，仅当前河长）
export const getRiverChiefHistoryPage = (params: RiverChiefHistoryPageReqVO) => {
  return request.get<{ list: RiverChiefHistoryPageRespVO[]; total: number }>({
    url: '/river/chief-history/page',
    params
  })
}

// 查询某个设施下的河长变更时间轴（当前 + 历史）
export const getRiverChiefHistoryTimeline = (params: { referenceType: string; referenceId: string | number }) => {
  return request.get<RiverChiefHistoryTimelineItemRespVO[]>({
    url: '/river/chief-history/timeline',
    params
  })
}

// 导出 Excel（按设施聚合，仅当前河长）
export const exportRiverChiefHistoryExcel = (params: RiverChiefHistoryPageReqVO) => {
  return request.download({
    url: '/river/chief-history/export-excel',
    params
  })
}
