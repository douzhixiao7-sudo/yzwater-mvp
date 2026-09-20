import request from '@/config/axios'

/** IoT 实时数据点位映射信息 */
export interface RealtimeDataMapping {
  id?: string
  sourceId?: string
  pointName?: string
  deviceId?: number | string
  deviceName?: string
  identifier?: string
  enabled?: boolean
  sort?: number
  tenantId?: number
  createTime?: string
}

export interface RealtimeDataMappingImportItem {
  pointName: string
  identifier: string
}

export interface RealtimeDataMappingBatchImportReq {
  sourceId: string | number
  deviceId: string | number
  items: RealtimeDataMappingImportItem[]
}

export interface RealtimeDataMappingBatchImportResp {
  totalCount: number
  importedCount: number
  skippedCount: number
}

export interface RealtimeDataMappingBatchAddByStationPreview {
  deviceId: string | number
  deviceName?: string
  existingCount: number
  importCount: number
  items: RealtimeDataMappingImportItem[]
}

export interface RealtimeDataMappingBatchAddByStationReq {
  sourceId: string | number
  stationId: string
}

export interface RealtimeDataMappingBatchAddByStationResp {
  deviceCount: number
  importedCount: number
  deletedCount: number
  skippedDeviceCount: number
}


// IoT 实时数据点位映射 API
export const RealtimeDataMappingApi = {
  // 查询点位映射分页
  getMappingPage: async (params: any) => {
    return await request.get({ url: `/iot/realtime-data-mapping/page`, params })
  },

  // 查询点位映射详情
  getMapping: async (id: string) => {
    return await request.get({ url: `/iot/realtime-data-mapping/get?id=` + id })
  },

  // 新增点位映射
  createMapping: async (data: RealtimeDataMapping) => {
    return await request.post({ url: `/iot/realtime-data-mapping/create`, data })
  },

  // 修改点位映射
  updateMapping: async (data: RealtimeDataMapping) => {
    return await request.put({ url: `/iot/realtime-data-mapping/update`, data })
  },

  // 删除点位映射
  deleteMapping: async (id: string) => {
    return await request.delete({ url: `/iot/realtime-data-mapping/delete?id=` + id })
  },

  // 批量删除点位映射
  deleteMappingList: async (ids: Array<string | number>) => {
    return await request.delete({
      url: `/iot/realtime-data-mapping/delete-list`,
      params: { ids: ids.join(',') }
    })
  },

  // 查询指定采集源和设备的点位名称列表
  getPointNameList: async (params: { sourceId: string | number; deviceId: string | number }) => {
    return await request.get({ url: `/iot/realtime-data-mapping/point-name-list`, params })
  },

  // 获取批量导入预览列表
  getImportPreview: async (params: { sourceId: string | number; deviceId: string | number }) => {
    return await request.get({ url: `/iot/realtime-data-mapping/import-preview`, params })
  },

  // 获取批量导入预览列表（仅标签）
  getImportPreviewByTag: async (params: { sourceId: string | number; deviceId: string | number }) => {
    return await request.get({ url: `/iot/realtime-data-mapping/import-preview-by-tag`, params })
  },

  // 批量导入点位映射
  importBatch: async (data: RealtimeDataMappingBatchImportReq) => {
    return await request.post({ url: `/iot/realtime-data-mapping/import-batch`, data })
  },

  // 站点设备批量新增预览
  getBatchAddPreview: async (params: { sourceId: string | number; stationId: string }) => {
    return await request.get({ url: `/iot/realtime-data-mapping/batch-add-preview`, params })
  },

  // 站点设备批量新增
  batchAddByStation: async (data: RealtimeDataMappingBatchAddByStationReq) => {
    return await request.post({ url: `/iot/realtime-data-mapping/batch-add-by-station`, data })
  }

}
