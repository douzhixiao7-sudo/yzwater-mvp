import request from '@/config/axios'

/** 巡检标准记录项模板 */
export interface InspectionStandardItemRecordVO {
  id?: number
  attrName: string
  attrUnit: string
  valueType?: string
  defaultValue: string
  requiredFlag?: number
  remark?: string
  sort?: number
}

/** 巡检标准检查项 */
export interface InspectionStandardItemVO {
  id?: number
  itemName: string
  itemDesc: string
  qualifiedRule: string
  needUploadAttachment?: number
  defaultResult?: string
  sort?: number
  recordCount?: number
  recordTemplates: InspectionStandardItemRecordVO[]
}

/** 巡检标准适用对象 */
export interface InspectionStandardTargetVO {
  id?: number
  targetType: string
  targetId: string | number
  targetName?: string
  stationId?: string
  sort?: number
  itemCount?: number
  checkResultConfigs?: InspectionCheckResultConfigVO[]
  items: InspectionStandardItemVO[]
}

/** 巡检检查结果等级配置 */
export interface InspectionCheckResultConfigVO {
  value: string
  label: string
  remark: string
}

/** 巡检标准 */
export interface InspectionStandardVO {
  id?: number
  stationId: string
  standardName: string
  inspectionType: string
  suggestCycleUnit: string
  suggestCycleValue?: number
  status: number
  targetCount?: number
  itemCount?: number
  targetTypes?: string[]
  targetNames?: string[]
  remark?: string
  createTime?: string | number
  targets: InspectionStandardTargetVO[]
}

/** 巡检标准分页请求 */
export interface InspectionStandardPageReqVO {
  pageNo: number
  pageSize: number
  stationId?: string
  standardName?: string
  inspectionType?: string
  targetType?: string
  status?: number
  createTime?: string[]
}

/** 适用对象下拉 */
export interface InspectionTargetOptionVO {
  id: string | number
  name: string
  targetType: string
  stationId?: string
}

export interface InspectionTargetOptionReqVO {
  targetType: string
  stationId?: string
  keyword?: string
  limit?: number
}

export const InspectionStandardApi = {
  // 分页查询
  getInspectionStandardPage: async (params: InspectionStandardPageReqVO) => {
    return await request.get({ url: '/iot/inspection-standard/page', params })
  },

  // 获取详情
  getInspectionStandard: async (id: string | number) => {
    return await request.get<InspectionStandardVO>({ url: `/iot/inspection-standard/get?id=${id}` })
  },

  // 新增
  createInspectionStandard: async (data: InspectionStandardVO) => {
    return await request.post({ url: '/iot/inspection-standard/create', data })
  },

  // 更新
  updateInspectionStandard: async (data: InspectionStandardVO) => {
    return await request.put({ url: '/iot/inspection-standard/update', data })
  },

  // 删除
  deleteInspectionStandard: async (id: number) => {
    return await request.delete({ url: `/iot/inspection-standard/delete?id=${id}` })
  },

  // 查询适用对象下拉
  getInspectionTargetOptions: async (params: InspectionTargetOptionReqVO) => {
    return await request.get<InspectionTargetOptionVO[]>({
      url: '/iot/inspection-standard/target-options',
      params
    })
  }
}
