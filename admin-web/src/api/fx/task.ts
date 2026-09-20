import request from '@/config/axios'

/** 0-待处理 1-已解决 */
export const FX_TASK_STATUS = {
  PENDING: 0,
  RESOLVED: 1
} as const

export type FxTaskStatus = (typeof FX_TASK_STATUS)[keyof typeof FX_TASK_STATUS]

export interface FxTaskListRespVO {
  id: string
  code?: string
  name: string
  addr: string
  riverChannelId?: string
  riverChannelName?: string
  level: string
  content: string
  counterMeasures?: string
  files?: string[]
  sort?: number
  geometryGeoJson?: string
  /** 处理状态：0-待处理 1-已解决 */
  status?: FxTaskStatus
  /** 解决说明 */
  resolveRemark?: string
  /** 解决时间（ISO 字符串） */
  resolveTime?: string
  /** 处置附件 */
  resolveFiles?: string[]
}

export interface FxTaskResolveReqVO {
  id: string
  resolveRemark: string
  resolveFiles?: string[]
}

export interface FxTaskSaveReqVO {
  id?: string
  code?: string
  name: string
  addr: string
  riverChannelId?: string
  riverChannelName?: string
  level: string
  content: string
  counterMeasures?: string
  files: string[]
  sort?: number
  geometryGeoJson?: string
}

export const getFxTaskList = () => {
  return request.get<FxTaskListRespVO[]>({
    url: '/fx-task/list'
  })
}

export const getFxTaskDetail = (id: string) => {
  return request.get<FxTaskSaveReqVO>({
    url: `/fx-task/${id}`
  })
}

export const createFxTask = (data: FxTaskSaveReqVO) => {
  return request.post<string>({
    url: '/fx-task',
    data
  })
}

export const updateFxTask = (data: FxTaskSaveReqVO) => {
  return request.put({
    url: '/fx-task',
    data
  })
}

export const deleteFxTask = (id: string) => {
  return request.delete({
    url: `/fx-task/${id}`
  })
}

/** 标记隐患点为已解决（后端接口待对接） */
export const resolveFxTask = (data: FxTaskResolveReqVO) => {
  return request.put({
    url: '/fx-task/resolve',
    data
  })
}
