import request from '@/config/axios'

export interface EmbankmentPageReqVO {
  pageNo: number
  pageSize: number
  embankmentCode?: string
  embankmentName?: string
  embankmentLevel?: string
  embankmentType?: string
}

export interface EmbankmentPageRespVO {
  id: string
  embankmentCode: string
  embankmentName: string
  riverBankSideLabel?: string
  embankmentLevelLabel?: string
  embankmentTypeLabel?: string
  embankmentFormLabel?: string
  designHighTide?: string
  lengthM?: number
  startPoint?: string
  endPoint?: string
  managementDepartment?: string
}

export interface EmbankmentSaveReqVO {
  id?: string
  /** 堤防名称 */
  embankmentName: string
  /** 堤防代码，新增时可不传，由后台自动生成 */
  embankmentCode?: string
  /** 区划代码（可多选，行政区划树选择的 id 列表） */
  divisionCode?: string[]
  /** 所在河道 ID */
  riverChannelId?: string
  /** 所在河段 ID（可选） */
  riverSectionId?: string
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
  /** 河流岸别（字典：zd_hlab） */
  riverBankSide?: string
  /** 堤防跨界情况 */
  crossBoundaryStatus?: string
  /** 堤防类型（字典：zd_dflx） */
  embankmentType?: string
  /** 堤防形式（字典：zd_dfxs） */
  embankmentForm?: string
  /** 堤防级别（字典：zd_dfjb） */
  embankmentLevel?: string
  /** 防洪标准 */
  floodStandard?: string
  /** 设计重现期（年） */
  designReturnPeriod?: number
  /** 堤防长度(m) */
  lengthM?: number
  /** 标准长度(m) */
  standardLengthM?: number
  /** 高程系统 */
  elevationSystem?: string
  /** 设计高潮位(m) */
  designHighTide?: string
  /** 堤防最大高度(m) */
  maxHeight?: number
  /** 堤防最小高度(m) */
  minHeight?: number
  /** 堤防最大宽度(m) */
  maxWidth?: number
  /** 堤防最小宽度(m) */
  minWidth?: number
  /** 堤顶高程(m) */
  crestElevation?: number
  /** 起点 */
  startPoint?: string
  /** 终点 */
  endPoint?: string
  /** 工程任务 */
  projectTask?: string
  /** 终点所在位置 */
  endLocation?: string
  /** 工程建设情况 */
  constructionStatus?: string
  /** 归口管理部门 */
  managementDepartment?: string
  /** 堤防图片（最多 5 张） */
  embankmentImages?: string[]
  /** 备注 */
  remarks?: string
}

export const getEmbankmentPage = (params: EmbankmentPageReqVO) => {
  return request.get<{ list: EmbankmentPageRespVO[]; total: number }>({
    url: '/embankment/page',
    params
  })
}

// 查询堤防详情
export const getEmbankmentDetail = (id: string | number) => {
  return request.get<EmbankmentSaveReqVO>({
    url: `/embankment/${id}`
  })
}

// 新增堤防
export const createEmbankment = (data: EmbankmentSaveReqVO) => {
  return request.post<string>({
    url: '/embankment',
    data
  })
}

// 编辑堤防
export const updateEmbankment = (data: EmbankmentSaveReqVO) => {
  return request.put({
    url: '/embankment',
    data
  })
}

// 删除堤防
export const deleteEmbankment = (id: string | number) => {
  return request.delete({
    url: `/embankment/${id}`
  })
}

// 导出堤防 Excel
export const exportEmbankmentExcel = (params: EmbankmentPageReqVO) => {
  return request.download({
    url: '/embankment/export-excel',
    params
  })
}

// 导入堤防 Excel
export const importEmbankmentExcel = (data: FormData) => {
  return request.post({
    url: '/embankment/import-excel',
    data,
    headersType: 'multipart/form-data'
  })
}

// 下载堤防导入模板
export const getEmbankmentImportTemplate = () => {
  return request.download({
    url: '/embankment/get-import-template'
  })
}
