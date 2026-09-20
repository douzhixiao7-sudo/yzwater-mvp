import request from '@/config/axios'
import { ThingModelData } from '@/api/iot/thingmodel'

export interface DeviceGenesisPointVO {
  id?: number
  deviceId: number
  thingModelId?: number
  identifier?: string
  name?: string
  pointName: string
  sort?: number
  status: number
}

export interface DeviceGenesisPointPageReqVO {
  pageNo: number
  pageSize: number
  deviceId: number
  name?: string
  identifier?: string
  pointName?: string
}

export interface DeviceGenesisPointListReqVO {
  deviceId: number
  name?: string
  identifier?: string
  pointName?: string
}

export const DeviceGenesisPointApi = {
  getGenesisPointPage: async (params: DeviceGenesisPointPageReqVO) => {
    return await request.get({ url: `/iot/device-genesis-point/page`, params })
  },

  getGenesisPointList: async (params: DeviceGenesisPointListReqVO) => {
    return await request.get<ThingModelData[]>({ url: `/iot/device-genesis-point/list`, params })
  },

  getGenesisPoint: async (id: number) => {
    return await request.get<DeviceGenesisPointVO>({
      url: `/iot/device-genesis-point/get?id=${id}`
    })
  },

  createGenesisPoint: async (data: DeviceGenesisPointVO) => {
    return await request.post({ url: `/iot/device-genesis-point/create`, data })
  },

  updateGenesisPoint: async (data: DeviceGenesisPointVO) => {
    return await request.put({ url: `/iot/device-genesis-point/update`, data })
  },

  deleteGenesisPoint: async (id: number) => {
    return await request.delete({ url: `/iot/device-genesis-point/delete?id=${id}` })
  }
}
