import request from '@/config/axios'

export interface FxYaglListRespVO {
  id: string
  name: string
  files?: string[]
  sort?: number
}

export interface FxYaglSaveReqVO {
  id?: string
  name: string
  files: string[]
  sort?: number
}

export const getFxYaglList = () => {
  return request.get<FxYaglListRespVO[]>({
    url: '/fx-yagl/list'
  })
}

export const getFxYaglDetail = (id: string) => {
  return request.get<FxYaglSaveReqVO>({
    url: `/fx-yagl/${id}`
  })
}

export const createFxYagl = (data: FxYaglSaveReqVO) => {
  return request.post<string>({
    url: '/fx-yagl',
    data
  })
}

export const updateFxYagl = (data: FxYaglSaveReqVO) => {
  return request.put({
    url: '/fx-yagl',
    data
  })
}

export const deleteFxYagl = (id: string) => {
  return request.delete({
    url: `/fx-yagl/${id}`
  })
}
