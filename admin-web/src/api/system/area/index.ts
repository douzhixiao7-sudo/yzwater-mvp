import request from '@/config/axios'

export interface AreaNodeRespVO {
  id: number
  name: string
  /** 类型（与 system_area.type 一致，可能不存在于旧接口返回） */
  type?: number
  children?: AreaNodeRespVO[]
}

export interface AreaRespVO {
  id: number
  parentId?: number
  name: string
  type?: number
  sort?: number
  gemo?: string
  gemoGeoJson?: string
}

export interface AreaSaveReqVO {
  id: number
  parentId?: number
  name: string
  type?: number
  sort?: number
  gemo?: string
}

// 获得地区树（管理后台：默认返回 321081 下级节点）
export const getAreaTree = async () => {
  return await request.get<AreaNodeRespVO[]>({ url: '/system/area/tree' })
}

// 查看行政区划详情
export const getArea = async (id: number) => {
  return await request.get<AreaRespVO>({ url: '/system/area/get', params: { id } })
}

// 新增行政区划
export const createArea = async (data: AreaSaveReqVO) => {
  return await request.post<number>({ url: '/system/area/create', data })
}

// 修改行政区划
export const updateArea = async (data: AreaSaveReqVO) => {
  return await request.put<boolean>({ url: '/system/area/update', data })
}

// 删除行政区划
export const deleteArea = async (id: number) => {
  return await request.delete<boolean>({ url: '/system/area/delete', params: { id } })
}

// 获得 IP 对应的地区名（保留原有能力）
export const getAreaByIp = async (ip: string) => {
  return await request.get({ url: '/system/area/get-by-ip', params: { ip } })
}
