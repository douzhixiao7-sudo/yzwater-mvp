import request from '@/config/axios'

export interface FxZhbPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  areaCode?: string
}

export interface FxZhbPageRespVO {
  id: string
  name: string
  addr?: string
  tel?: string
  fax?: string
  areaCode?: string
  zipCode?: string
}

export interface FxZhbMemberSaveReqVO {
  id?: string
  commandDepartmentId?: string
  name?: string
  title?: string
  tel?: string
  sort?: number
}

export interface FxZhbSaveReqVO {
  id?: string
  name: string
  addr?: string
  tel?: string
  fax?: string
  areaCode?: string
  zipCode?: string
  sort?: number
  members?: FxZhbMemberSaveReqVO[]
}

export interface FxZhbDetailRespVO {
  id: string
  name: string
  addr?: string
  tel?: string
  fax?: string
  areaCode?: string
  zipCode?: string
  sort?: number
  members?: FxZhbMemberSaveReqVO[]
}

export interface FxZhbMemberPageReqVO {
  pageNo: number
  pageSize: number
  commandDepartmentId?: string
  name?: string
  title?: string
}

export interface FxZhbMemberExportReqVO {
  commandDepartmentId?: string
  name?: string
  title?: string
}

export interface FxZhbMemberPageRespVO {
  id: string
  commandDepartmentId?: string
  name?: string
  title?: string
  tel?: string
  sort?: number
}

export const getFxZhbPage = (params: FxZhbPageReqVO) => {
  return request.get<{ list: FxZhbPageRespVO[]; total: number }>({
    url: '/fx-zhb/page',
    params
  })
}

export const getFxZhbDetail = (id: string) => {
  return request.get<FxZhbDetailRespVO>({
    url: `/fx-zhb/${id}`
  })
}

export const createFxZhb = (data: FxZhbSaveReqVO) => {
  return request.post<string>({
    url: '/fx-zhb',
    data
  })
}

export const updateFxZhb = (data: FxZhbSaveReqVO) => {
  return request.put({
    url: '/fx-zhb',
    data
  })
}

export const deleteFxZhb = (id: string) => {
  return request.delete({
    url: `/fx-zhb/${id}`
  })
}

export const exportFxZhbExcel = (params: FxZhbPageReqVO) => {
  return request.download({
    url: '/fx-zhb/export-excel',
    params
  })
}

export const getFxZhbMemberPage = (params: FxZhbMemberPageReqVO) => {
  return request.get<{ list: FxZhbMemberPageRespVO[]; total: number }>({
    url: '/fx-zhb-member/page',
    params
  })
}

export const createFxZhbMember = (data: FxZhbMemberSaveReqVO) => {
  return request.post<string>({
    url: '/fx-zhb-member',
    data
  })
}

export const updateFxZhbMember = (data: FxZhbMemberSaveReqVO) => {
  return request.put({
    url: '/fx-zhb-member',
    data
  })
}

export const deleteFxZhbMember = (id: string) => {
  return request.delete({
    url: `/fx-zhb-member/${id}`
  })
}

export const exportFxZhbMemberExcel = (params: FxZhbMemberExportReqVO) => {
  return request.download({
    url: '/fx-zhb-member/export-excel',
    params
  })
}
