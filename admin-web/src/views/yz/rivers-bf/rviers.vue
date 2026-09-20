<template>
  <div class="river-page">
      <ContentWrap class="query-wrap">
        <el-form
          class="query-form -mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="80px"
        >
          <el-form-item label="河道名称" prop="riverName">
            <el-input
              v-model="queryParams.riverName"
              placeholder="请输入河道名称关键词"
              clearable
              @keyup.enter="handleSearch"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="河道级别" prop="riverLevel">
            <el-select
              v-model="queryParams.riverLevel"
              multiple
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="2"
              placeholder="请选择河道级别"
              clearable
              class="!w-240px"
            >
              <el-option v-for="item in riverLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="生态类型" prop="ecologyType">
            <el-select v-model="queryParams.ecologyType" placeholder="请选择生态类型" clearable class="!w-240px">
              <el-option v-for="item in ecologyOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item
            label="省级骨干河道"
            prop="isProvincialBackbone"
            label-width="140px"
            class="provincial-backbone-item"
          >
            <el-checkbox v-model="queryProvincialBackboneChecked" class="query-single-checkbox" />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button type="primary" plain @click="openCreateDialog">
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button type="success" plain @click="handleExport">
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
            <el-button type="warning" plain @click="openImportDialog">
              <Icon icon="ep:upload" class="mr-5px" /> 导入
            </el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>

      <!-- 列表 -->
      <ContentWrap class="table-wrap">
        <el-table class="river-table" v-loading="tableLoading" :data="tableData">
          <el-table-column type="index" label="序号" width="70" align="center" />

          <el-table-column label="河道名称" align="center" min-width="160" prop="riverName" show-overflow-tooltip />
          <el-table-column label="河道级别" align="center" min-width="120">
            <template #default="{ row }">
              {{ row.riverLevelLabel || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="河道功能" align="center" min-width="140">
            <template #default="{ row }">
              {{ row.riverTypeLabel || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="所在流域" align="center" min-width="140">
            <template #default="{ row }">
              {{ row.basinTypeLabel || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="startEndLocation" label="起点/终点" align="center" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.startEndLocation || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="lengthKm" label="河道长度(km)" align="center" min-width="140">
            <template #default="{ row }">
              {{ formatNumber(row.lengthKm) }}
            </template>
          </el-table-column>
          <el-table-column prop="catchmentKm2" label="流域面积(km²)" align="center" min-width="140">
            <template #default="{ row }">
              {{ formatNumber(row.catchmentKm2) }}
            </template>
          </el-table-column>
          <!--
          <el-table-column prop="managementUnit" label="管理单位" align="center" min-width="200" show-overflow-tooltip />
          -->
          <el-table-column label="操作" align="center" width="300" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="handleChiefOverview(row)">河长</el-button>
              <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
              <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="fetchTable"
        />
      </ContentWrap>

    <el-drawer
      v-model="chiefDrawerVisible"
      title="河长信息"
      size="560px"
      direction="rtl"
      class="river-chief-drawer"
      destroy-on-close
    >
      <div v-loading="chiefDrawerLoading" class="chief-overview">
        <div class="chief-overview__hero">
          <div class="chief-overview__eyebrow">当前有效河长</div>
          <div class="chief-overview__title">{{ chiefOverviewData?.riverName || chiefDrawerRiverName || '-' }}</div>
          <div class="chief-overview__stats">
            <div class="chief-overview__stat">
              <span class="chief-overview__stat-value">{{ chiefOverviewData?.totalCount || 0 }}</span>
              <span class="chief-overview__stat-label">总人数</span>
            </div>
            <div class="chief-overview__stat">
              <span class="chief-overview__stat-value">{{ chiefOverviewData?.riverChiefs?.length || 0 }}</span>
              <span class="chief-overview__stat-label">河道直属</span>
            </div>
            <div class="chief-overview__stat">
              <span class="chief-overview__stat-value">{{ chiefOverviewData?.sectionChiefGroups?.length || 0 }}</span>
              <span class="chief-overview__stat-label">涉及河段</span>
            </div>
          </div>
        </div>

        <template v-if="hasChiefOverviewData">
          <section class="chief-block">
            <div class="chief-block__header">
              <div class="chief-block__title">
                <span class="chief-block__badge chief-block__badge--river">河道</span>
                <span>河道直属河长</span>
              </div>
              <span class="chief-block__count">{{ chiefOverviewData?.riverChiefs?.length || 0 }} 人</span>
            </div>
            <div v-if="chiefOverviewData?.riverChiefs?.length" class="chief-grid">
              <article v-for="chief in chiefOverviewData?.riverChiefs" :key="`river-${chief.id || chief.headName}`" class="chief-card">
                <div class="chief-card__top">
                  <div>
                    <div class="chief-card__name">{{ chief.headName || '-' }}</div>
                    <div class="chief-card__scope">归属：当前河道</div>
                  </div>
                  <el-tag type="primary" effect="plain" round>
                    {{ chief.headLevelLabel || resolveHeadLevelLabel(chief.headLevel) || '未标注' }}
                  </el-tag>
                </div>
                <div class="chief-card__meta">
                  <span>职务</span>
                  <span>{{ chief.headPosition || '-' }}</span>
                </div>
              </article>
            </div>
            <el-empty v-else description="当前河道暂无直属有效河长" :image-size="76" />
          </section>

          <section class="chief-block">
            <div class="chief-block__header">
              <div class="chief-block__title">
                <span class="chief-block__badge chief-block__badge--section">河段</span>
                <span>河段河长</span>
              </div>
              <span class="chief-block__count">
                {{ chiefOverviewData?.sectionChiefGroups?.reduce((sum, item) => sum + (item.chiefs?.length || 0), 0) || 0 }} 人
              </span>
            </div>
            <div v-if="chiefOverviewData?.sectionChiefGroups?.length" class="chief-section-list">
              <section
                v-for="group in chiefOverviewData?.sectionChiefGroups"
                :key="group.sectionId || group.sectionName"
                class="chief-section-group"
              >
                <div class="chief-section-group__header">
                  <div class="chief-section-group__name">河段：{{ group.sectionName || '-' }}</div>
                  <div class="chief-section-group__count">{{ group.chiefs?.length || 0 }} 人</div>
                </div>
                <div class="chief-grid">
                  <article
                    v-for="chief in group.chiefs"
                    :key="`section-${group.sectionId}-${chief.id || chief.headName}`"
                    class="chief-card chief-card--section"
                  >
                    <div class="chief-card__top">
                      <div>
                        <div class="chief-card__name">{{ chief.headName || '-' }}</div>
                        <div class="chief-card__scope">归属：{{ group.sectionName || '-' }}</div>
                      </div>
                      <el-tag type="success" effect="plain" round>
                        {{ chief.headLevelLabel || resolveHeadLevelLabel(chief.headLevel) || '未标注' }}
                      </el-tag>
                    </div>
                    <div class="chief-card__meta">
                      <span>职务</span>
                      <span>{{ chief.headPosition || '-' }}</span>
                    </div>
                  </article>
                </div>
              </section>
            </div>
            <el-empty v-else description="当前河段暂无有效河长" :image-size="76" />
          </section>
        </template>
        <el-empty v-else-if="!chiefDrawerLoading" description="当前河道暂无有效河长信息" :image-size="88" />
      </div>
    </el-drawer>

    <el-dialog
      class="river-edit-dialog"
      v-model="createDialogVisible"
      :title="dialogTitle"
      width="1100px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="110px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="12">
          <el-col :span="24">
            <div class="group-title">自然属性</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道名称" prop="riverName">
              <el-input v-model="createForm.riverName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道长度" prop="lengthKm">
              <el-input-number v-model="createForm.lengthKm" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">km</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流域面积" prop="catchmentKm2">
              <el-input-number v-model="createForm.catchmentKm2" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">km²</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道平均比降" prop="averageSlope">
              <el-input-number v-model="createForm.averageSlope" :min="0" :precision="4" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道级别" prop="riverLevel">
              <el-select v-model="createForm.riverLevel" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in riverLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生态类型" prop="ecologyType">
              <el-select v-model="createForm.ecologyType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in ecologyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在流域" prop="basinType">
              <el-select v-model="createForm.basinType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in basinOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发源山系" prop="sourceMountainRange">
              <el-input v-model="createForm.sourceMountainRange" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河流归宿" prop="riverTerminus">
              <el-input v-model="createForm.riverTerminus" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道功能" prop="riverType">
              <el-select
                v-model="createForm.riverType"
                multiple
                collapse-tags
                :max-collapse-tags="6"
                clearable
                class="select-long"
                placeholder="可多选"
              >
                <el-option v-for="item in riverTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河口位置" prop="riverEntrance">
              <el-input v-model="createForm.riverEntrance" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河源位置" prop="riverOrigin">
              <el-input v-model="createForm.riverOrigin" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起点/终点" prop="startPoint">
              <div class="double-input">
                <el-input v-model="createForm.startPoint" placeholder="请输入起点" />
                <el-input v-model="createForm.endPoint" placeholder="请输入终点" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心经度" prop="centroidLongitude">
              <el-input-number v-model="createForm.centroidLongitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心纬度" prop="centroidLatitude">
              <el-input-number v-model="createForm.centroidLatitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河口经度" prop="riverEndLongitude">
              <el-input-number v-model="createForm.riverEndLongitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河口纬度" prop="riverEndLatitude">
              <el-input-number v-model="createForm.riverEndLatitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河源经度" prop="riverSourceLongitude">
              <el-input-number v-model="createForm.riverSourceLongitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河源纬度" prop="riverSourceLatitude">
              <el-input-number v-model="createForm.riverSourceLatitude" :precision="6" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流经地区" prop="flowAreas">
              <el-tree-select
                v-model="createForm.flowAreas"
                multiple
                show-checkbox
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterAreaTreeNode"
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                placeholder="请选择（可搜索）"
                class="select-long"
                :disabled="formReadonly"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">防洪工程</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跨界类别" prop="transboundaryType">
              <el-select v-model="createForm.transboundaryType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in transboundaryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防等级" prop="embankmentLevel">
              <el-select v-model="createForm.embankmentLevel" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in dikeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防长度" prop="embankmentLength">
              <el-input-number v-model="createForm.embankmentLength" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">km</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防洪标准" prop="floodStandard">
              <el-select v-model="createForm.floodStandard" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in floodOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">水文监测</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年均径流量" prop="averageAnnualRunoff">
              <el-input-number v-model="createForm.averageAnnualRunoff" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">m</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="历史最高水位" prop="historicalMaxWaterLevel">
              <el-input-number v-model="createForm.historicalMaxWaterLevel" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">m</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高水位时间" prop="maxWaterLevelDate">
              <el-date-picker
                v-model="createForm.maxWaterLevelDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="历史最低水位" prop="historicalMinWaterLevel">
              <el-input-number v-model="createForm.historicalMinWaterLevel" :min="0" :precision="2" controls-position="right" />
              <span class="unit-text">m</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低水位时间" prop="lowestWaterLevelDate">
              <el-date-picker
                v-model="createForm.lowestWaterLevelDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河道水质" prop="waterQualityStatus">
              <el-select v-model="createForm.waterQualityStatus" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in waterQualityOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">管理体系</div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="河道图片">
              <UploadImgs v-model="createForm.riverPhotos" :limit="6" :file-size="10" :drag="false" :disabled="formReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属乡镇" prop="town">
              <el-tree-select
                v-model="createForm.town"
                multiple
                show-checkbox
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterAreaTreeNode"
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                placeholder="请选择（可搜索）"
                class="select-long"
                :disabled="formReadonly"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="省级骨干河道" prop="isProvincialBackbone">
              <el-radio-group v-model="createForm.isProvincialBackbone">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理单位" prop="managementUnit">
              <el-input v-model="createForm.managementUnit" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="河长职责" prop="responsibilities">
              <el-input v-model="createForm.responsibilities" type="textarea" :rows="2" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="河道概况" prop="remarks">
              <el-input v-model="createForm.remarks" type="textarea" :rows="2" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">地图位置</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="facilityGeoJson"
              :active="createDialogVisible"
              :height="360"
            />
            <div v-else>
              <TiandituGeoJsonEditor v-model="facilityGeoJson" :height="420" />
              <div class="map-actions">
                <el-button
                  type="primary"
                  :disabled="!createForm.id"
                  :loading="facilityGeomSaving"
                  @click="handleSaveFacilityGeometry"
                >
                  保存位置
                </el-button>
                <el-button v-if="!createForm.id" disabled>请先保存河道基础信息</el-button>
              </div>
              <div class="map-tip">支持缩放、全屏、绘制点/线/面，支持基于已有图形进行二次编辑。</div>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="group-title">河段划分</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河段划分" prop="riverSectionCount">
              <el-select
                v-model="createForm.riverSectionCount"
                placeholder="请选择"
                class="select-long"
                :disabled="formReadonly"
                @change="handleRiverSectionCountChange"
              >
                <el-option
                  v-for="opt in riverSectionCountOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <template v-if="createForm.riverSectionCount && createForm.riverSectionCount > 0">
            <el-col :span="24" v-for="(seg, idx) in createForm.sections" :key="idx">
              <div class="segment-block">
                <div class="segment-title">河段{{ idx + 1 }}</div>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item :label="`河段${idx + 1}名称`" :prop="`sections.${idx}.sectionName`" :rules="[{ required: true, message: '请输入河段名称', trigger: 'blur' }]">
                      <el-input v-model="seg.sectionName" placeholder="请输入河段名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item :label="`起点/终点`">
                      <div class="double-input">
                        <el-input v-model="seg.startPoint" placeholder="起点" />
                        <el-input v-model="seg.endPoint" placeholder="终点" />
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="起点经纬度">
                      <div class="double-input">
                        <el-input-number v-model="seg.startLongitude" :precision="6" controls-position="right" placeholder="经度" />
                        <el-input-number v-model="seg.startLatitude" :precision="6" controls-position="right" placeholder="纬度" />
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="终点经纬度">
                      <div class="double-input">
                        <el-input-number v-model="seg.endLongitude" :precision="6" controls-position="right" placeholder="经度" />
                        <el-input-number v-model="seg.endLatitude" :precision="6" controls-position="right" placeholder="纬度" />
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="备注">
                      <el-input v-model="seg.remarks" type="textarea" :rows="2" placeholder="请输入备注" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <div class="segment-title">监督单位</div>
                    <el-row :gutter="12" v-for="(sup, supIdx) in seg.supervisions" :key="supIdx">
                      <el-col :span="12">
                        <el-form-item label="监督单位">
                          <el-input v-model="sup.supervisionUnit" placeholder="请输入监督单位" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="监督电话">
                          <el-input v-model="sup.supervisionContact" placeholder="请输入监督电话" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="24" class="supervision-actions">
                        <el-button
                          v-if="!formReadonly && seg.supervisions.length > 1"
                          type="danger"
                          link
                          @click="removeSectionSupervision(idx, supIdx)"
                        >
                          删除监督单位
                        </el-button>
                      </el-col>
                    </el-row>
                    <el-button type="primary" link @click="addSectionSupervision(idx)">新增监督单位</el-button>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </template>
          <template v-else>
            <el-col :span="24">
              <div class="segment-block">
                <div class="segment-title">监督单位</div>
                <el-row :gutter="12" v-for="(sup, supIdx) in riverSupervisions" :key="supIdx">
                  <el-col :span="12">
                    <el-form-item label="监督单位">
                      <el-input v-model="sup.supervisionUnit" placeholder="请输入监督单位" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="监督电话">
                      <el-input v-model="sup.supervisionContact" placeholder="请输入监督电话" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24" class="supervision-actions">
                    <el-button
                      v-if="!formReadonly && riverSupervisions.length > 1"
                      type="danger"
                      link
                      @click="removeRiverSupervision(supIdx)"
                    >
                      删除监督单位
                    </el-button>
                  </el-col>
                </el-row>
                <el-button type="primary" link @click="addRiverSupervision">新增监督单位</el-button>
              </div>
            </el-col>
          </template>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">关闭</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="handleSubmitForm">
          {{ isEditMode ? '保存修改' : '提交' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      class="river-head-dialog"
      v-model="headDialogVisible"
      title="河长信息"
      width="1000px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="headFormRef" label-width="100px" label-position="left">
        <div v-for="(sec, sIdx) in headForm.sections" :key="sIdx" class="head-section">
          <div class="section-title">河段 {{ sIdx + 1 }}</div>
          <el-row :gutter="12">
            <el-col :span="24">
              <el-form-item label="河段名称">
                <el-input v-model="sec.sectionName" disabled />
              </el-form-item>
            </el-col>
            <el-col v-for="(head, hIdx) in sec.heads" :key="hIdx" :span="24" class="head-card">
              <div class="segment-title head-title">
                <span>河长 {{ hIdx + 1 }}</span>
                <el-button v-if="sec.heads.length > 1" type="danger" link @click="removeHead(sIdx, hIdx)">
                  删除河长
                </el-button>
              </div>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="河长级别" required>
                    <el-select v-model="head.headLevel" placeholder="请选择" clearable class="select-long">
                      <el-option v-for="opt in headLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="河长职务">
                    <el-input v-model="head.headPosition" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="河长姓名" required>
                    <el-input v-model="head.headName" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="行政区划">
                    <el-tree-select
                      v-model="head.administrativeRegion"
                      multiple
                      show-checkbox
                      :data="areaTreeData"
                      :props="areaTreeProps"
                      node-key="value"
                      check-strictly
                      filterable
                      :filter-node-method="filterAreaTreeNode"
                      clearable
                      collapse-tags
                      collapse-tags-tooltip
                      :max-collapse-tags="6"
                      placeholder="请选择（可搜索）"
                      class="select-long"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-col>
            <el-col :span="24">
              <el-button type="primary" link @click="addHead(sIdx)">新增河长</el-button>
            </el-col>
            <el-col :span="24" class="head-card">
              <div class="segment-title">监督单位</div>
              <el-row :gutter="12" v-for="(sup, supIdx) in sec.supervisions" :key="supIdx">
                <el-col :span="12">
                  <el-form-item label="监督单位">
                    <el-input v-model="sup.supervisionUnit" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="监督电话">
                    <el-input v-model="sup.supervisionContact" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="24" class="supervision-actions">
                  <el-button
                    v-if="sec.supervisions.length > 1"
                    type="danger"
                    link
                    @click="removeSupervision(sIdx, supIdx)"
                  >
                    删除监督单位
                  </el-button>
                </el-col>
              </el-row>
              <el-button type="primary" link @click="addSupervision(sIdx)">新增监督单位</el-button>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="headDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="headSubmitLoading" @click="handleSubmitHead">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入河道" width="520px">
      <el-upload
        class="import-uploader"
        drag
        action="#"
        :auto-upload="false"
        :file-list="importFileList"
        :limit="1"
        accept=".xls,.xlsx"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :on-exceed="handleImportExceed"
      >
        <Icon icon="ep:upload" class="mb-8px" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            <div>仅允许导入 xls、xlsx 格式文件。</div>
            <el-link :underline="false" type="primary" @click="downloadImportTemplate">下载导入模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import {
  createRiverChannel,
  deleteRiverChannel,
  getRiverChannelImportTemplate,
  getRiverChannelPage,
  getRiverChannelDetail,
  getRiverChiefOverview,
  getRiverDict,
  getRiverSections,
  getRiverManagement,
  importRiverChannelExcel,
  exportRiverChannelExcel,
  saveRiverManagement,
  updateRiverChannel,
  updateRiverChannelGeometry,
  type DictDataItemRespVO,
  type RiverChannelPageReqVO,
  type RiverChannelPageRespVO,
  type RiverChannelDetailRespVO,
  type RiverChiefOverviewRespVO,
  type RiverSectionSaveReqVO,
  type RiverHeadSectionItemReqVO,
  type RiverHeadItemReqVO,
  type RiverHeadSupervisionItemReqVO
} from '@/api/gis/riverChannelBf'
import download from '@/utils/download'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import { getAreaTree } from '@/api/system/area'

const queryParams = reactive<RiverChannelPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  riverName: '',
  riverLevel: [],
  ecologyType: '',
  isProvincialBackbone: undefined as number | undefined
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<RiverChannelPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)
const chiefDrawerVisible = ref(false)
const chiefDrawerLoading = ref(false)
const chiefDrawerRiverName = ref('')
const chiefOverviewData = ref<RiverChiefOverviewRespVO>()

const riverLevelOptions = ref<DictDataItemRespVO[]>([])
const ecologyOptions = ref<DictDataItemRespVO[]>([])
const queryProvincialBackboneChecked = computed<boolean>({
  get: () => queryParams.isProvincialBackbone === 1,
  set: (checked) => {
    queryParams.isProvincialBackbone = checked ? 1 : undefined
  }
})
const basinOptions = ref<DictDataItemRespVO[]>([])
const transboundaryOptions = ref<DictDataItemRespVO[]>([])
const floodOptions = ref<DictDataItemRespVO[]>([])
const dikeOptions = ref<DictDataItemRespVO[]>([])
const riverTypeOptions = ref<DictDataItemRespVO[]>([])
const waterQualityOptions = ref<DictDataItemRespVO[]>([])
const headLevelOptions = ref<DictDataItemRespVO[]>([])

type AreaTreeNode = {
  id: number
  name: string
  children?: AreaTreeNode[]
}
type AreaTreeSelectNode = {
  value: string
  label: string
  children?: AreaTreeSelectNode[]
}
const areaTreeData = ref<AreaTreeSelectNode[]>([])
const areaTreeProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

const filterAreaTreeNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
}

const buildAreaTreeSelectData = (nodes?: AreaTreeNode[]): AreaTreeSelectNode[] => {
  if (!nodes || !nodes.length) return []
  return nodes
    .filter((n) => !!n)
    .map((node) => ({
      value: String(node.id),
      label: node.name,
      children: buildAreaTreeSelectData(node.children)
    }))
}

const createDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const facilityGeoJson = ref('')
const facilityGeomSaving = ref(false)
const riverSectionCountOptions = [
  { value: 0, label: '不划分河段' },
  { value: 2, label: '2段' },
  { value: 3, label: '3段' },
  { value: 4, label: '4段' },
  { value: 5, label: '5段' },
  { value: 6, label: '6段' }
]
const createForm = reactive<RiverChannelDetailRespVO>({
  id: '',
  facilityId: '',
  riverCode: '',
  riverName: '',
  lengthKm: undefined,
  catchmentKm2: undefined,
  averageSlope: undefined,
  basinType: '',
  ecologyType: '',
  transboundaryType: '',
  floodStandard: '',
  embankmentLevel: '',
  embankmentLength: undefined,
  centroidLongitude: undefined,
  centroidLatitude: undefined,
  riverEndLongitude: undefined,
  riverEndLatitude: undefined,
  riverSourceLongitude: undefined,
  riverSourceLatitude: undefined,
  flowAreas: [],
  historicalMaxWaterLevel: undefined,
  maxWaterLevelDate: '',
  lowestWaterLevelDate: '',
  historicalMinWaterLevel: undefined,
  averageAnnualRunoff: undefined,
  sourceMountainRange: '',
  riverTerminus: '',
  riverLevel: '',
  isProvincialBackbone: 0,
  riverEntrance: '',
  riverOrigin: '',
  startPoint: '',
  endPoint: '',
  riverType: [],
  riverPhotos: [],
  waterQualityStatus: '',
  riverSectionCount: 0,
  town: [],
  managementUnit: '',
  responsibilities: '',
  remarks: '',
  sections: []
})

const originalResponsibilities = ref('')
const riverSupervisions = ref<RiverHeadSupervisionItemReqVO[]>([{ supervisionUnit: '', supervisionContact: '' }])

const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑河道'
  if (formMode.value === 'view') return '河道详情'
  return '新增河道'
})
const formReadonly = computed(() => formMode.value === 'view')
const isEditMode = computed(() => formMode.value === 'edit')

const createRules: FormRules = {
  riverName: [{ required: true, message: '请输入河道名称', trigger: 'blur' }],
  lengthKm: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  catchmentKm2: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  embankmentLength: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  averageAnnualRunoff: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  historicalMaxWaterLevel: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  historicalMinWaterLevel: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  averageSlope: [{ type: 'number', min: 0, message: '请输入非负数', trigger: 'change' }],
  riverSectionCount: [
    {
      validator: (_, value, callback) => {
        const count = Number(value || 0)
        if (count === 0) return callback()
        if (count < 2 || count > 6) {
          return callback(new Error('河段划分仅支持“不划分河段”或 2-6 段'))
        }
        return callback()
      },
      trigger: 'change'
    }
  ]
}

type HeadSupervisionForm = RiverHeadSupervisionItemReqVO
type HeadForm = RiverHeadItemReqVO
type SectionHeadForm = RiverHeadSectionItemReqVO & { heads: HeadForm[]; supervisions: HeadSupervisionForm[] }

const headDialogVisible = ref(false)
const headSubmitLoading = ref(false)
const headFormRef = ref<FormInstance>()
const headForm = reactive<{ riverChannelId?: string; sections: SectionHeadForm[] }>({
  riverChannelId: undefined,
  sections: []
})

const hasChiefOverviewData = computed(() => {
  const riverCount = chiefOverviewData.value?.riverChiefs?.length || 0
  const sectionCount = chiefOverviewData.value?.sectionChiefGroups?.length || 0
  return riverCount > 0 || sectionCount > 0
})

const formatNumber = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return num.toFixed(2)
}

const resolveHeadLevelLabel = (value?: string) => {
  if (!value) return ''
  return headLevelOptions.value.find((item) => item.value === value)?.label || value
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getRiverChannelPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchTable()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  fetchTable()
}

const handleDelete = async (row: RiverChannelPageRespVO) => {
  await ElMessageBox.confirm(`确认删除河道【${row.riverName || row.riverCode || ''}】吗？`, '提示', {
    type: 'warning'
  })
  await deleteRiverChannel(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const handleDetail = async (row: RiverChannelPageRespVO) => {
  await openEditDialog(String(row.id), 'view')
}

const handleEdit = async (row: RiverChannelPageRespVO) => {
  await openEditDialog(String(row.id), 'edit')
}

const handleChiefOverview = async (row: RiverChannelPageRespVO) => {
  chiefDrawerRiverName.value = row.riverName || ''
  chiefOverviewData.value = undefined
  chiefDrawerVisible.value = true
  chiefDrawerLoading.value = true
  try {
    const data = await getRiverChiefOverview(row.id)
    chiefOverviewData.value = data
  } finally {
    chiefDrawerLoading.value = false
  }
}

const handleExport = async () => {
  try {
    const data = await exportRiverChannelExcel(queryParams)
    download.excel(data, '河道信息.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const importVisible = ref(false)
const importLoading = ref(false)
const importFileList = ref<UploadFile[]>([])
const importRawFile = ref<File>()

const openImportDialog = () => {
  importVisible.value = true
  importFileList.value = []
  importRawFile.value = undefined
}

const handleImportFileChange = (file: UploadFile, fileList: UploadFiles) => {
  importFileList.value = fileList.slice(-1)
  importRawFile.value = file.raw
}

const handleImportFileRemove = () => {
  importRawFile.value = undefined
}

const handleImportExceed = () => {
  ElMessage.warning('最多只能上传一个文件')
}

const downloadImportTemplate = async () => {
  const data = await getRiverChannelImportTemplate()
  download.excel(data, '河道导入模板.xls')
}

const submitImport = async () => {
  if (!importRawFile.value) {
    ElMessage.warning('请先上传导入文件')
    return
  }
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importRawFile.value)
    const result: any = await importRiverChannelExcel(formData)
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item: string) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，跳过 ${result?.skipCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    await fetchTable()
  } finally {
    importLoading.value = false
  }
}

const loadDict = async () => {
  const [riverLevels, ecologyTypes, basins, kjlb, fhbz, dfdj, hdlx, szqk, hzjb] = await Promise.all([
    getRiverDict('zd_hljb'),
    getRiverDict('zd_stlx'),
    getRiverDict('zd_szly'),
    getRiverDict('zd_kjlb'),
    getRiverDict('zd_fhbz'),
    getRiverDict('zd_dfdj'),
    getRiverDict('zd_hdlx'),
    getRiverDict('zd_hdszqk'),
    getRiverDict('zd_hzjb')
  ])
  riverLevelOptions.value = riverLevels || []
  ecologyOptions.value = ecologyTypes || []
  basinOptions.value = basins || []
  transboundaryOptions.value = kjlb || []
  floodOptions.value = fhbz || []
  dikeOptions.value = dfdj || []
  riverTypeOptions.value = hdlx || []
  waterQualityOptions.value = szqk || []
  headLevelOptions.value = hzjb || []
}

onMounted(async () => {
  await loadDict()
  try {
    const list = (await getAreaTree()) as unknown as AreaTreeNode[]
    areaTreeData.value = Array.isArray(list) ? buildAreaTreeSelectData(list) : []
  } catch {
    areaTreeData.value = []
  }
  fetchTable()
})

const resetCreateForm = () => {
  createForm.id = ''
  createForm.facilityId = ''
  createForm.riverName = ''
  createForm.lengthKm = undefined
  createForm.catchmentKm2 = undefined
  createForm.averageSlope = undefined
  createForm.basinType = ''
  createForm.ecologyType = ''
  createForm.transboundaryType = ''
  createForm.floodStandard = ''
  createForm.embankmentLevel = ''
  createForm.embankmentLength = undefined
  createForm.centroidLongitude = undefined
  createForm.centroidLatitude = undefined
  createForm.riverEndLongitude = undefined
  createForm.riverEndLatitude = undefined
  createForm.riverSourceLongitude = undefined
  createForm.riverSourceLatitude = undefined
  createForm.flowAreas = []
  createForm.historicalMaxWaterLevel = undefined
  createForm.maxWaterLevelDate = ''
  createForm.lowestWaterLevelDate = ''
  createForm.historicalMinWaterLevel = undefined
  createForm.averageAnnualRunoff = undefined
  createForm.sourceMountainRange = ''
  createForm.riverTerminus = ''
  createForm.riverLevel = ''
  createForm.isProvincialBackbone = 0
  createForm.riverEntrance = ''
  createForm.riverOrigin = ''
  createForm.startPoint = ''
  createForm.endPoint = ''
  createForm.riverType = []
  createForm.riverPhotos = []
  createForm.waterQualityStatus = ''
  createForm.riverSectionCount = 0
  createForm.town = []
  createForm.managementUnit = ''
  createForm.responsibilities = ''
  originalResponsibilities.value = ''
  createForm.remarks = ''
  createForm.sections = []
  riverSupervisions.value = [buildEmptySupervision()]
}

const openCreateDialog = () => {
  resetCreateForm()
  formMode.value = 'create'
  facilityGeoJson.value = ''
  createDialogVisible.value = true
}

const fillCreateForm = (detail: RiverChannelDetailRespVO) => {
  resetCreateForm()
  createForm.id = detail.id || ''
  createForm.facilityId = detail.facilityId || ''
  createForm.riverName = detail.riverName || ''
  createForm.lengthKm = detail.lengthKm
  createForm.catchmentKm2 = detail.catchmentKm2
  createForm.averageSlope = detail.averageSlope
  createForm.basinType = detail.basinType || ''
  createForm.ecologyType = detail.ecologyType || ''
  createForm.transboundaryType = detail.transboundaryType || ''
  createForm.floodStandard = detail.floodStandard || ''
  createForm.embankmentLevel = detail.embankmentLevel || ''
  createForm.embankmentLength = detail.embankmentLength
  createForm.centroidLongitude = detail.centroidLongitude
  createForm.centroidLatitude = detail.centroidLatitude
  createForm.riverEndLongitude = detail.riverEndLongitude
  createForm.riverEndLatitude = detail.riverEndLatitude
  createForm.riverSourceLongitude = detail.riverSourceLongitude
  createForm.riverSourceLatitude = detail.riverSourceLatitude
  createForm.flowAreas = Array.isArray(detail.flowAreas) ? detail.flowAreas : []
  createForm.historicalMaxWaterLevel = detail.historicalMaxWaterLevel
  createForm.maxWaterLevelDate = detail.maxWaterLevelDate || ''
  createForm.lowestWaterLevelDate = detail.lowestWaterLevelDate || ''
  createForm.historicalMinWaterLevel = detail.historicalMinWaterLevel
  createForm.averageAnnualRunoff = detail.averageAnnualRunoff
  createForm.sourceMountainRange = detail.sourceMountainRange || ''
  createForm.riverTerminus = detail.riverTerminus || ''
  createForm.riverLevel = detail.riverLevel || ''
  createForm.isProvincialBackbone = Number(detail.isProvincialBackbone || 0)
  createForm.riverEntrance = detail.riverEntrance || ''
  createForm.riverOrigin = detail.riverOrigin || ''
  createForm.startPoint = detail.startPoint || ''
  createForm.endPoint = detail.endPoint || ''
  createForm.riverType = detail.riverType || []
  createForm.riverPhotos = detail.riverPhotos || []
  createForm.waterQualityStatus = detail.waterQualityStatus || ''
  createForm.town = Array.isArray(detail.town) ? detail.town : []
  createForm.managementUnit = detail.managementUnit || ''
  createForm.responsibilities = detail.responsibilities || ''
  originalResponsibilities.value = createForm.responsibilities
  createForm.remarks = detail.remarks || ''
  createForm.sections =
    detail.sections && detail.sections.length > 0
      ? detail.sections.map((item) => ({
      id: item.id,
      facilityId: item.facilityId,
      sectionName: item.sectionName || '',
      startPoint: item.startPoint || '',
      endPoint: item.endPoint || '',
      startLongitude: item.startLongitude,
      startLatitude: item.startLatitude,
      endLongitude: item.endLongitude,
      endLatitude: item.endLatitude,
      remarks: item.remarks || '',
      supervisions: [buildEmptySupervision()]
    }))
      : []
  const rawCount = createForm.sections.length ? createForm.sections.length : Number(detail.riverSectionCount || 0)
  createForm.riverSectionCount = rawCount <= 1 ? 0 : rawCount
  // 兜底：当后端未返回河段明细时，根据划分数量生成可编辑的河段列表
  syncSectionList()
}

const openEditDialog = async (id: string, mode: 'edit' | 'view') => {
  const detail = await getRiverChannelDetail(id)
  formMode.value = mode
  fillCreateForm(detail)
  try {
    await loadInlineSupervisions(id)
  } catch {
    riverSupervisions.value = [buildEmptySupervision()]
  }
  facilityGeoJson.value = detail?.geometryGeoJson || ''
  createDialogVisible.value = true
}

const handleSaveFacilityGeometry = async () => {
  if (!createForm.id) {
    ElMessage.warning('请先保存河道基础信息后再保存位置')
    return
  }
  facilityGeomSaving.value = true
  try {
    const facilityId = await updateRiverChannelGeometry(String(createForm.id), {
      geometryGeoJson: facilityGeoJson.value || '',
      srid: 4490
    })
    createForm.facilityId = facilityId || createForm.facilityId
    ElMessage.success('位置已保存')
  } finally {
    facilityGeomSaving.value = false
  }
}

watch(
  () => createDialogVisible.value,
  (visible) => {
    if (!visible) {
      facilityGeoJson.value = ''
    }
  }
)

const handleRiverSectionCountChange = () => {
  const count = Number(createForm.riverSectionCount || 0)
  // 清空/非法值兜底为“不划分河段”
  if (!Number.isFinite(count) || count <= 0) {
    createForm.riverSectionCount = 0
    createForm.sections = []
    riverSupervisions.value = ensureSupervisionList(riverSupervisions.value)
    return
  }
  createForm.riverSectionCount = count
  syncSectionList()
}

const syncSectionList = () => {
  const count = Number(createForm.riverSectionCount || 0)
  const list: Array<RiverSectionSaveReqVO & { supervisions?: HeadSupervisionForm[] }> = Array.isArray(createForm.sections)
    ? [...createForm.sections]
    : []
  while (list.length < count) {
    list.push({
      id: '',
      facilityId: '',
      sectionName: '',
      startPoint: '',
      endPoint: '',
      startLongitude: undefined,
      startLatitude: undefined,
      endLongitude: undefined,
      endLatitude: undefined,
      remarks: '',
      supervisions: [buildEmptySupervision()]
    })
  }
  if (list.length > count) {
    list.length = count
  }
  list.forEach((item) => {
    item.supervisions = ensureSupervisionList(item.supervisions)
  })
  createForm.sections = list
}

watch(
  () => createForm.riverSectionCount,
  () => {
    syncSectionList()
  }
)

const buildEmptySupervision = (): HeadSupervisionForm => ({
  supervisionUnit: '',
  supervisionContact: ''
})
const ensureSupervisionList = (list?: HeadSupervisionForm[]) => {
  const normalized =
    Array.isArray(list) && list.length
      ? list.map((item) => ({
          supervisionUnit: item?.supervisionUnit || '',
          supervisionContact: item?.supervisionContact || ''
        }))
      : []
  return normalized.length ? normalized : [buildEmptySupervision()]
}
const normalizeSupervisionPayload = (list?: HeadSupervisionForm[]) => {
  return (list || [])
    .map((item) => ({
      supervisionUnit: (item?.supervisionUnit || '').trim(),
      supervisionContact: (item?.supervisionContact || '').trim()
    }))
    .filter((item) => item.supervisionUnit || item.supervisionContact)
}
const addRiverSupervision = () => {
  riverSupervisions.value.push(buildEmptySupervision())
}
const removeRiverSupervision = (supIndex: number) => {
  riverSupervisions.value.splice(supIndex, 1)
  if (!riverSupervisions.value.length) {
    riverSupervisions.value.push(buildEmptySupervision())
  }
}
const addSectionSupervision = (sectionIndex: number) => {
  const sec = (createForm.sections?.[sectionIndex] || null) as (RiverSectionSaveReqVO & { supervisions?: HeadSupervisionForm[] }) | null
  if (!sec) return
  sec.supervisions = ensureSupervisionList(sec.supervisions)
  sec.supervisions.push(buildEmptySupervision())
}
const removeSectionSupervision = (sectionIndex: number, supIndex: number) => {
  const sec = (createForm.sections?.[sectionIndex] || null) as (RiverSectionSaveReqVO & { supervisions?: HeadSupervisionForm[] }) | null
  if (!sec) return
  sec.supervisions = ensureSupervisionList(sec.supervisions)
  sec.supervisions.splice(supIndex, 1)
  if (!sec.supervisions.length) {
    sec.supervisions.push(buildEmptySupervision())
  }
}
const loadInlineSupervisions = async (channelId: string) => {
  const management = await getRiverManagement(channelId)
  const hasSectionSplit = Number(createForm.riverSectionCount || 0) > 0
  const sectionSupervisionMap = new Map<string, HeadSupervisionForm[]>()
  const channelSupervisions: HeadSupervisionForm[] = []
  ;(management || []).forEach((item) => {
    const supervisions = normalizeSupervisionPayload(item.supervisions as HeadSupervisionForm[])
    if (item.sectionId) {
      sectionSupervisionMap.set(
        String(item.sectionId),
        supervisions.length ? supervisions : [buildEmptySupervision()]
      )
      return
    }
    if (supervisions.length) {
      channelSupervisions.push(...supervisions)
    }
  })
  if (!hasSectionSplit) {
    riverSupervisions.value = ensureSupervisionList(channelSupervisions)
    return
  }
  const list = (Array.isArray(createForm.sections) ? [...createForm.sections] : []) as Array<
    RiverSectionSaveReqVO & { supervisions?: HeadSupervisionForm[] }
  >
  list.forEach((sec, idx) => {
    const sectionId = sec.id ? String(sec.id) : ''
    if (sectionId && sectionSupervisionMap.has(sectionId)) {
      sec.supervisions = ensureSupervisionList(sectionSupervisionMap.get(sectionId))
      return
    }
    sec.supervisions = ensureSupervisionList(sec.supervisions)
    if (!sectionId && idx === 0 && channelSupervisions.length) {
      sec.supervisions = ensureSupervisionList(channelSupervisions)
    }
  })
  createForm.sections = list
  riverSupervisions.value = [buildEmptySupervision()]
}

const buildEmptyHead = (): HeadForm => ({
  headLevel: '',
  headPosition: '',
  headName: '',
  administrativeRegion: []
})
const buildSectionHead = (sectionId?: string, sectionName?: string): SectionHeadForm => ({
  sectionId,
  sectionName: sectionName || '',
  heads: [buildEmptyHead()],
  supervisions: [buildEmptySupervision()]
})

const openHeadDialog = async (channelId: string, riverName: string) => {
  headForm.riverChannelId = channelId
  const management = await getRiverManagement(channelId)
  if (management && management.length) {
    headForm.sections = management.map((item) => ({
      sectionId: item.sectionId ? String(item.sectionId) : undefined,
      sectionName: item.sectionName || riverName,
      heads:
        item.heads && item.heads.length
          ? item.heads.map((head) => ({
              headLevel: head.headLevel || '',
              headPosition: head.headPosition || '',
              headName: head.headName || '',
              administrativeRegion: Array.isArray(head.administrativeRegion) ? head.administrativeRegion : []
            }))
          : [buildEmptyHead()]
      ,
      supervisions:
        item.supervisions && item.supervisions.length
          ? item.supervisions.map((sup) => ({
              supervisionUnit: sup.supervisionUnit || '',
              supervisionContact: sup.supervisionContact || ''
            }))
          : [buildEmptySupervision()]
    }))
  } else {
    const sections = await getRiverSections(channelId)
    if (sections && sections.length) {
      headForm.sections = sections.map((item) => buildSectionHead(String(item.id), item.sectionName || riverName))
    } else {
      headForm.sections = [buildSectionHead(undefined, riverName)]
    }
  }
  headDialogVisible.value = true
}

const addHead = (sectionIndex: number) => {
  headForm.sections[sectionIndex].heads.push(buildEmptyHead())
}

const addSupervision = (sectionIndex: number) => {
  headForm.sections[sectionIndex].supervisions.push(buildEmptySupervision())
}

const removeHead = (sectionIndex: number, headIndex: number) => {
  const sec = headForm.sections[sectionIndex]
  if (!sec) return
  sec.heads.splice(headIndex, 1)
  // 至少保留一条空河长，避免界面没有可编辑项
  if (!sec.heads.length) {
    sec.heads.push(buildEmptyHead())
  }
}

const removeSupervision = (sectionIndex: number, supIndex: number) => {
  const sec = headForm.sections[sectionIndex]
  if (!sec) return
  sec.supervisions.splice(supIndex, 1)
  // 至少保留一条空监督单位，避免界面没有可编辑项
  if (!sec.supervisions.length) {
    sec.supervisions.push(buildEmptySupervision())
  }
}

const normalizeHeadPayload = () => {
  const sections: SectionHeadForm[] = []
  headForm.sections.forEach((sec) => {
    const heads = (sec.heads || [])
      .map((head) => {
        const hasData = head.headLevel || head.headName || head.headPosition
        if (!hasData) return null
        return {
          headLevel: head.headLevel,
          headPosition: head.headPosition,
          headName: head.headName,
          administrativeRegion: Array.isArray(head.administrativeRegion) ? head.administrativeRegion : []
        } as RiverHeadItemReqVO
      })
      .filter((item) => item) as HeadForm[]
    const supervisions =
      sec.supervisions?.filter((sup) => sup.supervisionUnit || sup.supervisionContact).map((sup) => ({ ...sup })) || []
    if (!heads.length && !supervisions.length) return
    const hasSection = !!sec.sectionId
    const referenceType = hasSection ? 'river_section' : 'river'
    const referenceId = hasSection ? String(sec.sectionId) : String(headForm.riverChannelId)
    sections.push({
      sectionId: sec.sectionId,
      referenceId,
      referenceType,
      sectionName: sec.sectionName,
      heads,
      supervisions
    })
  })
  return sections
}

const handleSubmitHead = async () => {
  if (!headForm.riverChannelId) {
    ElMessage.error('缺少河道信息')
    return
  }
  for (const sec of headForm.sections || []) {
    const sectionName = sec.sectionName || '河道'
    const heads = sec.heads || []
    for (let i = 0; i < heads.length; i++) {
      const head = heads[i]
      const hasData = head.headLevel || head.headName || head.headPosition
      if (!hasData) continue
      if (!(head.headLevel || '').trim()) {
        ElMessage.error(`【${sectionName}】第 ${i + 1} 个河长：河长级别不能为空`)
        return
      }
      if (!(head.headName || '').trim()) {
        ElMessage.error(`【${sectionName}】第 ${i + 1} 个河长：河长姓名不能为空`)
        return
      }
    }
  }
  const sections = normalizeHeadPayload()
  if (!sections.length) {
    ElMessage.error('请至少填写一条河长或监督单位信息')
    return
  }
  headSubmitLoading.value = true
  try {
    await saveRiverManagement({
      riverChannelId: String(headForm.riverChannelId),
      sections
    })
    ElMessage.success('河长信息保存成功')
    headDialogVisible.value = false
  } finally {
    headSubmitLoading.value = false
  }
}

const buildInlineSupervisionSections = async (channelId: string): Promise<SectionHeadForm[]> => {
  const hasSectionSplit = Number(createForm.riverSectionCount || 0) > 0
  if (!hasSectionSplit) {
    return [{
      sectionId: undefined,
      referenceId: channelId,
      referenceType: 'river',
      sectionName: createForm.riverName || '河道',
      heads: [],
      supervisions: normalizeSupervisionPayload(riverSupervisions.value)
    }]
  }
  let sectionList = (Array.isArray(createForm.sections) ? [...createForm.sections] : []) as Array<
    RiverSectionSaveReqVO & { supervisions?: HeadSupervisionForm[] }
  >
  const hasMissingId = sectionList.some((item) => !item.id)
  if (hasMissingId) {
    const latest = await getRiverChannelDetail(channelId)
    const latestSections = (Array.isArray(latest?.sections) ? latest.sections : []) as RiverSectionSaveReqVO[]
    sectionList = sectionList.map((item, idx) => ({
      ...item,
      id: item.id || latestSections[idx]?.id || ''
    }))
    createForm.sections = sectionList
  }
  return sectionList.map((item, idx) => {
    const sectionId = item.id ? String(item.id) : ''
    if (!sectionId) {
      throw new Error(`河段${idx + 1}绑定监督单位失败：未获取到河段ID`)
    }
    return {
      sectionId,
      referenceId: sectionId,
      referenceType: 'river_section',
      sectionName: item.sectionName || `河段${idx + 1}`,
      heads: [],
      supervisions: normalizeSupervisionPayload(ensureSupervisionList(item.supervisions))
    }
  })
}

const handleSubmitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  submitLoading.value = true
  try {
    const hasSectionSplit = Number(createForm.riverSectionCount || 0) > 0
    const sectionPayload = hasSectionSplit
      ? (createForm.sections || []).map((item) => ({
          id: item.id || undefined,
          facilityId: item.facilityId || undefined,
          sectionName: item.sectionName || '',
          startPoint: item.startPoint || '',
          endPoint: item.endPoint || '',
          startLongitude: item.startLongitude,
          startLatitude: item.startLatitude,
          endLongitude: item.endLongitude,
          endLatitude: item.endLatitude,
          remarks: item.remarks || ''
        }))
      : []
    const payload: RiverChannelDetailRespVO = {
      ...createForm,
      id: createForm.id || undefined,
      facilityId: createForm.facilityId || undefined,
      riverSectionCount: hasSectionSplit ? sectionPayload.length : 0,
      sections: sectionPayload,
      // 将起止点描述写回起点/终点字段
      riverEntrance: createForm.riverEntrance,
      riverOrigin: createForm.riverOrigin
    }
    let channelId = ''
    if (isEditMode.value) {
      await updateRiverChannel(payload)
      channelId = String(createForm.id || '')
      if (!channelId) {
        throw new Error('缺少河道ID，无法保存监督单位')
      }
      const supervisionSections = await buildInlineSupervisionSections(channelId)
      await saveRiverManagement({
        riverChannelId: channelId,
        sections: supervisionSections
      })
      ElMessage.success('保存成功')
      createDialogVisible.value = false
      fetchTable()
    } else {
      const createRes = await createRiverChannel(payload)
      channelId = String((createRes as any)?.data ?? createRes ?? '')
      if (!channelId) {
        throw new Error('新增河道成功但未返回ID，无法保存监督单位')
      }
      const supervisionSections = await buildInlineSupervisionSections(channelId)
      await saveRiverManagement({
        riverChannelId: channelId,
        sections: supervisionSections
      })
      ElMessage.success('新增成功')
      createDialogVisible.value = false
      fetchTable()
    }
  } finally {
    submitLoading.value = false
  }
}

const handleSubmitForm = () => handleSubmitCreate()
</script>

<style scoped>
.river-page {
  --river-bg: #f3f7fc;
  --river-card-bg: #ffffff;
  --river-border: rgba(148, 163, 184, 0.28);
  --river-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --river-primary: #1e40af;
  --river-primary-soft: rgba(30, 64, 175, 0.08);
  --river-text-main: #1f2a37;
  --river-text-muted: #5b677a;

  padding: 4px 2px 10px;
  background: linear-gradient(180deg, #f8fbff 0%, var(--river-bg) 100%);
}

.query-wrap,
.table-wrap {
  background: var(--river-card-bg);
  border: 1px solid var(--river-border);
  border-radius: 14px;
  box-shadow: var(--river-shadow);
}

.table-wrap {
  margin-top: 12px;
}

.query-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.query-form :deep(.el-form-item__label) {
  color: var(--river-text-main);
  font-weight: 600;
}

.query-form :deep(.el-input__wrapper),
.query-form :deep(.el-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.32) inset;
  transition: box-shadow 0.2s ease, background-color 0.2s ease;
}

.query-form :deep(.el-input__wrapper:hover),
.query-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.42) inset;
}

.query-form :deep(.el-input__wrapper.is-focus),
.query-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--river-primary) inset;
}

.query-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

.query-form :deep(.el-button:not(.el-button--primary):not(.is-plain)) {
  border-color: rgba(148, 163, 184, 0.38);
}

.provincial-backbone-item :deep(.el-form-item__label) {
  white-space: nowrap;
}

.query-single-checkbox {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.32) inset;
  background: #fff;
  margin-right: 0;
}

.query-single-checkbox :deep(.el-checkbox__label) {
  padding-left: 4px;
  color: var(--river-text-main);
  font-weight: 500;
  white-space: nowrap;
}

.river-table {
  border-radius: 12px;
  overflow: hidden;
}

.river-table :deep(.el-table__header th) {
  background: #f5f8ff;
  color: #1e3a8a;
  font-weight: 700;
}

.river-table :deep(.el-table__row > td) {
  transition: background-color 0.2s ease;
}

.river-table :deep(.el-table__body tr:hover > td) {
  background: var(--river-primary-soft);
}

.river-table :deep(.el-button.is-link) {
  font-weight: 600;
}

.table-wrap :deep(.el-pagination) {
  margin-top: 14px;
  justify-content: flex-end;
}

.select-long {
  min-width: 220px;
}

.double-input {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.unit-text {
  margin-left: 6px;
  color: var(--river-text-muted);
}

.section-title {
  font-weight: 700;
  margin: 8px 0;
  color: var(--river-text-main);
}

.group-title {
  font-weight: 700;
  margin: 12px 0 14px;
  padding: 7px 12px;
  color: var(--river-text-main);
  background: linear-gradient(90deg, rgba(30, 64, 175, 0.12), rgba(30, 64, 175, 0.02));
  border-left: 4px solid var(--river-primary);
  border-radius: 8px;
}

.map-tip {
  margin-top: 8px;
  font-size: 13px;
  color: var(--river-text-muted);
  line-height: 1.5;
}

.map-actions {
  margin-top: 10px;
}

.map-actions :deep(.el-button) {
  border-radius: 9px;
}

.segment-block {
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 10px;
  padding: 14px;
  margin-top: 8px;
  background: #fcfdff;
}

.segment-title {
  font-weight: 700;
  margin-bottom: 10px;
  color: #1e3a8a;
}

.head-section {
  border: 1px solid rgba(148, 163, 184, 0.34);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
  background: #fcfdff;
}

.head-card {
  border: 1px dashed rgba(100, 116, 139, 0.45);
  border-radius: 10px;
  padding: 12px;
  margin-top: 8px;
  background: #ffffff;
}

.head-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chief-overview {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 100%;
}

.chief-overview__hero {
  padding: 18px;
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(30, 64, 175, 0.96), rgba(59, 130, 246, 0.88)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0));
  box-shadow: 0 18px 36px rgba(30, 64, 175, 0.18);
  color: #eff6ff;
}

.chief-overview__eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  opacity: 0.78;
}

.chief-overview__title {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.chief-overview__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.chief-overview__stat {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
}

.chief-overview__stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
}

.chief-overview__stat-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.82;
}

.chief-block {
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.06);
}

.chief-block__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.chief-block__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: var(--river-text-main);
}

.chief-block__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 50px;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.chief-block__badge--river {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.14);
}

.chief-block__badge--section {
  color: #047857;
  background: rgba(16, 185, 129, 0.14);
}

.chief-block__count {
  color: var(--river-text-muted);
  font-size: 13px;
  font-weight: 600;
}

.chief-grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  gap: 12px;
}

.chief-card {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.14);
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.chief-card:hover {
  transform: translateY(-1px);
  border-color: rgba(59, 130, 246, 0.28);
  box-shadow: 0 14px 28px rgba(59, 130, 246, 0.08);
}

.chief-card--section {
  border-color: rgba(16, 185, 129, 0.16);
}

.chief-card--section:hover {
  border-color: rgba(16, 185, 129, 0.28);
  box-shadow: 0 14px 28px rgba(16, 185, 129, 0.08);
}

.chief-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.chief-card__name {
  font-size: 18px;
  font-weight: 700;
  color: var(--river-text-main);
}

.chief-card__scope {
  margin-top: 4px;
  font-size: 12px;
  color: var(--river-text-muted);
}

.chief-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--river-text-main);
  border-top: 1px dashed rgba(148, 163, 184, 0.28);
}

.chief-card__meta span:first-child {
  color: var(--river-text-muted);
}

.chief-section-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chief-section-group {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(16, 185, 129, 0.16);
  background: linear-gradient(180deg, rgba(236, 253, 245, 0.72), rgba(255, 255, 255, 0.96));
}

.chief-section-group__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.chief-section-group__name {
  font-size: 15px;
  font-weight: 700;
  color: #065f46;
}

.chief-section-group__count {
  font-size: 12px;
  font-weight: 700;
  color: #047857;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(16, 185, 129, 0.12);
}

.supervision-actions {
  text-align: right;
  margin-top: 4px;
}

:deep(.river-edit-dialog .el-dialog),
:deep(.river-head-dialog .el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.2);
}

:deep(.river-edit-dialog .el-dialog__header),
:deep(.river-head-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(90deg, #f8fbff 0%, #f3f7ff 100%);
}

:deep(.river-edit-dialog .el-dialog__title),
:deep(.river-head-dialog .el-dialog__title) {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.river-edit-dialog .el-dialog__body),
:deep(.river-head-dialog .el-dialog__body) {
  padding: 16px 20px;
}

:deep(.river-edit-dialog .el-dialog__footer),
:deep(.river-head-dialog .el-dialog__footer) {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 20px;
}

:deep(.river-chief-drawer .el-drawer) {
  background: linear-gradient(180deg, #f8fbff 0%, #f1f6fd 100%);
}

:deep(.river-chief-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 18px 20px 10px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

:deep(.river-chief-drawer .el-drawer__title) {
  color: #1e3a8a;
  font-weight: 700;
}

:deep(.river-chief-drawer .el-drawer__body) {
  padding: 18px 20px 24px;
}

@media (max-width: 768px) {
  .query-form :deep(.el-input),
  .query-form :deep(.el-select) {
    width: 100% !important;
  }

  .query-single-checkbox {
    width: 100%;
  }

  .query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .double-input {
    grid-template-columns: 1fr;
  }

  .chief-overview__stats {
    grid-template-columns: 1fr;
  }
}
.import-uploader :deep(.el-upload-dragger) {
  width: 100%;
}

.import-uploader :deep(.el-upload__tip) {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  color: #64748b;
}
</style>
