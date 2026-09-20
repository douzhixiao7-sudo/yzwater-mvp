import endpointImg from '@/assets/imgs/endpoint.png'

/** 地图仓库点统一标识色（与路线色板无关） */
export const WAREHOUSE_MARKER_COLOR = '#1677ff'
export const WAREHOUSE_MARKER_HOVER_COLOR = '#4096ff'

export type WarehouseMapPoint = {
  id: string | number
  warehouseName: string
  longitude: number
  latitude: number
  /** 仓库点展示色（固定蓝色，与路线色无关） */
  routeColor: string
  routeHoverColor: string
}

export const WAREHOUSE_PICKER_ICON_SIZE = 42

export const estimateWarehousePickerIconBox = (label: string, nameExpanded = false) => {
  if (!nameExpanded) {
    return { width: WAREHOUSE_PICKER_ICON_SIZE, height: WAREHOUSE_PICKER_ICON_SIZE + 24 }
  }
  const textLen = label.trim().length
  const width = Math.min(280, Math.max(WAREHOUSE_PICKER_ICON_SIZE, textLen * 7 + 28))
  const lineCount = Math.max(1, Math.ceil(textLen / 14))
  return {
    width,
    height: WAREHOUSE_PICKER_ICON_SIZE + lineCount * 16 + 14
  }
}

/** 仓库点标识色：统一蓝色（路线仍按序号走色板） */
export const assignWarehouseRouteColor = (_warehouseId: string | number, _fallbackIndex = 0) => ({
  routeColor: WAREHOUSE_MARKER_COLOR,
  routeHoverColor: WAREHOUSE_MARKER_HOVER_COLOR
})

export type WarehousePickerMarkerOptions = {
  linked?: boolean
  linkable?: boolean
  /** 点击后展开完整仓库名（与路线终点标签同风格） */
  nameExpanded?: boolean
}

export const buildWarehousePickerMarkerHtml = (
  label: string,
  options: WarehousePickerMarkerOptions
) => {
  const text = label.trim()
  const { linked = false, linkable = false, nameExpanded = false } = options
  const stateClass = linked ? 'is-linked' : linkable ? 'is-linkable' : 'is-idle'
  const labelClass = `tianditu-warehouse-picker-marker__label${nameExpanded ? ' is-expanded' : ''}`
  return `<div class="tianditu-warehouse-picker-marker ${stateClass}${nameExpanded ? ' is-name-expanded' : ''}" style="--warehouse-route-color:${WAREHOUSE_MARKER_COLOR}">
    <img class="tianditu-warehouse-picker-marker__img" src="${endpointImg}" width="${WAREHOUSE_PICKER_ICON_SIZE}" height="${WAREHOUSE_PICKER_ICON_SIZE}" alt="" draggable="false" />
    <div class="tianditu-warehouse-picker-marker__color-bar"></div>
    ${text ? `<div class="${labelClass}">${escapeHtml(text)}</div>` : ''}
  </div>`
}

const escapeHtml = (text: string) =>
  text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
