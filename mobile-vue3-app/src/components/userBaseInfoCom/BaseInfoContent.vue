<!-- 基础信息内容组件 -->
<template>
    <div class="base-info-content">
        <!-- 河道类型显示 -->
        <template v-if="!isReservoir">
            <!-- 当前河段：已隐藏，不再展示 -->
            <!--
            <template v-if="baseInfoData && baseInfoData.riverSections && baseInfoData.riverSections.length > 0">
                <span class="contentTit" style="color: #3D3D3D;">当前河段</span>
                <div class="base-info-content-item1">
                    <template v-for="(item, index) in currentRiver" :key="item.label">
                        <div class="currentRiverItem">
                            <div class="currentItemIcon" :style="{ backgroundImage: `url(${item.icon})` }"></div>
                            <div class="currentRiverItemLabel">{{ item.label }}</div>
                            <el-tooltip :content="item.value" placement="top" v-if="item.value && item.value.length > 5">
                                <div class="currentRiverItemValue">{{ item.value }} {{ item.unit }}</div>
                            </el-tooltip>
                            <div class="currentRiverItemValue" v-else>{{ item.value }}{{ item.unit }}</div>
                        </div>
                        <div v-if="index < currentRiver.length - 1" class="divider"></div>
                    </template>
                </div>
                <div class="divingLine"></div>
            </template>
            -->

            <!-- 当前河道 -->
            <span class="contentTit" style="color: #3D3D3D;">当前河道</span>
            <div class="current-channel-row">
                <span class="current-channel-label">河道名称</span>
                <span class="current-channel-value">{{ currentChannelName }}</span>
            </div>
            <div class="divingLine"></div>

            <span class="contentTit" style="color: #3D3D3D;">河道信息</span>

            <!-- <div class='base-info-content-item2'>
                <div class="riverInfo" v-for="item in riverInfo" :key="item.label"
                    :style="{ backgroundImage: `url(${item.imgUrl})` }">
                    <div class="riverInfoLabel">{{ item.label }}</div>
                    <div class="riverInfoValue">{{ item.value }}{{ item.unit }}</div>
                </div>
            </div> -->

            <div class="base-info-content-item1 riverInfo2">
                <template v-for="(item, index) in riverInfo2" :key="item.label">
                    <div class="currentRiverItem">
                        <div class="currentItemIcon" :style="{ backgroundImage: `url(${item.icon})` }"></div>

                        <div class="currentRiverItemLabel">{{ item.label }}</div>
                        <el-tooltip :content="item.value" placement="top" v-if="item.value && item.value.length > 5">
                            <div class="currentRiverItemValue">{{ item.value }} {{ item.unit }}</div>
                        </el-tooltip>
                        <div class="currentRiverItemValue" v-else>{{ item.value }}{{ item.unit }}</div>

                    </div>
                    <!-- 分隔线，最后一个不显示 -->
                    <div v-if="index < riverInfo2.length - 1" class="divider"></div>
                </template>
            </div>

            <div class="riverInfo3">
                <div v-for="(item, index) in riverInfo3" :key="item.prop" class="riverInfo3-item"
                    :class="{ 'riverInfo3-item-gray': index % 2 === 1 }">
                    <div class="riverInfo3-label">{{ item.label }}</div>
                    <div class="riverInfo3-value">{{ item.value }}</div>
                </div>
            </div>
        </template>

        <!-- 水库类型显示 -->
        <template v-else>
            <div class="reservoir-header">
                <span class="contentTit" style="color: #3D3D3D;">水库信息</span>
                <span class="reservoir-management" v-if="baseInfoData && baseInfoData.waterReservoirs && baseInfoData.waterReservoirs[0] && baseInfoData.waterReservoirs[0].managementUnit && baseInfoData.waterReservoirs[0].managementUnit.length > 0">
                    管理单位: {{ baseInfoData.waterReservoirs[0].managementUnit.join('、') }}
                </span>
            </div>
 
            <!-- 水库信息：按数据动态分行，每行3列 -->
            <div class="reservoir-info-container">
                <div class="reservoir-info-row" v-for="(row, rowIndex) in reservoirInfoRows" :key="`row-${rowIndex}`">
                    <template v-for="(item, index) in row" :key="`row-${rowIndex}-item-${index}`">
                        <div class="reservoir-info-item">
                            <div class="reservoir-item-icon" :style="{ backgroundImage: `url(${item.icon})` }"></div>
                            <div class="reservoir-item-label">{{ item.label }}</div>
                            <el-tooltip :content="item.value + item.unit" placement="top" v-if="item.value && (item.value + item.unit).length > 8">
                                <div class="reservoir-item-value">{{ item.value }}{{ item.unit }}</div>
                            </el-tooltip>
                            <div class="reservoir-item-value" v-else>{{ item.value }}{{ item.unit }}</div>
                        </div>
                        <div v-if="index < row.length - 1" class="reservoir-divider"></div>
                    </template>
                </div>
            </div>

            <!-- 水库介绍 -->
            <!-- <div class="divingLine" style="margin-top: 24px;"></div> -->
            <span class="contentTit" style="color: #3D3D3D; margin-top: 8px; display: block;">水库介绍</span>
            <!-- 有数据时显示介绍内容 -->
            <div v-if="reservoirIntroduction && reservoirIntroduction !== '--'" class="reservoir-introduction">
                {{ reservoirIntroduction }}
            </div>
            <!-- 无数据时显示缺省图 -->
            <div v-else class="reservoir-introduction-empty">
                <div class="empty-placeholder">
                    <img :src="nodataImg" alt="暂无数据" class="nodata-img" />
                    <div class="nodata-text">——暂无数据——</div>
                </div>
            </div>
        </template>
    </div>
</template>

<script>
import riverNameIcon from '@/assets/img/riverNameIcon.png';
import riverLevelIcon from '@/assets/img/riverLevelIcon.png';
import riverLengthIcon from '@/assets/img/riverLengthIcon.png';
import riverStartEndIcon from '@/assets/img/riverEndStartIcon.png';
import riverInfoImg1 from '@/assets/img/riverInfoBck1.png';
import riverInfoImg2 from '@/assets/img/riverInfoBck2.png';
// 导入水库图标
import reservoirIcon1 from '@/assets/img/reservoir-icon1.png';
import reservoirIcon2 from '@/assets/img/reservoir-icon2.png';
import reservoirIcon3 from '@/assets/img/reservoir-icon3.png';
import reservoirIcon4 from '@/assets/img/reservoir-icon4.png';
import reservoirIcon5 from '@/assets/img/reservoir-icon5.png';
import reservoirIcon6 from '@/assets/img/reservoir-icon6.png';
import reservoirIcon7 from '@/assets/img/reservoir-icon7.png';
import reservoirIcon8 from '@/assets/img/reservoir-icon8.png';
import reservoirIcon9 from '@/assets/img/reservoir-icon9.png';
import reservoirIcon10 from '@/assets/img/reservoir-icon10.png';
import reservoirIcon11 from '@/assets/img/reservoir-icon11.png';
import reservoirIcon12 from '@/assets/img/reservoir-icon12.png';
// 引入 el-tooltip
import { ElTooltip } from 'element-plus';
// 引入河道级别组件
import RiverLevel from '@/components/userBaseInfoCom/RiverLevel.vue';
import nodataImg from '@/assets/img/nodataImg.png';

export default {
    name: 'BaseInfoContent',
    components: {
        RiverLevel
    },
    async mounted() {
        this.baseInfoData = await this.getBaseInfo()
        this.fillTemplateData()
    },
    data() {
        return {
            // 当前河段
            currentRiver: [
                { label: '河段名称', value: '--', unit: '', icon: riverNameIcon },
                { label: '河段起点', value: '--', unit: '', icon: riverStartEndIcon },
                { label: '河段终点', value: '--', unit: '', icon: riverStartEndIcon },
            ],
            //河道信息1
            riverInfo: [
                { label: '河道级别', value: '--', unit: '', imgUrl: riverInfoImg1 },
                { label: '生态类型', value: '--', unit: '', imgUrl: riverInfoImg2 },
            ],

            //河道信息 - 河道级别
            riberLevel: '--',
            //河道信息2（卡片：河道级别、河道长度）
            riverInfo2: [
                { label: '河道级别', value: '--', unit: '', icon: riverLevelIcon },
                { label: '河道长度', value: '--', unit: 'km', icon: riverLengthIcon },
            ],
            //河道信息3
            riverInfo3: [
                { label: '起点终点', prop: 'startEnd', value: '--' },
                { label: '所在流域', prop: 'riverBasin', value: '--' },
                { label: '河道功能', prop: 'riverType', value: '--' },
                { label: '河道概况', prop: 'riverOverview', value: '--' },
            ],

            baseInfoData: null,
            // 类型标识：true 为水库，false 为河道
            isReservoir: false,
            // 水库信息数据（12个字段，4行3列）
            reservoirInfo: [],
            // 水库介绍
            reservoirIntroduction: '',
            // 缺省图片
            nodataImg,
            // 当前河道名称（河道类型展示）
            currentChannelName: '--',

        }
    },
    computed: {

        //获取Qr
        getQrcode() {
            return this.$store.state.qrcode
        },
        // 水库信息按每行3个分组
        reservoirInfoRows() {
            const rows = []
            for (let i = 0; i < this.reservoirInfo.length; i += 3) {
                rows.push(this.reservoirInfo.slice(i, i + 3))
            }
            return rows
        }
    },
    methods: {
        //获取基础信息
        async getBaseInfo() {
            const response = await this.$http.get(`/app-api/signboard/info/by-qr/${this.getQrcode}`, {
            });
            return response.data;
        },

        //填充数据 - 根据 waterReservoirId 判断类型
        fillTemplateData() {
            if (!this.baseInfoData) return;

            // 根据 waterReservoirId 字段判断类型
            const { waterReservoirId } = this.baseInfoData;

            if (waterReservoirId) {
                // 有 waterReservoirId，走水库逻辑
                this.isReservoir = true;
                this.fillReservoirData();
            } else {
                // 没有 waterReservoirId，走河道逻辑
                this.isReservoir = false;
                this.fillRiverData();
            }
        },

        // 填充河道数据
        fillRiverData() {
            if (!this.baseInfoData.rivers || this.baseInfoData.rivers.length === 0) return;

            let riverInfo = this.baseInfoData.rivers[0].channel;

            console.log('处理河道数据:', riverInfo);

            // 当前河道名称（与下方河道信息中的 riverName 一致）
            this.currentChannelName = riverInfo.riverName || '--';

            // 0. 当前河段数据仍填充，便于日后恢复展示；模板已注释
            if (this.baseInfoData.riverSections && this.baseInfoData.riverSections.length > 0) {
                const riverSection = this.baseInfoData.riverSections[0];
                this.currentRiver = [
                    { label: '河段名称', value: riverSection.sectionName || '--', unit: '', icon: riverNameIcon },
                    { label: '河段起点', value: riverSection.startPoint || '--', unit: '', icon: riverStartEndIcon },
                    { label: '河段终点', value: riverSection.endPoint || '--', unit: '', icon: riverStartEndIcon },
                ];
            } else {
                this.currentRiver = [
                    { label: '河段名称', value: '--', unit: '', icon: riverNameIcon },
                    { label: '河段起点', value: '--', unit: '', icon: riverStartEndIcon },
                    { label: '河段终点', value: '--', unit: '', icon: riverStartEndIcon },
                ];
            }

            // 1. 更新河道信息2 (级别, 长度) - 直接重新赋值确保响应式
            this.riverInfo2 = [
                { label: '河道级别', value: riverInfo.riverLevelLabel || '--', unit: '', icon:riverLevelIcon },
                { label: '河道长度', value: riverInfo.lengthKm || '--', unit: 'km', icon: riverLengthIcon },
            ];

            // 2. 更新河道级别
            const levelLabel = riverInfo.riverLevelLabel || '--';
            this.riverInfo =[
                { label: '河道级别', value: levelLabel.substring(0, 2), unit: '', imgUrl: riverInfoImg1 },
                { label: '生态类型', value: riverInfo.ecologyTypeLabel || '--', unit: '', imgUrl: riverInfoImg2 },
            ]

            //2.1 河道右上角sign
            this.riberLevel = riverInfo.riverLevelLabel.substring(0, 2) || '--';
           

            // 3. 更新河道信息3 (列表展示)
            this.riverInfo3 = [
                { label: '起点终点', prop: 'startEnd', value: (riverInfo.startPoint && riverInfo.endPoint) ? `${riverInfo.startPoint}-${riverInfo.endPoint}` : '--' },
                { label: '所在流域', prop: 'riverBasin', value: riverInfo.basinTypeLabel || '--' },
                { label: '河道功能', prop: 'riverType', value: riverInfo.riverTypeLabel || '--' },
                { label: '河道概况', prop: 'remarks', value: riverInfo.remarks || '--' },
            ];
        },

        // 填充水库数据
        fillReservoirData() {
            // 使用 waterReservoirs 数组的第一个对象
            if (!this.baseInfoData.waterReservoirs || this.baseInfoData.waterReservoirs.length === 0) return;

            const reservoir = this.baseInfoData.waterReservoirs[0];

            console.log('处理水库数据:', reservoir);

            // 设计水位：无效值（null、undefined、空串、NaN、非数字串等）不展示该卡片
            const designLevelRaw = reservoir.designFloodLevel;
            let designLevelDisplay = '';
            let showDesignLevel = false;
            if (designLevelRaw !== null && designLevelRaw !== undefined) {
                if (typeof designLevelRaw === 'number' && Number.isFinite(designLevelRaw)) {
                    showDesignLevel = true;
                    designLevelDisplay = designLevelRaw.toString();
                } else if (typeof designLevelRaw === 'string') {
                    const s = designLevelRaw.trim();
                    if (s !== '' && !/^null$/i.test(s) && s !== '--') {
                        const n = parseFloat(s.replace(/,/g, ''));
                        if (Number.isFinite(n)) {
                            showDesignLevel = true;
                            designLevelDisplay = s;
                        }
                    }
                }
            }

            // 填充水库信息（精简字段）
            // 显示：名称、规模、总库容、兴利库容、[设计水位]、汛限水位
            const rows = [
                { label: '名称', value: reservoir.reservoirName || '--', unit: '', icon: reservoirIcon1 },
                { label: '规模', value: reservoir.reservoirScale || '--', unit: '', icon: reservoirIcon2 },
                { label: '总库容', value: reservoir.totalCapacity ? reservoir.totalCapacity.toString() : '--', unit: '万m³', icon: reservoirIcon4 },
                { label: '兴利库容', value: reservoir.activeCapacity ? reservoir.activeCapacity.toString() : '--', unit: '万m³', icon: reservoirIcon5 },
            ];
            if (showDesignLevel) {
                rows.push({
                    label: '设计水位',
                    value: designLevelDisplay,
                    unit: 'm',
                    icon: reservoirIcon7
                });
            }
            rows.push({
                label: '汛限水位',
                value: reservoir.floodLimitLevel ? reservoir.floodLimitLevel.toString() : '--',
                unit: 'm',
                icon: reservoirIcon8
            });
            this.reservoirInfo = rows;

            // 填充水库介绍（使用 remarks 字段）
            this.reservoirIntroduction = reservoir.remarks || '--';

            // 检查缺失的字段并提示
            const missingFields = [];
            if (!reservoir.reservoirName) missingFields.push('reservoirName');
            if (!reservoir.reservoirScale) missingFields.push('reservoirScale');
            if (!reservoir.reservoirNature) missingFields.push('reservoirNature');
            if (reservoir.totalCapacity === undefined || reservoir.totalCapacity === null) missingFields.push('totalCapacity');
            if (reservoir.activeCapacity === undefined || reservoir.activeCapacity === null) missingFields.push('activeCapacity');
            if (reservoir.floodControlCapacity === undefined || reservoir.floodControlCapacity === null) missingFields.push('floodControlCapacity');
            if (reservoir.normalOperatingLevel === undefined || reservoir.normalOperatingLevel === null) missingFields.push('normalOperatingLevel');
            if (reservoir.floodLimitLevel === undefined || reservoir.floodLimitLevel === null) missingFields.push('floodLimitLevel');
            if (reservoir.deadLevel === undefined || reservoir.deadLevel === null) missingFields.push('deadLevel');
            if (!reservoir.completionDate) missingFields.push('completionDate');
            if (!reservoir.reinforcementStartDate) missingFields.push('reinforcementStartDate');
            if (!reservoir.reinforcementEndDate) missingFields.push('reinforcementEndDate');
            if (!reservoir.remarks) missingFields.push('remarks');

            if (missingFields.length > 0) {
                console.warn('水库数据缺失以下字段:', missingFields.join(', '));
            }
        },
        // 后退按钮点击
        handlePrev() {
            console.log('后退');
            // 可以在这里添加后退逻辑，比如切换数据、路由跳转等
            this.$emit('prev');
        },
        // 前进按钮点击
        handleNext() {
            console.log('前进');
            // 可以在这里添加前进逻辑，比如切换数据、路由跳转等
            this.$emit('next');
        }
    }
}
</script>

<style lang="scss" scoped>
.base-info-content {

    .contentTit {
        font-size: 32px;
        color: #3D3D3D;
        font-weight: 600;
        display: block;
        margin-bottom: 8px; // 原 marT4 (4px) 翻倍
    }

    .current-channel-row {
        display: flex;
        align-items: center;
        width: 100%;
        min-height: 82px;
        margin-top: 8px;
        background:url('@/assets/img/currentChannelBck.png') no-repeat center center;
        background-size: 100% 100%;
        border-radius: 4px;
        padding: 16px 20px;
        box-sizing: border-box;

        .current-channel-label {
            flex-shrink: 0;
            font-size: 28px;
            color: #3D3D3D;
            font-weight: 600;
            margin-right: 16px;
        }

        .current-channel-value {
            flex: 1;
            font-size: 32px;
            color: #349DFF;
            font-weight: 600;
            word-break: break-all;
        }
    }

    .base-info-content-item1 {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 158px;
        background-color: #f8f8f8;
        padding: 20px 0px;
        margin-top: 8px; // 原 marT4 (4px) 翻倍

        // box-sizing: border-box;
        .currentRiverItem {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
            min-width: 0; // 确保 flex 子元素可以收缩
            max-width: 100%; // 限制最大宽度

            .currentItemIcon {
                width: 36px;
                height: 36px;
                background-size: 100% 100%;


            }

            .currentRiverItemLabel {
                margin: 8px 0px 4px 0px;
                color: #3D3D3D;
                font-weight: 600;
                font-size: 28px; // 原 fs14 (14px) 翻倍

            }

            .currentRiverItemValue {
                color: #349DFF;
                font-weight: 600;
                font-size: 32px; // 原 fs16 (16px) 翻倍
                // 超出容器文字隐藏
                width: 100%;
                max-width: 100%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }

        .divider {
            width: 2px;
            height: 44px;
            border-radius: 5px;
            opacity: 1;
            background: #dddddd;
            margin: 0 10px; // 减小间距以适应4个项目
        }
    }

    .base-info-content-item2 {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 120px;
        // 如果需要底部间距，应该设置在父容器上，而不是子元素
        margin-bottom: 16px;

        .riverInfo {
            width: 50%;
            height: 120px;
            background-size: 100% 100%;
            background-repeat: no-repeat;
            background-position: center center;
            padding: 24px 0px 0px 36px;

            .riverInfoLabel {
                font-size: 28px;
                color: #3D3D3D;
                font-weight: 600;
                margin-bottom: 4px;
            }

            .riverInfoValue {
                font-size: 32px;
                color: #349DFF;
                font-weight: 600;
            }
        }

        // 第一个riverInfo
        .riverInfo:first-child {
            padding: 24px 0px 0px 16px;

        }

    }

    .divingLine {
        width: 100%;
        height: 1px;
        background: #eeeeee;
        margin-top: 16px; // 原 marT8 (8px) 翻倍
        margin-bottom: 24px; // 原 marB12 (12px) 翻倍
    }

    .riverInfo3 {
        width: 100%;
        margin-top: 16px;
        background-color: #ffffff;
        border-radius: 4px;

        .riverInfo3-item {
            display: flex;
            align-items: stretch;
            padding: 0;
            background-color: #fafafa;

            &:last-child {
                border-bottom: none;
            }

            &.riverInfo3-item-gray {
                background-color: #f5f5f5;
            }

            .riverInfo3-label {
                min-width: 140px;
                font-size: 28px;
                color: #2c2c2c;
                font-weight: 600;
                line-height: 1.5;
                flex-shrink: 0;
                // 允许换行
                word-wrap: break-word;
                word-break: break-all;
                // 单独的色块背景，撑满整个高度
                background-color: #f3f3f3;
                padding: 16px;
                display: flex;
                align-items: center;
            }

            .riverInfo3-value {
                flex: 1;
                font-size: 28px;
                color: #4a4a4a;
                font-weight: 400;
                line-height: 1.5;
                // 允许换行
                word-wrap: break-word;
                word-break: break-all;
                padding: 16px 20px;
                display: flex;
                align-items: center;
            }
        }
    }

    // 水库相关样式
    .reservoir-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .reservoir-management {
            font-size: 24px;
            color: #666;
            font-weight: 400;
        }
    }

    .reservoir-info-container {
        margin-top: 16px;

        .reservoir-info-row {
            display: flex;
            align-items: center;
            justify-content: flex-start;
            width: 100%;
            height: 158px;
            background-color: #f8f8f8;
            padding: 20px 0px;
            margin-bottom: 16px;
            position: relative;

            &:last-child {
                margin-bottom: 0;
            }

            // 下分割线
            // &::after {
            //     content: '';
            //     position: absolute;
            //     bottom: 0;
            //     left: 50%;
            //     transform: translateX(-50%);
            //     width: 4px;
            //     height: 44px;
            //     border-radius: 5px;
            //     opacity: 1;
            //     background: #dddddd;
            // }

            &:last-child::after {
                display: none; // 最后一行不显示分割线
            }

            .reservoir-info-item {
                // 与满行 3 卡时同宽：两竖线 2×(2+10+10)=44px
                flex: 0 0 calc((100% - 44px) / 3);
                max-width: calc((100% - 44px) / 3);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;
                min-width: 0;

                .reservoir-item-icon {
                    width: 36px;
                    height: 32px;
                    background-size: contain;
                    background-repeat: no-repeat;
                    background-position: center;
                    margin-bottom: 8px;
                }

                .reservoir-item-label {
                    margin: 0px 0px 0px 0px;
                    color: #3D3D3D;
                    font-weight: 600;
                    font-size: 28px;
                }

                .reservoir-item-value {
                    color: #349DFF;
                    font-weight: 600;
                    font-size: 32px;
                    width: 100%;
                    max-width: 100%;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
            }

            .reservoir-divider {
                width: 2px;
                height: 44px;
                border-radius: 5px;
                opacity: 1;
                background: #dddddd;
                margin: 0 10px;
            }
        }
    }

    .reservoir-introduction {
        margin-top: 8px;
        padding: 20px;
        background-color: #f8f8f8;
        border-radius: 8px;
        font-size: 28px;
        color: #4a4a4a;
        line-height: 1.6;
        // 可视区域继续加高，超出再滚动
        min-height: 280px;
        max-height: 360px;
        overflow-y: auto;
        word-wrap: break-word;
        word-break: break-all;
    }

    // 水库介绍缺省图
    .reservoir-introduction-empty {
        margin-top: 8px;
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;

        .empty-placeholder {
            width: 100%;
            min-height: 280px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background-color: #f8f8f8;
            border-radius: 8px;
            padding: 20px 0;

            .nodata-img {
                width: 100px;
                height: auto;
                margin-bottom: 16px;
            }

            .nodata-text {
                font-size: 24px;
                color: #999999;
                text-align: center;
            }
        }
    }

}
</style>
