<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="水库名称" prop="reservoirName">
          <el-input
            v-model="queryParams.reservoirName"
            placeholder="请输入水库名称关键词"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="规模" prop="reservoirScale">
          <el-select v-model="queryParams.reservoirScale" placeholder="请选择规模" clearable class="!w-240px">
            <el-option v-for="item in reservoirScaleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理单位" prop="managementUnit">
          <el-select v-model="queryParams.managementUnit" placeholder="请选择管理单位" clearable class="!w-240px">
            <el-option v-for="item in managementUnitOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="名称" align="center" min-width="160" prop="reservoirName" show-overflow-tooltip />
        <el-table-column label="所在乡镇" align="center" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTownship(row) }}
          </template>
        </el-table-column>
        <el-table-column label="集水面积(km²)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.catchmentArea) }}
          </template>
        </el-table-column>
        <el-table-column label="总库容(万m³)" align="center" min-width="140">
          <template #default="{ row }">
            {{ formatNumber(row.totalCapacity) }}
          </template>
        </el-table-column>
        <el-table-column label="兴利库容(万m³)" align="center" min-width="140">
          <template #default="{ row }">
            {{ formatNumber(row.activeCapacity) }}
          </template>
        </el-table-column>
        <el-table-column label="兴利水位(m)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.normalOperatingLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="汛限水位(m)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.floodLimitLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="设计水位(m)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.designFloodLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="校核水位(m)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.verifiedFloodLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="坝顶高程(m)" align="center" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.damCrestElevation || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="最大坝高(m)" align="center" min-width="130">
          <template #default="{ row }">
            {{ formatNumber(row.maxDamHeight) }}
          </template>
        </el-table-column>
        <el-table-column label="坝顶长度(m)" align="center" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.damTopLength || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="360" fixed="right" class-name="river-table__action-column">
          <template #default="{ row }">
            <div class="river-table__action-cell">
              <el-button link type="success" @click="handleChiefOverview(row)">河长</el-button>
              <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
              <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              <el-button link type="primary" @click="handleJump(row)">跳转</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
          <div class="chief-overview__title">{{ chiefOverviewData?.reservoirName || chiefDrawerReservoirName || '-' }}</div>
          <div class="chief-overview__stats chief-overview__stats--two">
            <div class="chief-overview__stat">
              <span class="chief-overview__stat-value">{{ chiefOverviewData?.totalCount || 0 }}</span>
              <span class="chief-overview__stat-label">总人数</span>
            </div>
            <div class="chief-overview__stat">
              <span class="chief-overview__stat-value">{{ chiefOverviewData?.reservoirChiefs?.length || 0 }}</span>
              <span class="chief-overview__stat-label">水库直属</span>
            </div>
          </div>
        </div>

        <section v-if="hasChiefOverviewData" class="chief-block">
          <div class="chief-block__header">
            <div class="chief-block__title">
              <span class="chief-block__badge chief-block__badge--reservoir">水库</span>
              <span>水库河长</span>
            </div>
            <span class="chief-block__count">{{ chiefOverviewData?.reservoirChiefs?.length || 0 }} 人</span>
          </div>
          <div class="chief-grid">
            <article
              v-for="chief in chiefOverviewData?.reservoirChiefs"
              :key="`reservoir-${chief.id || chief.headName}`"
              class="chief-card chief-card--reservoir"
            >
              <div class="chief-card__top">
                <div>
                  <div class="chief-card__name">{{ chief.headName || '-' }}</div>
                  <div class="chief-card__scope">归属：当前水库</div>
                </div>
                <el-tag type="warning" effect="plain" round>
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
        <el-empty v-else-if="!chiefDrawerLoading" description="当前水库暂无有效河长信息" :image-size="88" />
      </div>
    </el-drawer>

    <el-dialog class="facility-dialog" v-model="createDialogVisible" :title="dialogTitle" width="1100px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="110px"
        label-position="left"
        :disabled="formMode === 'view'"
      >
        <div class="group-title">基础信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="水库名称" prop="reservoirName">
              <el-input v-model="createForm.reservoirName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规模" prop="reservoirScale">
              <el-select v-model="createForm.reservoirScale" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in reservoirScaleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理单位" prop="managementUnit">
              <el-select
                v-model="createForm.managementUnit"
                multiple
                collapse-tags
                :max-collapse-tags="6"
                clearable
                class="select-long"
                placeholder="可多选"
              >
                <el-option v-for="item in managementUnitOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="水库性质" prop="reservoirNature">
              <el-select v-model="createForm.reservoirNature" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in reservoirNatureOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="河长职责" prop="responsibilities">
              <el-input v-model="createForm.responsibilities" type="textarea" :rows="2" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总库容(万m³)" prop="totalCapacity">
              <el-input-number v-model="createForm.totalCapacity" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="兴利库容(万m³)" prop="activeCapacity">
              <el-input-number v-model="createForm.activeCapacity" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="死水位(m)" prop="deadLevel">
              <el-input-number v-model="createForm.deadLevel" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="group-title">水库位置</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所在乡镇" prop="township">
              <el-tree-select
                v-model="createForm.township"
                multiple
                show-checkbox
                :data="townshipTreeData"
                :props="townshipTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterTownshipNode"
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                placeholder="请选择（可搜索）"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在地点" prop="location">
              <el-input v-model="createForm.location" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="createForm.longitude" :precision="6" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="createForm.latitude" :precision="6" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="section-title">水库位置</div>
            <TiandituGeoJsonPreview
              v-if="formMode === 'view'"
              :geo-json="createForm.geometryGeoJson || ''"
              :active="createDialogVisible"
              :height="360"
              :center="reservoirMapCenter"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="createForm.geometryGeoJson"
                :active="createDialogVisible"
                :height="420"
                :center="reservoirMapCenter"
              />
              <div class="map-tip">支持缩放、全屏、绘制点/线/面，保存后自动入库。</div>
            </div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主管部门" prop="supervisingDepartment">
              <el-input v-model="createForm.supervisingDepartment" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="灌溉面积(亩)" prop="irrigationArea">
              <el-input-number v-model="createForm.irrigationArea" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设计灌溉面积(亩)" prop="designIrrigationArea">
              <el-input-number
                v-model="createForm.designIrrigationArea"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际灌溉面积(亩)" prop="actualIrrigationArea">
              <el-input v-model="createForm.actualIrrigationArea" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保护面积(亩)" prop="protectionArea">
              <el-input-number v-model="createForm.protectionArea" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下游主要设施" prop="downstreamFacilities">
              <el-input v-model="createForm.downstreamFacilities" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供水对象" prop="waterSupplyTarget">
              <el-input v-model="createForm.waterSupplyTarget" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="集水面积(km2)" prop="catchmentArea">
              <el-input-number v-model="createForm.catchmentArea" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高程基准面" prop="elevationDatum">
              <el-input v-model="createForm.elevationDatum" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抗震烈度" prop="seismicIntensity">
              <el-input v-model="createForm.seismicIntensity" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竣工日期" prop="completionDate">
              <el-date-picker
                v-model="createForm.completionDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="除险加固开工" prop="reinforcementStartDate">
              <el-date-picker
                v-model="createForm.reinforcementStartDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="除险加固竣工" prop="reinforcementEndDate">
              <el-date-picker
                v-model="createForm.reinforcementEndDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="除险加固日期" prop="reinforcementDate">
              <el-date-picker
                v-model="createForm.reinforcementDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="水库照片" prop="reservoirPhotos">
              <UploadImgs v-model="createForm.reservoirPhotos" :limit="6" :file-size="10" :drag="false" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="group-title">防洪与库容水位</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="设计洪水标准" prop="designFloodStandard">
              <el-input v-model="createForm.designFloodStandard" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="校核洪水标准" prop="verifiedFloodStandard">
              <el-input v-model="createForm.verifiedFloodStandard" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重现期设计(年)" prop="designReturnPeriod">
              <el-input-number v-model="createForm.designReturnPeriod" :min="0" :precision="0" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重现期校核(年)" prop="checkReturnPeriod">
              <el-input-number v-model="createForm.checkReturnPeriod" :min="0" :precision="0" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调洪库容(万m³)" prop="floodControlCapacity">
              <el-input-number
                v-model="createForm.floodControlCapacity"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="死库容(万m³)" prop="deadCapacity">
              <el-input-number v-model="createForm.deadCapacity" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="校核水位(m)" prop="verifiedFloodLevel">
              <el-input-number v-model="createForm.verifiedFloodLevel" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设计水位(m)" prop="designFloodLevel">
              <el-input-number v-model="createForm.designFloodLevel" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="兴利水位(m)" prop="normalOperatingLevel">
              <el-input-number
                v-model="createForm.normalOperatingLevel"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="汛限水位(m)" prop="floodLimitLevel">
              <el-input-number v-model="createForm.floodLimitLevel" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="group-title">大坝信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="坝顶高程(m)" prop="damCrestElevation">
              <el-input v-model="createForm.damCrestElevation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="坝顶宽度(m)" prop="damTopWidth">
              <el-input v-model="createForm.damTopWidth" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="坝顶高度(m)" prop="damTopHeight">
              <el-input-number v-model="createForm.damTopHeight" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大坝高(m)" prop="maxDamHeight">
              <el-input-number v-model="createForm.maxDamHeight" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="坝顶长度(m)" prop="damTopLength">
              <el-input v-model="createForm.damTopLength" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="挡浪墙顶高程(m)" prop="waveWallCrestElevation">
              <el-input-number
                v-model="createForm.waveWallCrestElevation"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路面结构型式" prop="damRoadSurfaceType">
              <el-input v-model="createForm.damRoadSurfaceType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防渗结构型式" prop="seepageControlType">
              <el-input v-model="createForm.seepageControlType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防渗起止桩号" prop="seepagePileRange">
              <el-input v-model="createForm.seepagePileRange" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防渗起止高程" prop="seepageElevRange">
              <el-input v-model="createForm.seepageElevRange" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迎水坡型式" prop="upstreamSlopeType">
              <el-input v-model="createForm.upstreamSlopeType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迎水坡起止高程" prop="upstreamSlopeElevation">
              <el-input v-model="createForm.upstreamSlopeElevation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迎水坡坡比" prop="upstreamSlopeRatio">
              <el-input v-model="createForm.upstreamSlopeRatio" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="背水坡坡比" prop="downstreamSlopeRatio">
              <el-input v-model="createForm.downstreamSlopeRatio" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护坡结构型式" prop="slopeProtectionType">
              <el-input v-model="createForm.slopeProtectionType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护坡起止高程" prop="slopeProtectionElevRange">
              <el-input v-model="createForm.slopeProtectionElevRange" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="背水坡高程(m)" prop="downstreamSlopeElevation">
              <el-input-number
                v-model="createForm.downstreamSlopeElevation"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="背水坡地宽(m)" prop="downstreamSlopeWidth">
              <el-input-number v-model="createForm.downstreamSlopeWidth" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="group-title">溢洪道与排洪</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="溢洪道型式" prop="spillwayType">
              <el-input v-model="createForm.spillwayType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="控制方式" prop="spillwayControlType">
              <el-input v-model="createForm.spillwayControlType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有无交通桥" prop="spillwayHasBridge">
              <el-radio-group v-model="createForm.spillwayHasBridge">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堰顶高程(m)" prop="spillwayCrestElevation">
              <el-input-number
                v-model="createForm.spillwayCrestElevation"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="底高程(m)" prop="spillwayBottomElevation">
              <el-input-number
                v-model="createForm.spillwayBottomElevation"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="底宽(m)" prop="spillwayBottomWidth">
              <el-input v-model="createForm.spillwayBottomWidth" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大流量(m3/s)" prop="spillwayMaxDischarge">
              <el-input-number
                v-model="createForm.spillwayMaxDischarge"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排洪河道名称" prop="floodChannelName">
              <el-input v-model="createForm.floodChannelName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全泄量(m3/s)" prop="floodChannelSafeDischarge">
              <el-input v-model="createForm.floodChannelSafeDischarge" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="group-title">涵洞与供水</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="涵洞型式" prop="culvertType">
              <el-input v-model="createForm.culvertType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="断面尺寸(m)" prop="culvertSectionSize">
              <el-input v-model="createForm.culvertSectionSize" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="闸门型式" prop="culvertGateType">
              <el-input v-model="createForm.culvertGateType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设计流量(m3/s)" prop="culvertDesignDischarge">
              <el-input-number
                v-model="createForm.culvertDesignDischarge"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出口底高程(m)" prop="culvertExitElevation">
              <el-input-number
                v-model="createForm.culvertExitElevation"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="涵洞直径(m)" prop="culvertDiameter">
              <el-input-number v-model="createForm.culvertDiameter" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="涵洞高度(m)" prop="culvertHeight">
              <el-input-number v-model="createForm.culvertHeight" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年供水量(万m3)" prop="annualWaterSupply">
              <el-input-number v-model="createForm.annualWaterSupply" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="宜鱼面积(亩)" prop="fisheryArea">
              <el-input-number v-model="createForm.fisheryArea" :min="0" :precision="2" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否水源地" prop="waterSource">
              <el-radio-group v-model="createForm.waterSource">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="跳转链接" prop="jumpUrl">
              <el-input v-model="createForm.jumpUrl" placeholder="请输入外链地址，如 http://..." clearable />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remarks">
              <el-input v-model="createForm.remarks" type="textarea" :rows="3" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">{{ formMode === 'view' ? '关闭' : '取消' }}</el-button>
        <el-button v-if="formMode !== 'view'" type="primary" :loading="createSubmitting" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      class="facility-dialog"
      v-model="headDialogVisible"
      title="河长信息"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form label-width="100px" label-position="left">
        <el-form-item label="水库名称">
          <el-input v-model="headForm.reservoirName" disabled />
        </el-form-item>
        <div v-for="(head, hIdx) in headForm.heads" :key="hIdx" class="head-card">
          <div class="segment-title head-title">
            <span>河长 {{ hIdx + 1 }}</span>
            <el-button
              v-if="!headReadonly && headForm.heads.length > 1"
              type="danger"
              link
              @click="removeHead(hIdx)"
            >
              删除河长
            </el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="河长级别" required>
                <el-select v-model="head.headLevel" :disabled="headReadonly" placeholder="请选择" clearable class="select-long">
                  <el-option v-for="opt in headLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="河长职务">
                <el-input v-model="head.headPosition" :disabled="headReadonly" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工作单位">
                <el-input v-model="head.headUnit" :disabled="headReadonly" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="河长姓名" required>
                <el-input v-model="head.headName" :disabled="headReadonly" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="行政区划">
                <el-tree-select
                  v-model="head.administrativeRegion"
                  multiple
                  show-checkbox
                  :data="townshipTreeData"
                  :props="townshipTreeProps"
                  node-key="value"
                  check-strictly
                  filterable
                  :filter-node-method="filterTownshipNode"
                  clearable
                  collapse-tags
                  collapse-tags-tooltip
                  :max-collapse-tags="6"
                  placeholder="请选择（可搜索）"
                  class="select-long"
                  :disabled="headReadonly"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-button v-if="!headReadonly" type="primary" link @click="addHead">新增河长</el-button>
      </el-form>
      <template #footer>
        <el-button @click="headDialogVisible = false">取消</el-button>
        <el-button v-if="!headReadonly" type="primary" :loading="headSubmitting" @click="submitHead">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入水库" width="520px">
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
import download from '@/utils/download'
import { getRiverDict, type DictDataItemRespVO } from '@/api/gis/riverChannel'
import { getAreaTree } from '@/api/system/area'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import {
  createReservoir,
  deleteReservoir,
  exportReservoirExcel,
  getReservoirChiefOverview,
  getReservoirImportTemplate,
  getReservoirManagement,
  getReservoirDetail,
  getReservoirPage,
  importReservoirExcel,
  saveReservoirManagement,
  type ReservoirChiefOverviewRespVO,
  updateReservoir,
  type ReservoirHeadItemReqVO,
  type ReservoirPageReqVO,
  type ReservoirPageRespVO,
  type ReservoirSaveReqVO
} from '@/api/gis/reservoir'

const queryParams = reactive<ReservoirPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  reservoirName: '',
  reservoirScale: '',
  managementUnit: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<ReservoirPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)
const chiefDrawerVisible = ref(false)
const chiefDrawerLoading = ref(false)
const chiefDrawerReservoirName = ref('')
const chiefOverviewData = ref<ReservoirChiefOverviewRespVO>()
const hasChiefOverviewData = computed(() => (chiefOverviewData.value?.reservoirChiefs?.length || 0) > 0)

const reservoirScaleOptions = ref<DictDataItemRespVO[]>([])
const managementUnitOptions = ref<DictDataItemRespVO[]>([])
const reservoirNatureOptions = ref<DictDataItemRespVO[]>([])
const headLevelOptions = ref<DictDataItemRespVO[]>([])

type AreaTreeNode = {
  name: string
  id: number
  children?: AreaTreeNode[]
}

type AreaTreeSelectNode = {
  label: string
  value: string
  children?: AreaTreeSelectNode[]
}

type ReservoirHeadItem = ReservoirHeadItemReqVO

const headDialogVisible = ref(false)
const headSubmitting = ref(false)
const headDialogMode = ref<'draft' | 'edit' | 'view'>('edit')
const headReadonly = computed(() => headDialogMode.value === 'view')
const draftHeads = ref<ReservoirHeadItem[]>([])
const headForm = reactive<{
  reservoirId?: string
  reservoirName?: string
  heads: ReservoirHeadItem[]
}>({
  reservoirId: undefined,
  reservoirName: '',
  heads: []
})

const townshipTreeData = ref<AreaTreeSelectNode[]>([])
const townshipTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}
const townshipNameMap = ref<Record<string, string>>({})

const filterTownshipNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
}

const loadAreaTree = async () => {
  const res = (await getAreaTree()) as any
  const data = (res && (res.data || res)) as AreaTreeNode[]
  const map: Record<string, string> = {}

  const build = (nodes?: AreaTreeNode[]): AreaTreeSelectNode[] => {
    if (!nodes || !nodes.length) return []
    return nodes
      .filter((n) => !!n)
      .map((node) => {
        const value = String(node.id)
        map[value] = node.name
        return {
          label: node.name,
          value,
          children: build(node.children)
        }
      })
  }

  townshipTreeData.value = build(data || [])
  townshipNameMap.value = map
}

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

const formatTownship = (row: ReservoirPageRespVO) => {
  const townshipName = (row as any)?.townshipName
  if (typeof townshipName === 'string' && townshipName.trim()) {
    return townshipName.trim()
  }

  const raw = (row as any)?.township
  if (!raw) return '-'

  let ids: string[] = []
  if (Array.isArray(raw)) {
    ids = raw.map((v) => String(v || '').trim()).filter((v) => !!v)
  } else if (typeof raw === 'string') {
    const text = raw.trim()
    if (!text) return '-'
    if ((text.startsWith('[') && text.endsWith(']')) || (text.startsWith('{') && text.endsWith('}'))) {
      const inner = text.substring(1, text.length - 1)
      ids = inner
        .split(/[,，、;；]/)
        .map((v) => v.trim().replace(/^['"]|['"]$/g, ''))
        .filter((v) => !!v)
    } else {
      ids = [text]
    }
  }

  if (!ids.length) return '-'
  const labels = ids.map((id) => townshipNameMap.value[id] || id).filter((v) => !!v)
  return labels.length ? Array.from(new Set(labels)).join('、') : '-'
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getReservoirPage(queryParams)
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

const handleExport = async () => {
  try {
    const data = await exportReservoirExcel(queryParams)
    download.excel(data, '水库信息.xls')
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
  const data = await getReservoirImportTemplate()
  download.excel(data, '水库导入模板.xls')
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
    const result: any = await importReservoirExcel(formData)
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item: string) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    await fetchTable()
  } finally {
    importLoading.value = false
  }
}

const handleDetail = (row: ReservoirPageRespVO) => {
  if (!row?.id) return
  openEditDialog(String(row.id), 'view')
}

const handleChiefOverview = async (row: ReservoirPageRespVO) => {
  if (!row?.id) return
  chiefDrawerVisible.value = true
  chiefDrawerLoading.value = true
  chiefDrawerReservoirName.value = row.reservoirName || row.reservoirCode || ''
  chiefOverviewData.value = undefined
  try {
    chiefOverviewData.value = await getReservoirChiefOverview(row.id)
  } finally {
    chiefDrawerLoading.value = false
  }
}

const handleEdit = (row: ReservoirPageRespVO) => {
  if (!row?.id) return
  openEditDialog(String(row.id), 'edit')
}

const handleJump = (row: ReservoirPageRespVO) => {
  const url = String(row?.jumpUrl ?? '').trim()
  if (!url) {
    ElMessage.info('当前水库暂未配置外链跳转')
    return
  }
  ElMessage.info('将在新窗口打开目标页面')
  window.open(url, '_blank', 'noopener,noreferrer')
}

const handleDelete = async (row: ReservoirPageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除水库「${row.reservoirName || row.reservoirCode || ''}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteReservoir(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()

type FormMode = 'create' | 'edit' | 'view'
const formMode = ref<FormMode>('create')

const dialogTitle = computed(() => {
  if (formMode.value === 'create') return '新增水库'
  if (formMode.value === 'edit') return '编辑水库'
  return '水库详情'
})

// 新增水库时地图中心点：经度 119.184766、纬度 32.272258（Leaflet 坐标顺序为 [纬度, 经度]）
const reservoirMapCenter = [32.272258, 119.184766] as [number, number]

const buildCreateForm = (): ReservoirSaveReqVO => ({
  id: undefined,
  facilityId: undefined,
  geometryGeoJson: '',
  geomType: '',
  srid: 4490,
  reservoirCode: '',
  reservoirName: '',
  reservoirScale: '',
  township: [],
  townshipName: '',
  location: '',
  managementUnit: [],
  reservoirPhotos: [],
  longitude: undefined,
  latitude: undefined,
  supervisingDepartment: '',
  reservoirNature: '',
  irrigationArea: undefined,
  designIrrigationArea: undefined,
  actualIrrigationArea: '',
  protectionArea: undefined,
  downstreamFacilities: '',
  waterSupplyTarget: '',
  catchmentArea: undefined,
  elevationDatum: '',
  seismicIntensity: '',
  completionDate: '',
  reinforcementStartDate: '',
  reinforcementEndDate: '',
  reinforcementDate: '',
  designFloodStandard: '',
  verifiedFloodStandard: '',
  designReturnPeriod: undefined,
  checkReturnPeriod: undefined,
  totalCapacity: undefined,
  activeCapacity: undefined,
  floodControlCapacity: undefined,
  deadCapacity: undefined,
  verifiedFloodLevel: undefined,
  designFloodLevel: undefined,
  normalOperatingLevel: undefined,
  floodLimitLevel: undefined,
  deadLevel: undefined,
  damCrestElevation: '',
  damTopWidth: '',
  damTopHeight: undefined,
  maxDamHeight: undefined,
  damTopLength: '',
  waveWallCrestElevation: undefined,
  damRoadSurfaceType: '',
  seepageControlType: '',
  seepagePileRange: '',
  seepageElevRange: '',
  upstreamSlopeType: '',
  upstreamSlopeElevation: '',
  upstreamSlopeRatio: '',
  downstreamSlopeRatio: '',
  slopeProtectionType: '',
  slopeProtectionElevRange: '',
  downstreamSlopeElevation: undefined,
  downstreamSlopeWidth: undefined,
  spillwayType: '',
  spillwayControlType: '',
  spillwayHasBridge: undefined,
  spillwayCrestElevation: undefined,
  spillwayBottomElevation: undefined,
  spillwayBottomWidth: '',
  spillwayMaxDischarge: undefined,
  floodChannelName: '',
  floodChannelSafeDischarge: '',
  culvertType: '',
  culvertSectionSize: '',
  culvertGateType: '',
  culvertDesignDischarge: undefined,
  culvertExitElevation: undefined,
  culvertDiameter: undefined,
  culvertHeight: undefined,
  annualWaterSupply: undefined,
  fisheryArea: undefined,
  waterSource: undefined,
  responsibilities: '',
  remarks: '',
  jumpUrl: ''
})

const createForm = reactive<ReservoirSaveReqVO>(buildCreateForm())
const originalResponsibilities = ref('')

const buildEmptyHead = (): ReservoirHeadItem => ({
  headLevel: '',
  headPosition: '',
  headUnit: '',
  headName: '',
  administrativeRegion: [],
  remarks: ''
})

const resetDraftHeads = () => {
  draftHeads.value = [buildEmptyHead()]
}
resetDraftHeads()

const cloneHeads = (heads: ReservoirHeadItem[]) => (heads || []).map((h) => ({ ...buildEmptyHead(), ...(h || {}) }))

const normalizeHeads = (heads: ReservoirHeadItem[]) => {
  const list = cloneHeads(heads || [])
  return list.length ? list : [buildEmptyHead()]
}

const isEmptyHead = (head: ReservoirHeadItem) => {
  const h = head || ({} as ReservoirHeadItem)
  return !(
    (h.headLevel || '').trim() ||
    (h.headPosition || '').trim() ||
    (h.headUnit || '').trim() ||
    (h.headName || '').trim()
  )
}

const loadReservoirHeads = async (reservoirId: string) => {
  const list = await getReservoirManagement(reservoirId)
  const normalized = normalizeHeads(list || [])
  headForm.heads = normalized.map((h) => {
    const { responsibilities, ...rest } = h || {}
    return { ...rest, remarks: '' }
  })
}

const addHead = () => {
  headForm.heads.push(buildEmptyHead())
}

const removeHead = (index: number) => {
  if (headForm.heads.length <= 1) return
  headForm.heads.splice(index, 1)
}

const openHeadDialog = async (mode: 'draft' | 'edit' | 'view', reservoirId?: string, reservoirName?: string) => {
  headDialogMode.value = mode
  headForm.reservoirId = reservoirId
  headForm.reservoirName = reservoirName || ''
  if (mode === 'draft') {
    headForm.heads = normalizeHeads(draftHeads.value)
  } else if (reservoirId) {
    await loadReservoirHeads(reservoirId)
  } else {
    headForm.heads = [buildEmptyHead()]
  }
  headDialogVisible.value = true
}

const submitHead = async () => {
  if (headReadonly.value) {
    headDialogVisible.value = false
    return
  }
  const nonEmptyHeads = (headForm.heads || []).filter((h) => !isEmptyHead(h))
  if (!nonEmptyHeads.length) {
    ElMessage.error('请至少填写一条河长信息')
    return
  }
  for (let i = 0; i < nonEmptyHeads.length; i++) {
    const head = nonEmptyHeads[i]
    if (!(head.headLevel || '').trim()) {
      ElMessage.error(`第 ${i + 1} 个河长：河长级别不能为空`)
      return
    }
    if (!(head.headName || '').trim()) {
      ElMessage.error(`第 ${i + 1} 个河长：河长姓名不能为空`)
      return
    }
  }
  if (headDialogMode.value === 'draft') {
    draftHeads.value = normalizeHeads(nonEmptyHeads)
    headDialogVisible.value = false
    return
  }
  const reservoirId = (headForm.reservoirId || '').trim()
  if (!reservoirId) {
    ElMessage.warning('请先保存水库后再保存河长信息')
    headDialogVisible.value = false
    return
  }
  const heads = nonEmptyHeads.map(({ responsibilities, ...rest }) => ({
    ...rest,
    // 备注不再维护，统一不提交
    remarks: undefined
  }))
  headSubmitting.value = true
  try {
    await saveReservoirManagement({
      waterReservoirId: reservoirId,
      referenceId: reservoirId,
      referenceType: 'reservoir',
      heads
    })
    ElMessage.success('河长信息保存成功')
    headDialogVisible.value = false
  } finally {
    headSubmitting.value = false
  }
}

watch(
  () => createForm.township,
  (val) => {
    if (!val || !val.length) {
      createForm.townshipName = ''
      return
    }
    const labels = val
      .filter((v) => !!v)
      .map((v) => townshipNameMap.value[v] || '')
      .filter((v) => !!v)
    createForm.townshipName = labels.length ? Array.from(new Set(labels)).join('、') : ''
  }
)

const requiredInput = (label: string) => [{ required: true, message: `请输入${label}`, trigger: 'blur' }]
const requiredSelect = (label: string) => [{ required: true, message: `请选择${label}`, trigger: 'change' }]

const createRules: FormRules = {
  reservoirName: requiredInput('水库名称'),
  reservoirScale: requiredSelect('规模')
}

const resetCreateForm = () => {
  Object.assign(createForm, buildCreateForm())
  originalResponsibilities.value = ''
  createFormRef.value?.clearValidate()
  resetDraftHeads()
}

const openCreateDialog = () => {
  resetCreateForm()
  formMode.value = 'create'
  createDialogVisible.value = true
}

const openEditDialog = async (id: string, mode: Exclude<FormMode, 'create'>) => {
  resetCreateForm()
  formMode.value = mode
  createDialogVisible.value = true
  try {
    const detail = await getReservoirDetail(id)
    Object.assign(createForm, buildCreateForm(), detail || {})
    createForm.srid = createForm.srid || 4490
    const townshipVal = (createForm as any).township
    if (typeof townshipVal === 'string') {
      createForm.township = townshipVal ? [townshipVal] : []
    } else if (!Array.isArray(townshipVal)) {
      createForm.township = []
    }
    originalResponsibilities.value = createForm.responsibilities || ''
  } catch {
    ElMessage.error('获取水库详情失败')
    createDialogVisible.value = false
  }
}

const submitForm = async () => {
  const form = createFormRef.value
  if (!form) return
  if (createForm.township && createForm.township.length) {
    const missing = createForm.township.filter((id) => !!id && !townshipNameMap.value[id])
    if (missing.length) {
      ElMessage.warning('乡镇名称获取失败，请刷新页面后重试')
      return
    }
  }
  if (!createForm.township || !createForm.township.length) {
    createForm.townshipName = ''
  } else {
    const labels = createForm.township
      .filter((v) => !!v)
      .map((v) => townshipNameMap.value[v] || '')
      .filter((v) => !!v)
    createForm.townshipName = labels.length ? Array.from(new Set(labels)).join('、') : ''
  }
  await form.validate()
  createSubmitting.value = true
  try {
    if (formMode.value === 'create') {
      const reservoirId = await createReservoir(createForm)
      createForm.id = reservoirId ? String(reservoirId) : undefined
      ElMessage.success('新增成功')
    } else {
      await updateReservoir(createForm)
      ElMessage.success('保存成功')
    }
    createDialogVisible.value = false
    fetchTable()
  } finally {
    createSubmitting.value = false
  }
}

const loadDict = async () => {
  const [scale, managementUnit, nature, headLevels] = await Promise.all([
    getRiverDict('zd_skgm'),
    getRiverDict('zd_gldw'),
    getRiverDict('zd_skxz'),
    getRiverDict('zd_hzjb')
  ])
  reservoirScaleOptions.value = scale || []
  managementUnitOptions.value = managementUnit || []
  reservoirNatureOptions.value = nature || []
  headLevelOptions.value = headLevels || []
}

onMounted(async () => {
  await Promise.all([loadDict(), loadAreaTree()])
  fetchTable()
})
</script>

<style scoped>
.river-page {
  --river-bg: #f3f7fc;
  --river-card-bg: #ffffff;
  --river-border: rgba(148, 163, 184, 0.28);
  --river-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --river-primary: #1e40af;
  --river-primary-soft: rgba(30, 64, 175, 0.08);
  --river-row-hover-solid: #eef2ff;
  --river-text-main: #1f2a37;

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
.query-form :deep(.el-select__wrapper),
.query-form :deep(.el-tree-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.32) inset;
  transition: box-shadow 0.2s ease, background-color 0.2s ease;
}

.query-form :deep(.el-input__wrapper:hover),
.query-form :deep(.el-select__wrapper:hover),
.query-form :deep(.el-tree-select__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.42) inset;
}

.query-form :deep(.el-input__wrapper.is-focus),
.query-form :deep(.el-select__wrapper.is-focused),
.query-form :deep(.el-tree-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--river-primary) inset;
}

.query-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

.river-table {
  border-radius: 12px;
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

.river-table :deep(.el-table__fixed-right),
.river-table :deep(.el-table__fixed-right-patch),
.river-table :deep(.el-table__fixed-right .el-table__body-wrapper),
.river-table :deep(.el-table__fixed-right .el-table__fixed-body-wrapper) {
  background-color: #fff !important;
  z-index: 5;
}

.river-table :deep(.el-table__fixed-right .el-table__header-wrapper th.el-table__cell),
.river-table :deep(.el-table__header th.el-table-fixed-column--right) {
  background-color: #f5f8ff !important;
}

.river-table :deep(.el-table__fixed-right .el-table__body-wrapper td.el-table__cell),
.river-table :deep(td.el-table-fixed-column--right),
.river-table :deep(.river-table__action-column.el-table__cell) {
  background-color: #fff !important;
}

.river-table :deep(.el-table__fixed-right .el-table__body tr:hover > td.el-table__cell),
.river-table :deep(.el-table__body tr:hover > td.el-table-fixed-column--right),
.river-table :deep(.el-table__body tr:hover > .river-table__action-column.el-table__cell) {
  background-color: var(--river-row-hover-solid) !important;
}

.river-table :deep(.river-table__action-column .cell) {
  padding: 0 !important;
  overflow: hidden;
  background-color: #fff !important;
}

.river-table :deep(.el-table__body tr:hover .river-table__action-column .cell) {
  background-color: var(--river-row-hover-solid) !important;
}

.river-table__action-cell {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 100%;
  box-sizing: border-box;
  min-height: 40px;
  padding: 8px 10px;
  background-color: #fff;
  white-space: nowrap;
}

.river-table :deep(.el-table__body tr:hover .river-table__action-cell) {
  background-color: var(--river-row-hover-solid);
}

.table-wrap :deep(.el-pagination) {
  margin-top: 14px;
  justify-content: flex-end;
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

.chief-overview__stats--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.chief-block__badge--reservoir {
  color: #b45309;
  background: rgba(245, 158, 11, 0.14);
}

.chief-block__count {
  color: #64748b;
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

.chief-card--reservoir {
  border-color: rgba(245, 158, 11, 0.18);
}

.chief-card--reservoir:hover {
  border-color: rgba(245, 158, 11, 0.34);
  box-shadow: 0 14px 28px rgba(245, 158, 11, 0.12);
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
  color: #64748b;
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
  color: #64748b;
}

:deep(.facility-dialog .el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.2);
}

:deep(.facility-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(90deg, #f8fbff 0%, #f3f7ff 100%);
}

:deep(.facility-dialog .el-dialog__title) {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.facility-dialog .el-dialog__body) {
  padding: 16px 20px;
}

:deep(.facility-dialog .el-dialog__footer) {
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
  .query-form :deep(.el-select),
  .query-form :deep(.el-tree-select) {
    width: 100% !important;
  }

  .query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .chief-overview__stats,
  .chief-overview__stats--two {
    grid-template-columns: 1fr;
  }
}

.section-title {
  font-weight: 600;
  margin: 8px 0;
  color: #303133;
}

.group-title {
  font-weight: 600;
  margin: 10px 0 12px;
  padding: 6px 10px;
  color: #303133;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.map-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.head-card {
  margin: 12px 0;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
}

.segment-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}

.select-long {
  width: 100%;
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
