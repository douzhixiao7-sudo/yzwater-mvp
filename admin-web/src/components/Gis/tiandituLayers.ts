/** 天地图 WMTS 影像底图（img）+ 影像注记（cia） */

export const TD_MAP_TOKEN_LIST = [
  '22a4277abffa39525703a9c1521729c2',
  '5793aca922ec3d4c0da703da937a0247'
] as const

/** props 默认值须从模块导出，不可引用 script setup 内局部变量 */
export const DEFAULT_TIANDITU_KEY: string = TD_MAP_TOKEN_LIST[0]

export const pickRandomTiandituKey = (): string => {
  const list = TD_MAP_TOKEN_LIST
  return list[Math.floor(Math.random() * list.length)] ?? list[0]
}

export const TIANDITU_TILE_SUBDOMAINS = ['0', '1', '2', '3', '4', '5', '6', '7'] as const

export type TiandituWmtsLayerCode = 'img' | 'cia' | 'vec' | 'cva'

const WMTS_LAYER_META: Record<TiandituWmtsLayerCode, { path: string; layer: string }> = {
  img: { path: 'img_w', layer: 'img' },
  cia: { path: 'cia_w', layer: 'cia' },
  vec: { path: 'vec_w', layer: 'vec' },
  cva: { path: 'cva_w', layer: 'cva' }
}

export const buildTiandituWmtsTileUrl = (code: TiandituWmtsLayerCode, tk: string) => {
  const { path, layer } = WMTS_LAYER_META[code]
  return (
    `https://t{s}.tianditu.gov.cn/${path}/wmts?` +
    'SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&' +
    `LAYER=${layer}&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&` +
    `TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${tk}`
  )
}

export const createTiandituImageryLayerGroup = (L: any, tk: string) => {
  const tileOptions = { subdomains: [...TIANDITU_TILE_SUBDOMAINS] }
  const imgLayer = L.tileLayer(buildTiandituWmtsTileUrl('img', tk), tileOptions)
  const ciaLayer = L.tileLayer(buildTiandituWmtsTileUrl('cia', tk), tileOptions)
  return L.layerGroup([imgLayer, ciaLayer])
}

/** 纯净影像底图（无注记） */
export const createTiandituImageryBaseLayer = (L: any, tk: string) => {
  return L.tileLayer(buildTiandituWmtsTileUrl('img', tk), { subdomains: [...TIANDITU_TILE_SUBDOMAINS] })
}

/** 天地图 WMTS 矢量底图（vec）+ 矢量注记（cva） */
export const createTiandituVectorLayerGroup = (L: any, tk: string) => {
  const tileOptions = { subdomains: [...TIANDITU_TILE_SUBDOMAINS] }
  const vecLayer = L.tileLayer(buildTiandituWmtsTileUrl('vec', tk), tileOptions)
  const cvaLayer = L.tileLayer(buildTiandituWmtsTileUrl('cva', tk), tileOptions)
  return L.layerGroup([vecLayer, cvaLayer])
}

/** 纯净矢量底图（无注记） */
export const createTiandituVectorBaseLayer = (L: any, tk: string) => {
  return L.tileLayer(buildTiandituWmtsTileUrl('vec', tk), { subdomains: [...TIANDITU_TILE_SUBDOMAINS] })
}
