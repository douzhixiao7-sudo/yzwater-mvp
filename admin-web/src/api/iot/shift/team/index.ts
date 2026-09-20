import request from '@/config/axios'

type IdType = string | number

const normalizeShiftTeamId = (id?: IdType | null) => {
  if (id === undefined || id === null) return undefined
  const normalizedId = String(id).trim()
  if (!normalizedId || normalizedId === 'undefined' || normalizedId === 'null') return undefined
  return normalizedId
}

export interface ShiftTeamMemberVO {
  userId: IdType
  userName?: string
  mobile?: string
  leader?: boolean
}

export interface ShiftTeamVO {
  id?: IdType
  teamNo?: string
  teamName?: string
  stationId?: string
  leaderUserId?: IdType
  leaderUserName?: string
  memberCount?: number
  memberUserIds?: IdType[]
  members?: ShiftTeamMemberVO[]
  remark?: string
  creator?: string
  createTime?: string | number
}

export interface ShiftTeamPageReqVO {
  pageNo: number
  pageSize: number
  stationId?: string
  teamName?: string
  leaderUserName?: string
}

export const ShiftTeamApi = {
  getPage: async (params: ShiftTeamPageReqVO) => {
    return await request.get({ url: '/iot/shift-team/page', params })
  },

  get: async (id?: IdType | null) => {
    const normalizedId = normalizeShiftTeamId(id)
    if (!normalizedId) {
      throw new Error('班组ID不能为空')
    }
    return await request.get<ShiftTeamVO>({ url: '/iot/shift-team/get', params: { id: normalizedId } })
  },

  create: async (data: ShiftTeamVO) => {
    return await request.post({ url: '/iot/shift-team/create', data })
  },

  update: async (data: ShiftTeamVO) => {
    return await request.put({ url: '/iot/shift-team/update', data })
  },

  remove: async (id?: IdType | null) => {
    const normalizedId = normalizeShiftTeamId(id)
    if (!normalizedId) {
      throw new Error('班组ID不能为空')
    }
    return await request.delete({ url: '/iot/shift-team/delete', params: { id: normalizedId } })
  },

  exportExcel: async (params: ShiftTeamPageReqVO) => {
    return await request.download({ url: '/iot/shift-team/export-excel', params })
  }
}
