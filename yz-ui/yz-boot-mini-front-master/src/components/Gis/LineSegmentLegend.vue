<template>
  <div class="line-segment-legend" aria-label="地图线段图例">
    <span class="line-segment-legend__title">图例</span>
    <div class="line-segment-legend__items">
      <template v-if="routeCount > 1">
        <span v-for="index in routeCount" :key="`route-${index}`" class="line-segment-legend__item">
          <i
            class="line-segment-legend__line"
            :style="{ borderTopColor: resolveRouteColor(index - 1) }"
          ></i>
          {{ resolveRouteLabel(index - 1) }}
        </span>
      </template>
      <span v-else class="line-segment-legend__item">
        <i
          class="line-segment-legend__line is-normal"
          :style="routeCount === 1 ? { borderTopColor: resolveRouteColor(0) } : undefined"
        ></i>
        {{ routeCount === 1 ? resolveRouteLabel(0) : '物资路线' }}
      </span>
      <span v-if="showRisk" class="line-segment-legend__item">
        <i class="line-segment-legend__line is-risk"></i>
        隐患段
      </span>
      <span v-if="showEndpoint" class="line-segment-legend__item">
        <img class="line-segment-legend__marker" :src="startpointImg" alt="" />
        {{ startAsHazardPoint ? '隐患点' : '起点' }}
      </span>
      <span v-if="showEndpoint" class="line-segment-legend__item">
        <img class="line-segment-legend__marker" :src="endpointImg" alt="" />
        {{ startAsHazardPoint ? '仓库' : '终点' }}
      </span>
      <span v-if="showArrow" class="line-segment-legend__item">
        <svg
          class="line-segment-legend__arrow"
          viewBox="0 0 24 24"
          width="14"
          height="14"
          aria-hidden="true"
        >
          <path
            d="M11 3 L17 19 L11 15 L5 19 Z"
            fill="#ffffff"
            stroke="#1677ff"
            stroke-width="1.6"
            stroke-linejoin="round"
          />
        </svg>
        行进方向
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import startpointImg from '@/assets/imgs/startpoint.png'
import endpointImg from '@/assets/imgs/endpoint.png'
import { getRouteLineColor } from './tiandituLineVertexEditor'

const props = withDefaults(
  defineProps<{
    showRisk?: boolean
    showEndpoint?: boolean
    showArrow?: boolean
    startAsHazardPoint?: boolean
    routeCount?: number
    routeColors?: string[]
    routeLabels?: string[]
  }>(),
  {
    showRisk: true,
    showEndpoint: true,
    showArrow: false,
    startAsHazardPoint: false,
    routeCount: 0,
    routeColors: () => [],
    routeLabels: () => []
  }
)

const resolveRouteColor = (index: number) => {
  const custom = props.routeColors?.[index]?.trim()
  return custom || getRouteLineColor(index)
}

const resolveRouteLabel = (index: number) => {
  const custom = props.routeLabels?.[index]?.trim()
  return custom || `路线 ${index + 1}`
}
</script>

<style scoped>
.line-segment-legend {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px 12px;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #edf4ff 0%, #fafafa 55%, #f4ffe8 100%);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 1px 4px rgb(0 0 0 / 6%);
  pointer-events: none;
  user-select: none;
}

.line-segment-legend__title {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.line-segment-legend__items {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px 14px;
}

.line-segment-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  line-height: 1.3;
  white-space: nowrap;
}

.line-segment-legend__line {
  display: inline-block;
  width: 18px;
  height: 0;
  flex-shrink: 0;
  border-top: 5px solid #1677ff;
  border-radius: 2px;
}

.line-segment-legend__line.is-normal {
  border-top-color: #1677ff;
}

.line-segment-legend__line.is-risk {
  border-top-color: #fadb14;
}

.line-segment-legend__marker {
  display: block;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  object-fit: contain;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 18%));
}

.line-segment-legend__arrow {
  flex-shrink: 0;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 18%));
}
</style>
