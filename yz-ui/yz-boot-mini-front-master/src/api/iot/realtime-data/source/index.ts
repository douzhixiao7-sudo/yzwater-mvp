import request from '@/config/axios'

/** IoT 实时数据采集源信息 */
export interface RealtimeDataSource {
  id?: string
  name?: string
  code?: string
  enabled?: boolean
  url?: string
  username?: string
  password?: string
  requestBody?: string
  cron?: string
  remark?: string
  createTime?: string | number
}

// IoT 实时数据采集源 API
export const RealtimeDataSourceApi = {
  // 查询采集源分页
  getSourcePage: async (params: any) => {
    return await request.get({ url: `/iot/realtime-data-source/page`, params })
  },

  // 查询采集源详情
  getSource: async (id: string) => {
    return await request.get({ url: `/iot/realtime-data-source/get?id=` + id })
  },

  // 新增采集源
  createSource: async (data: RealtimeDataSource) => {
    return await request.post({ url: `/iot/realtime-data-source/create`, data })
  },

  // 修改采集源
  updateSource: async (data: RealtimeDataSource) => {
    return await request.put({ url: `/iot/realtime-data-source/update`, data })
  },

  // 删除采集源
  deleteSource: async (id: string) => {
    return await request.delete({ url: `/iot/realtime-data-source/delete?id=` + id })
  }
}
