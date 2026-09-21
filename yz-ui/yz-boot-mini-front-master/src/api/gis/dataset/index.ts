import request from '@/config/axios'

export interface GisDatasetSimpleRespVO {
  id: number
  datasetCode: string
  name: string
  geomType: string
  srid: number
  layerGroup: string
  keyProps: string
  styleConfig: Record<string, any> | null
  remark: string
}

export interface GisDatasetFeatureRespVO {
  id: string
  datasetCode: string
  datasetName: string
  featureCode: string
  geomType: string
  srid: number
  geoJson: string
  properties: Record<string, any> | null
}

export interface GisDatasetFeatureListRespVO {
  dataset: GisDatasetSimpleRespVO
  features: GisDatasetFeatureRespVO[]
}

export interface GisDatasetFeatureUpdateReqVO {
  featureCode?: string
  properties: Record<string, any>
}

export interface GisDatasetFeatureSaveReqVO {
  geoJson: string
  geomType?: string
  srid?: number
  featureCode?: string
  properties?: Record<string, any>
}

export type GisDatasetFeatureExportFormat = 'GEOJSON' | 'SHP'

export interface GisDatasetFeatureExportReqVO {
  featureIds: string[]
  format: GisDatasetFeatureExportFormat
  targetSrid?: number
}

export interface GisDatasetCreateReqVO {
  datasetCode: string
  name: string
  geomType: string
  srid?: number
  layerGroup?: string
  keyProps?: string
  styleConfig?: Record<string, any>
  remark?: string
}

export const fetchGisDatasets = () => {
  return request.get<GisDatasetSimpleRespVO[]>({ url: '/dataset/list' })
}

export const createGisDataset = (data: GisDatasetCreateReqVO) => {
  return request.post<GisDatasetSimpleRespVO>({
    url: '/dataset',
    data
  })
}

export const deleteGisDataset = (datasetCode: string) => {
  return request.delete<boolean>({
    url: `/dataset/${datasetCode}`
  })
}

export const fetchGisDatasetFeatures = (datasetCode: string) => {
  return request.get<GisDatasetFeatureListRespVO>({ url: `/dataset/${datasetCode}/features` })
}

export const updateGisDatasetFeature = (
  datasetCode: string,
  featureId: string,
  data: GisDatasetFeatureUpdateReqVO
) => {
  return request.put<boolean>({
    url: `/dataset/${datasetCode}/features/${featureId}`,
    data
  })
}

export const downloadGisDatasetFeatures = (datasetCode: string, data: GisDatasetFeatureExportReqVO) => {
  return request.postOriginal({
    url: `/dataset/${datasetCode}/features/export`,
    data,
    responseType: 'blob'
  })
}

export const createGisDatasetFeature = (datasetCode: string, data: GisDatasetFeatureSaveReqVO) => {
  return request.post<GisDatasetFeatureRespVO>({
    url: `/dataset/${datasetCode}/features`,
    data
  })
}

export const updateGisDatasetFeatureGeometry = (
  datasetCode: string,
  featureId: string,
  data: GisDatasetFeatureSaveReqVO
) => {
  return request.put<GisDatasetFeatureRespVO>({
    url: `/dataset/${datasetCode}/features/${featureId}/geometry`,
    data
  })
}

export const deleteGisDatasetFeature = (datasetCode: string, featureId: string) => {
  return request.delete<boolean>({
    url: `/dataset/${datasetCode}/features/${featureId}`
  })
}
