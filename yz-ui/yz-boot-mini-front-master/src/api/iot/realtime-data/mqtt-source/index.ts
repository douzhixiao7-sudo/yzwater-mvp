import request from '@/config/axios'

export interface MqttSourceSimpleVO {
  id: number
  name: string
  code: string
  brokerHost: string
  brokerPort: number
  enabled: boolean
}

export interface MqttSourceVO extends MqttSourceSimpleVO {
  username?: string
  password?: string
  clientId?: string
  qos?: number
  cleanSession?: boolean
  keepAliveIntervalSeconds?: number
  connectTimeoutSeconds?: number
  reconnectDelayMs?: number
  sslEnabled?: boolean
  remark?: string
}

export const MqttSourceApi = {
  getSimpleMqttSourceList: async (enabled?: boolean) => {
    return await request.get<MqttSourceSimpleVO[]>({
      url: `/iot/mqtt-source/simple-list`,
      params: { enabled }
    })
  },

  getMqttSourcePage: async (params: any) => {
    return await request.get({ url: `/iot/mqtt-source/page`, params })
  },

  getMqttSource: async (id: number) => {
    return await request.get<MqttSourceVO>({ url: `/iot/mqtt-source/get?id=${id}` })
  },

  createMqttSource: async (data: MqttSourceVO) => {
    return await request.post({ url: `/iot/mqtt-source/create`, data })
  },

  updateMqttSource: async (data: MqttSourceVO) => {
    return await request.put({ url: `/iot/mqtt-source/update`, data })
  },

  deleteMqttSource: async (id: number) => {
    return await request.delete({ url: `/iot/mqtt-source/delete?id=${id}` })
  }
}
