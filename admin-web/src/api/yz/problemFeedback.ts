import request from '@/config/axios'

export interface ProblemFeedbackPageReqVO {
  pageNo: number
  pageSize: number
  facilityName?: string
  facilityType?: string
  /**
   * 选择日期（按天范围），传参示例：createTime=2025-12-02,2025-12-03
   */
  createTime?: string
  // 兼容旧字段：后续可移除
  riverCode?: string
  riverName?: string
  feedbackType?: string
  status?: number | string
}

export interface ProblemFeedbackListReqVO {
  facilityName?: string
  facilityType?: string
  /**
   * 选择日期（按天范围），传参示例：createTime=2025-12-02,2025-12-03
   */
  createTime?: string[]
  // 兼容旧字段：后续可移除
  riverCode?: string
  riverName?: string
  feedbackType?: string
  status?: number | string
}

export interface ProblemFeedbackPageRespVO {
  id: string | number
  // 兼容旧字段：后续可移除
  riverCode?: string
  riverName?: string
  referenceType?: string
  referenceId?: number
  referenceName?: string
  facilityCode?: string
  facilityName?: string
  feedbackType?: string
  feedbackTypeLabel?: string
  feedbackContent?: string
  feedbackPerson?: string
  uploadedFiles?: string[]
  specificLocation?: string
  createTime?: string
  status?: number
  statusLabel?: string
  assignedPersonId?: number
  assignedPersonName?: string
  plannedCompletionTime?: string
  expedited?: number
}

export interface ProblemFeedbackStatusTaskRespVO {
  id: number
  status?: number
  statusLabel?: string
  assignedPersonId?: number
  assignedPersonName?: string
  reviewerPersonId?: number
  reviewerPersonName?: string
  reviewerPersonTime?: string
  verificationId?: number
  verificationName?: string
  verificationTime?: string
  statusDescription?: string
  statusDescriptionTime?: string
  uploadedFiles?: string[]
  problemHandleImages?: string[]
  verifyHandleImages?: string[]
  processingTime?: string
  resolutionDescription?: string
  noNeedHandle?: boolean
  verificationResult?: string
  completionTime?: string
  plannedCompletionTime?: string
  createTime?: string
}

export interface ProblemFeedbackDetailRespVO {
  id: number
  feedbackType?: string
  feedbackTypeLabel?: string
  referenceType?: string
  referenceId?: number
  referenceName?: string
  facilityCode?: string
  facilityName?: string
  // 兼容旧字段：后续可移除
  riverCode?: string
  riverName?: string
  riverChannelName?: string
  riverSectionName?: string
  feedbackContent?: string
  problemLongitude?: number | string
  problemLatitude?: number | string
  issueSpecificLocation?: string
  feedbackPerson?: string
  uploadedFiles?: string[]
  specificLocation?: string
  status?: number
  statusLabel?: string
  createTime?: string
  statusTasks?: ProblemFeedbackStatusTaskRespVO[]
}

export interface ProblemFeedbackUserSimpleRespVO {
  id: number
  nickname: string
  name?: string
  username?: string
  roleNames?: string
  headLevel?: string
  headLevelLabel?: string
}

export interface ProblemFeedbackDashboardRespVO {
  statusStat?: {
    pendingCount?: number
    processingCount?: number
    pendingVerifyCount?: number
    finishedCount?: number
  }
  typeStatList?: { feedbackType: string; feedbackTypeLabel: string; count: number }[]
  top5List?: { referenceType?: string; referenceId?: number; referenceName?: string; count: number }[]
  participation?: {
    totalFeedbackCount?: number
    feedbackPersonCount?: number
    realPersonCount?: number
    anonymousPersonCount?: number
    realNameRate?: number
  }
}

export interface ProblemFeedbackSpatialAnalysisReqVO {
  longitude: number | string
  latitude: number | string
  radiusM: number | string
}

export interface ProblemFeedbackSpatialAnalysisTypeStatRespVO {
  feedbackType?: string
  feedbackTypeLabel?: string
  count?: number
}

export interface ProblemFeedbackSpatialAnalysisStatusStatRespVO {
  pendingCount?: number
  processingCount?: number
  pendingVerifyCount?: number
  finishedCount?: number
}

export interface ProblemFeedbackSpatialAnalysisAssetRespVO {
  riverCount?: number
  reservoirCount?: number
  embankmentCount?: number
  pumpStationCount?: number
  irrigationDistrictCount?: number
  floodMaterialCount?: number
}

export interface ProblemFeedbackSpatialAnalysisChiefRespVO {
  headName?: string
  headLevel?: string
  headLevelLabel?: string
  referenceType?: string
  referenceTypeLabel?: string
  referenceName?: string
  administrativeRegion?: string[]
}

export interface ProblemFeedbackSpatialAnalysisRespVO {
  longitude?: number
  latitude?: number
  radiusM?: number
  statsTime?: string
  problemTypeStats?: ProblemFeedbackSpatialAnalysisTypeStatRespVO[]
  statusStat?: ProblemFeedbackSpatialAnalysisStatusStatRespVO
  assetStats?: ProblemFeedbackSpatialAnalysisAssetRespVO
  managementList?: ProblemFeedbackSpatialAnalysisChiefRespVO[]
}

export interface ProblemFeedbackNotifyRespVO {
  publicSent?: boolean
  publicLogId?: number
  publicReason?: string
  handlerSent?: boolean
  handlerLogId?: number
  handlerReason?: string
}

export const getProblemFeedbackPage = (params: ProblemFeedbackPageReqVO) => {
  return request.get<{ list: ProblemFeedbackPageRespVO[]; total: number }>({
    url: '/problem/feedback/page',
    params
  })
}

export const getProblemFeedbackScreenList = (params?: ProblemFeedbackListReqVO) => {
  return request.get<ProblemFeedbackPageRespVO[]>({
    url: '/screen/statistics/problem/feedback/list',
    params
  })
}

export const getProblemFeedbackList = (params?: ProblemFeedbackListReqVO) => {
  return request.get<ProblemFeedbackPageRespVO[]>({
    url: '/problem/feedback/list',
    params
  })
}

export const getProblemFeedbackDashboard = (period?: string) => {
  return request.get<ProblemFeedbackDashboardRespVO>({
    url: '/problem/feedback/dashboard',
    params: { period }
  })
}

export const getProblemFeedbackDetail = (id: string | number) => {
  return request.get<ProblemFeedbackDetailRespVO>({
    url: `/problem/feedback/${id}`
  })
}

export const getProblemFeedbackSpatialAnalysis = (data: ProblemFeedbackSpatialAnalysisReqVO) => {
  return request.post<ProblemFeedbackSpatialAnalysisRespVO>({
    url: '/problem/feedback/spatial-analysis',
    data
  })
}

export const deleteProblemFeedback = (id: string | number) => {
  return request.delete({
    url: `/problem/feedback/${id}`
  })
}

export const exportProblemFeedbackExcel = (params: ProblemFeedbackPageReqVO) => {
  return request.download({
    url: '/problem/feedback/export-excel',
    params
  })
}

export const getProblemFeedbackUsers = (feedbackId?: string | number) => {
  return request.get<ProblemFeedbackUserSimpleRespVO[]>({
    url: '/problem/feedback/users',
    params: feedbackId !== undefined && feedbackId !== null && feedbackId !== '' ? { feedbackId } : undefined
  })
}

export const auditAndAssignProblemFeedback = (data: {
  id: string | number
  assignedPersonId?: number
  plannedCompletionTime?: string
  statusDescription: string
}) => {
  return request.post({
    url: '/problem/feedback/audit-assign',
    data
  })
}

export const processProblemFeedback = (data: {
  id: string | number
  handleResult: string
  resolutionDescription: string
  uploadedFiles?: string[]
}) => {
  return request.post({
    url: '/problem/feedback/process',
    data
  })
}

export const verifyProblemFeedback = (data: {
  id: string | number
  solved: boolean
  verificationResult: string
  uploadedFiles?: string[]
}) => {
  return request.post({
    url: '/problem/feedback/verify',
    data
  })
}

// 管理员指派后发送短信（给反馈人 + 处理人）
export const notifyProblemFeedbackAssignSms = (id: string | number) => {
  return request.post<ProblemFeedbackNotifyRespVO>({
    url: '/problem/feedback/notify-assign',
    data: { id }
  })
}

// 管理员办结后发送短信（给反馈人）
export const notifyProblemFeedbackFinishSms = (id: string | number) => {
  return request.post<ProblemFeedbackNotifyRespVO>({
    url: '/problem/feedback/notify-finish',
    data: { id }
  })
}

// 管理员驳回后发送短信（给反馈人）
export const notifyProblemFeedbackRejectSms = (id: string | number) => {
  return request.post<ProblemFeedbackNotifyRespVO>({
    url: '/problem/feedback/notify-reject',
    data: { id }
  })
}

// 管理员催办超时问题发送短信（给处理人）
export const notifyProblemFeedbackOverdueSms = (id: string | number) => {
  return request.post<ProblemFeedbackNotifyRespVO>({
    url: '/problem/feedback/notify-overdue',
    data: { id }
  })
}
