import { getConfigKey } from '@/api/infra/config'
import { pickRandomTiandituKey, TD_MAP_TOKEN_LIST } from './tiandituLayers'

const TIANDITU_CONFIG_KEY = 'map-key'

export { TD_MAP_TOKEN_LIST, pickRandomTiandituKey }

/**
 * 获取天地图 key（优先后端配置 map-key，否则从 tdMapTokenList 随机选取）。
 *
 * 后端接口：/infra/config/get-value-by-key?key=map-key
 */
export const getTiandituKey = async (): Promise<string> => {
  try {
    const res: any = await getConfigKey(TIANDITU_CONFIG_KEY)
    const value = typeof res?.data === 'string' ? res.data.trim() : ''
    if (value) return value
  } catch {
    // 忽略异常，从 token 池随机选取
  }
  return pickRandomTiandituKey()
}
