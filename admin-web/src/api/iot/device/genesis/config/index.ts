import request from '@/config/axios'

export interface DeviceGenesisConfigVO {
  id?: number
  deviceId: number
  baseUrl: string
  username: string
  password: string
  timeout: number
  collectInterval: number
  status: number
  remark?: string
}

export const DeviceGenesisConfigApi = {
  getGenesisConfig: async (deviceId: number) => {
    return await request.get<DeviceGenesisConfigVO>({
      url: `/iot/device-genesis-config/get`,
      params: { deviceId }
    })
  },

  saveGenesisConfig: async (data: DeviceGenesisConfigVO) => {
    return await request.post({ url: `/iot/device-genesis-config/save`, data })
  }
}
