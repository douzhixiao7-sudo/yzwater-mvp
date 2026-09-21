import request from '@/config/axios'
import { ThingModelData } from '@/api/iot/thingmodel'

export interface DeviceMqttMappingVO {
  id?: number
  deviceId: number
  thingModelId?: number
  identifier?: string
  name?: string
  payloadKey: string
  valueType?: string
  sort?: number
  status: number
  remark?: string
  createTime?: Date
}

export interface DeviceMqttMappingPageReqVO {
  pageNo: number
  pageSize: number
  deviceId: number
  name?: string
  identifier?: string
  payloadKey?: string
}

export interface DeviceMqttMappingListReqVO {
  deviceId: number
  name?: string
  identifier?: string
  payloadKey?: string
}

export const DeviceMqttMappingApi = {
  getMqttMappingPage: async (params: DeviceMqttMappingPageReqVO) => {
    return await request.get({ url: `/iot/device-mqtt-mapping/page`, params })
  },

  getMqttMappingList: async (params: DeviceMqttMappingListReqVO) => {
    return await request.get<ThingModelData[]>({ url: `/iot/device-mqtt-mapping/list`, params })
  },

  getMqttMapping: async (id: number) => {
    return await request.get<DeviceMqttMappingVO>({
      url: `/iot/device-mqtt-mapping/get?id=${id}`
    })
  },

  createMqttMapping: async (data: DeviceMqttMappingVO) => {
    return await request.post({ url: `/iot/device-mqtt-mapping/create`, data })
  },

  updateMqttMapping: async (data: DeviceMqttMappingVO) => {
    return await request.put({ url: `/iot/device-mqtt-mapping/update`, data })
  },

  deleteMqttMapping: async (id: number) => {
    return await request.delete({ url: `/iot/device-mqtt-mapping/delete?id=${id}` })
  }
}
