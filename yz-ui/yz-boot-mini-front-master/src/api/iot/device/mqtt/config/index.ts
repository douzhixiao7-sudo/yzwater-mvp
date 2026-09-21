import request from '@/config/axios'

export interface DeviceMqttConfigVO {
  id?: number
  deviceId: number
  sourceId?: number
  topic: string
  payloadMode: string
  reportTimeKey?: string
  reportTimeFormat?: string
  status: number
  remark?: string
}

export const DeviceMqttConfigApi = {
  getMqttConfig: async (deviceId: number) => {
    return await request.get<DeviceMqttConfigVO>({
      url: `/iot/device-mqtt-config/get`,
      params: { deviceId }
    })
  },

  saveMqttConfig: async (data: DeviceMqttConfigVO) => {
    return await request.post({ url: `/iot/device-mqtt-config/save`, data })
  }
}
