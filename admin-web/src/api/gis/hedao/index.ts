import request from '@/config/axios'

export interface GisYzHedaoDuanVO {
  id?: number
  hdfzmc?: string
  qd?: string
  zd?: string
  qdjd?: number
  qdwd?: number
  zdjd?: number
  zdwd?: number
  hdgk?: string
}

export interface GisYzHedaoRespVO {
  id: number
  hdbm: string
  hdmc: string
  hljb?: string
  hdlx?: string[]
  stlx?: string
  szly?: string
  kjlb?: string
  hdcdKm?: number
  lymjKm2?: number
  hywz?: string
  hkwz?: string
  zxjd?: number
  zxwd?: number
  hkjd?: number
  hkwd?: number
  hyjd?: number
  hywd?: number
  ljdq?: string
  startEnd?: string
  gldw?: string
  dfdj?: string
  dfcdKm?: number
  fhbz?: string
  njjll?: number
  lszgsw?: number
  zgswsj?: string
  lszdsw?: number
  zdswsj?: string
  fysx?: string
  hlgs?: string
  xz?: string
  hdhfSl?: number
  remark?: string
  segments?: GisYzHedaoDuanVO[]
  hezhangList?: GisYzHedaoHezhangVO[]
}

export interface GisYzHedaoPageReqVO extends PageParam {
  hdbm?: string
  hdmc?: string
  hljb?: string
  stlx?: string
}

export interface GisYzHedaoSaveReqVO {
  id?: number
  hdbm: string
  hdmc: string
  hljb?: string
  hdlx?: string[]
  stlx?: string
  szly?: string
  kjlb?: string
  hdcdKm?: number
  lymjKm2?: number
  zxjd?: number
  zxwd?: number
  hkjd?: number
  hkwd?: number
  hyjd?: number
  hywd?: number
  ljdq?: string
  hywz?: string
  hkwz?: string
  dfdj?: string
  dfcdKm?: number
  fhbz?: string
  njjll?: number
  lszgsw?: number
  zgswsj?: string
  lszdsw?: number
  zdswsj?: string
  fysx?: string
  hlgs?: string
  xz?: string
  hdhfSl?: number
  gldw?: string
  remark?: string
  segments?: GisYzHedaoDuanVO[]
  hezhangList?: GisYzHedaoHezhangVO[]
}

export interface GisYzHedaoHezhangVO {
  id?: number
  hedaoId?: number
  heduanId?: number
  hdfzmc?: string
  hdfzEwm?: string
  sjhzXm?: string
  sjhzZw?: string
  sjhzGzdw?: string
  sjhzLxdh?: string
  shjhzXm?: string
  shjhzZw?: string
  shjhzGzdw?: string
  shjhzLxdh?: string
  xjhzXm?: string
  xjhzZw?: string
  xjhzGzdw?: string
  xjhzLxdh?: string
  xzhzXm?: string
  xzhzZw?: string
  xzhzGzdw?: string
  xzhzLxdh?: string
  cjhzhdglXm?: string
  cjhzhdglZw?: string
  cjhzhdglGzdw?: string
  cjhzhdglLxdh?: string
  hzzr?: string
  jddw?: string
  jddh?: string
  remark?: string
}

// 分页查询河道
export const getHedaoPage = (params: GisYzHedaoPageReqVO) => {
  return request.get<PageResult<GisYzHedaoRespVO[]>>({
    url: '/hedao/page',
    params
  })
}

// 新增河道
export const createHedao = (data: GisYzHedaoSaveReqVO) => {
  return request.post<number>({
    url: '/hedao/create',
    data
  })
}

// 修改河道
export const updateHedao = (data: GisYzHedaoSaveReqVO) => {
  return request.put<boolean>({
    url: '/hedao/update',
    data
  })
}

// 删除河道
export const deleteHedao = (id: number) => {
  return request.delete<boolean>({
    url: '/hedao/delete',
    params: { id }
  })
}

// 查询详情
export const getHedao = (id: number) => {
  return request.get<GisYzHedaoRespVO>({
    url: '/hedao/get',
    params: { id }
  })
}
