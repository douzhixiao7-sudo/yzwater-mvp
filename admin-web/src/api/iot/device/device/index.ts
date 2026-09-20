import request from '@/config/axios'

// IoT 设备 VO
export interface DeviceVO {
  id: string | number // 设备 ID
  deviceName?: string // 设备标识（系统内部）
  nickname?: string // 设备名称
  serialNumber?: string // 设备编码
  stationId?: string // 所属站点
  productId?: string | number // 产品编号
  productKey?: string // 产品 Key
  deviceType?: number // 设备类型
  gatewayId?: number // 网关设备 ID
  state?: number // 设备状态
  useStatus?: string // 使用状态
  equipmentModel?: string // 设备型号
  manufacturer?: string // 生产厂家
  installDate?: string | Date // 安装日期
  lastMaintainTime?: string | Date // 上次养护时间
  maintainCycleDays?: number // 养护周期（天）
  ownerUserId?: number // 负责人用户 ID
  ownerName?: string // 负责人姓名
  ownerPhone?: string // 负责人联系方式
  deptId?: number // 所属部门 ID
  remark?: string // 备注
  qrCode?: string // 设备二维码
  picUrl?: string[] // 设备图片（多张）
  groupIds?: number[] // 设备分组 ID 列表
  config?: string // 设备参数配置
  locationType?: number // 定位类型
  latitude?: number // 纬度
  longitude?: number // 经度
  locationId?: string | number // 设备位置编号
  address?: string // 设备位置
  onlineTime?: Date | string // 最后上线时间
  offlineTime?: Date | string // 最后离线时间
  activeTime?: Date | string // 设备激活时间
  createTime?: Date | string // 创建时间
  ip?: string // 设备 IP 地址
  deviceSecret?: string // 设备密钥
  authType?: string // 认证类型
}

export interface IotDevicePropertyDetailRespVO {
  identifier: string // 属性标识符
  value: string // 最新值
  updateTime: Date // 更新时间
  name: string // 属性名称
  description?: string // 属性备注
  tagName?: string // 标签名称
  dataType: string // 数据类型
  dataSpecs: any // 数据定义
  dataSpecsList: any[] // 数据定义列表
}

// IoT 设备属性 VO
export interface IotDevicePropertyRespVO {
  identifier: string // 属性标识符
  value: string // 最新值
  updateTime: Date // 更新时间
}

export interface IotDevicePropertyTagSaveItem {
  identifier: string // 属性标识符
  tagName: string // 标签名称
}

export interface IotDevicePropertyTagSaveReqVO {
  deviceId: string | number // 设备编号
  items: IotDevicePropertyTagSaveItem[] // 标签列表
}

// TODO @芋艿：调整到 constants
// IoT 设备状态枚举
export enum DeviceStateEnum {
  INACTIVE = 0, // 未激活
  ONLINE = 1, // 在线
  OFFLINE = 2 // 离线
}

// 设备认证参数 VO
export interface IotDeviceAuthInfoVO {
  clientId: string // 客户端 ID
  username: string // 用户名
  password: string // 密码
}

// IoT 设备发送消息 Request VO
export interface IotDeviceMessageSendReqVO {
  deviceId: string | number // 设备编号
  method: string // 请求方法
  params?: any // 请求参数
}

// 设备 API
export const DeviceApi = {
  // 查询设备分页
  getDevicePage: async (params: any) => {
    return await request.get({ url: `/iot/device/page`, params })
  },

  // 查询设备详情
  getDevice: async (id: string | number) => {
    return await request.get({ url: `/iot/device/get?id=` + id })
  },

  // 新增设备
  createDevice: async (data: DeviceVO) => {
    return await request.post({ url: `/iot/device/create`, data })
  },

  // 修改设备
  updateDevice: async (data: DeviceVO) => {
    return await request.put({ url: `/iot/device/update`, data })
  },

  // 修改设备分组
  updateDeviceGroup: async (data: { ids: Array<string | number>; groupIds: number[] }) => {
    return await request.put({ url: `/iot/device/update-group`, data })
  },

  // 删除单个设备
  deleteDevice: async (id: string | number) => {
    return await request.delete({ url: `/iot/device/delete?id=` + id })
  },

  // 删除多个设备
  deleteDeviceList: async (ids: Array<string | number>) => {
    return await request.delete({ url: `/iot/device/delete-list`, params: { ids: ids.join(',') } })
  },

  // 导出设备
  exportDeviceExcel: async (params: any) => {
    return await request.download({ url: `/iot/device/export-excel`, params })
  },

  // 获取设备数量
  getDeviceCount: async (productId: string | number) => {
    return await request.get({ url: `/iot/device/count?productId=` + productId })
  },

  // 获取设备的精简信息列表
  getSimpleDeviceList: async (deviceType?: number, productId?: string | number) => {
    return await request.get({ url: `/iot/device/simple-list?`, params: { deviceType, productId } })
  },

  // 获取设备列表（不分页）
  getDeviceList: async (params?: any) => {
    return await request.get({ url: `/iot/device/list`, params })
  },

  // 根据产品编号，获取设备的精简信息列表
  getDeviceListByProductId: async (productId: string | number) => {
    return await request.get({ url: `/iot/device/simple-list?`, params: { productId } })
  },

  // 获取导入模板
  importDeviceTemplate: async () => {
    return await request.download({ url: `/iot/device/get-import-template` })
  },

  // 获取设备属性最新数据
  getLatestDeviceProperties: async (params: any) => {
    return await request.get({ url: `/iot/device/property/get-latest`, params })
  },

  // 获取设备属性历史数据
  getHistoryDevicePropertyList: async (params: any) => {
    return await request.get({ url: `/iot/device/property/history-list`, params })
  },

  // 批量保存设备属性标签
  saveDevicePropertyTags: async (data: IotDevicePropertyTagSaveReqVO) => {
    return await request.post({ url: `/iot/device/property/tag/save-batch`, data })
  },

  // 获取设备认证信息
  getDeviceAuthInfo: async (id: string | number) => {
    return await request.get({ url: `/iot/device/get-auth-info`, params: { id } })
  },

  // 查询设备消息分页
  getDeviceMessagePage: async (params: any) => {
    return await request.get({ url: `/iot/device/message/page`, params })
  },

  // 查询设备消息配对分页
  getDeviceMessagePairPage: async (params: any) => {
    return await request.get({ url: `/iot/device/message/pair-page`, params })
  },

  // 发送设备消息
  sendDeviceMessage: async (params: IotDeviceMessageSendReqVO) => {
    return await request.post({ url: `/iot/device/message/send`, data: params })
  }
}
