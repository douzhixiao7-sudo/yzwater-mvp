import request from '@/config/axios'

// IoT 产品 VO
export interface ProductVO {
  id: string | number
  name: string
  productKey: string
  protocolId: number
  categoryId: number
  categoryName?: string
  icon: string
  picUrl: string
  description: string
  status: number
  deviceType: number
  locationType: number
  netType: number
  protocolType: string
  codecType: string
  deviceCount: number
  createTime: Date
}

// IOT 产品设备类型枚举类：0 直连设备，1 网关子设备，2 网关设备
export enum DeviceTypeEnum {
  DEVICE = 0,
  GATEWAY_SUB = 1,
  GATEWAY = 2
}

// IOT 产品定位类型枚举类：1 IP 定位，2 定位模块定位，3 手动定位
export enum LocationTypeEnum {
  IP = 1,
  MODULE = 2,
  MANUAL = 3
}

// IOT 协议类型
export enum ProtocolTypeEnum {
  HTTP = 'http',
  TCP = 'tcp',
  UDP = 'udp',
  WEBSOCKET = 'websocket',
  MQTT = 'mqtt',
  EMQX = 'emqx',
  COAP = 'coap',
  GENESIS64_HTTP = 'genesis64_http',
  MQTT_SOURCE = 'mqtt_source'
}

// IOT 序列化类型（沿用原 codecType 字段）
export enum CodecTypeEnum {
  ALINK = 'Alink'
}

// IoT 产品 API
export const ProductApi = {
  getProductPage: async (params: any) => {
    return await request.get({ url: `/iot/product/page`, params })
  },

  getProduct: async (id: string | number) => {
    return await request.get({ url: `/iot/product/get?id=` + id })
  },

  createProduct: async (data: ProductVO) => {
    return await request.post({ url: `/iot/product/create`, data })
  },

  updateProduct: async (data: ProductVO) => {
    return await request.put({ url: `/iot/product/update`, data })
  },

  deleteProduct: async (id: string | number) => {
    return await request.delete({ url: `/iot/product/delete?id=` + id })
  },

  exportProduct: async (params) => {
    return await request.download({ url: `/iot/product/export-excel`, params })
  },

  updateProductStatus: async (id: string | number, status: number) => {
    return await request.put({ url: `/iot/product/update-status?id=` + id + `&status=` + status })
  },

  publishAllUnpublished: async () => {
    return await request.put({ url: `/iot/product/publish-all` })
  },

  getSimpleProductList() {
    return request.get({ url: '/iot/product/simple-list' })
  },

  getProductByKey: async (productKey: string) => {
    return await request.get({ url: `/iot/product/get-by-key`, params: { productKey } })
  }
}
