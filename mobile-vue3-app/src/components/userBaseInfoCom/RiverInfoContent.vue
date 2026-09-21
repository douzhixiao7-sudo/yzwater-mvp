<!-- 河长信息内容组件 -->
<template>
    <div class="river-info-content">
        <!-- 河长职责 -->
        <div class="section">
            <span class="contentTit">河长职责</span>
            <div v-if="riverDuty && riverDuty !== '--'" class="duty-content">
                <div class="duty-text" ref="dutyTextRef">
                    {{ riverDuty }}
                </div>
            </div>
            <div v-else class="duty-empty">
                <div class="empty-placeholder">
                    <img :src="nodataImg" alt="暂无数据" class="nodata-img" />
                    <div class="nodata-text">——暂无数据——</div>
                </div>
            </div>
        </div>

        <!-- 河长信息 -->
        <div class="section" style="position: relative;">
            <span class="contentTit">河长信息</span>
            <div v-if="totalChiefCount > 0" class="river-chief-list" :class="{ 'is-expanded': isExpanded }">
                <div
                    class="river-chief-card"
                    v-for="(chief, index) in displayedUpstreamList"
                    :key="chiefRowKey(chief, 'up-' + index)"
                >
                    <RiverLevel :level="chief.levelText" />
                    <div class="chief-info">
                        <div class="info-item">
                            <span class="label">姓名:</span>
                            <span class="value name-val">{{ chief.name }}&nbsp;&nbsp;</span>
                        </div>
                        <div class="info-item">
                            <span class="label">职务:</span>
                            <span class="value">{{ chief.position }}</span>
                        </div>
                    </div>
                </div>

                <template v-if="!showVillageGrouped">
                    <div
                        class="river-chief-card"
                        v-for="(chief, index) in displayedVillageList"
                        :key="chiefRowKey(chief, 'vil-' + index)"
                    >
                        <RiverLevel :level="chief.levelText" />
                        <div class="chief-info">
                            <div class="info-item">
                                <span class="label">姓名:</span>
                                <span class="value name-val">{{ chief.name }}&nbsp;&nbsp;</span>
                            </div>
                            <div class="info-item">
                                <span class="label">职务:</span>
                                <span class="value">{{ chief.position }}</span>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <div
                        class="village-section-group"
                        v-for="(group, gIndex) in displayedVillageGroups"
                        :key="'sec-' + gIndex"
                    >
                        <div class="section-group-title" v-if="group.sectionName">{{ group.sectionName }}</div>
                        <div
                            class="river-chief-card"
                            v-for="(chief, index) in group.chiefs"
                            :key="chiefRowKey(chief, 'vil-' + gIndex + '-' + index)"
                        >
                            <RiverLevel :level="chief.levelText" />
                            <div class="chief-info">
                                <div class="info-item">
                                    <span class="label">姓名:</span>
                                    <span class="value name-val">{{ chief.name }}&nbsp;&nbsp;</span>
                                </div>
                                <div class="info-item">
                                    <span class="label">职务:</span>
                                    <span class="value">{{ chief.position }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </div>

            <div v-else class="river-chief-empty">
                <div class="empty-placeholder">
                    <img :src="nodataImg" alt="暂无数据" class="nodata-img" />
                    <div class="nodata-text">——暂无数据——</div>
                </div>
            </div>

            <div class="expand-toggle-box" v-if="totalChiefCount > 3" @click="toggleExpand">
                <div class="expand-icon" :class="{ 'is-rotate': isExpanded }">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M7 10L12 15L17 10" stroke="#3D3D3D" stroke-width="2" stroke-linecap="round"
                            stroke-linejoin="round" />
                        <path d="M7 5L12 10L17 5" stroke="#3D3D3D" stroke-width="2" stroke-linecap="round"
                            stroke-linejoin="round" opacity="0.5" />
                    </svg>
                </div>
                <span>{{ isExpanded ? '收起全部' : '展开全部' }}</span>
            </div>
        </div>
    </div>
</template>

<script>
import RiverLevel from '@/components/userBaseInfoCom/RiverLevel.vue';
import nodataImg from '@/assets/img/nodataImg.png';

/** 河道：上级 channelHeads + 村级 sections[].heads（村级勿从 channelHeads 读取） */
function buildDisplayChiefsFromRiver(river) {
    if (!river || typeof river !== 'object') {
        return { upstream: [], villageGroups: [] };
    }
    const upstream = Array.isArray(river.channelHeads) ? river.channelHeads : [];
    const villageGroups = (Array.isArray(river.sections) ? river.sections : [])
        .filter(sec => sec && Array.isArray(sec.heads) && sec.heads.length > 0)
        .map(sec => ({
            sectionName: sec.sectionName || '',
            heads: sec.heads
        }));
    return { upstream, villageGroups };
}

function mapHeadToChiefItem(item) {
    if (!item || typeof item !== 'object') return null;
    return {
        id: item.id,
        name: item.headName || '--',
        position: item.headPosition || '--',
        phone: item.headContact || '--',
        level: item.headLevel,
        levelText: item.headLevelLabel || '--'
    };
}

function findFirstResponsibilities(upstream, villageGroups) {
    for (let i = 0; i < upstream.length; i++) {
        if (upstream[i].responsibilities) return upstream[i].responsibilities;
    }
    for (let g = 0; g < villageGroups.length; g++) {
        const heads = villageGroups[g].heads || [];
        for (let j = 0; j < heads.length; j++) {
            if (heads[j].responsibilities) return heads[j].responsibilities;
        }
    }
    return null;
}

export default {
    name: 'RiverInfoContent',
    components: {
        RiverLevel
    },
    async mounted() {
        try {
            this.riverInfo = await this.getBaseInfo();
            this.fillTemplateData();
        } catch (e) {
            console.error('[RiverInfoContent] 加载公示牌信息失败', e);
            this.riverInfo = null;
            this.riverDuty = '--';
            this.resetChiefDisplay();
        }
    },

    data() {
        return {
            nodataImg,
            isExpanded: false,
            riverDuty: '--',
            upstreamChiefList: [],
            villageChiefGroups: [],
            showVillageGrouped: false,
            riverInfo: null
        };
    },
    computed: {
        getQrcode() {
            return this.$store.state.qrcode;
        },
        villageChiefList() {
            return this.villageChiefGroups.flatMap(g => g.chiefs);
        },
        flatChiefList() {
            return [...this.upstreamChiefList, ...this.villageChiefList];
        },
        totalChiefCount() {
            return this.flatChiefList.length;
        },
        displayedUpstreamList() {
            if (this.isExpanded) return this.upstreamChiefList;
            const upstreamLen = this.upstreamChiefList.length;
            if (upstreamLen >= 3) return this.upstreamChiefList.slice(0, 3);
            return this.upstreamChiefList;
        },
        displayedVillageList() {
            const list = this.villageChiefList;
            if (this.isExpanded) return list;
            const upstreamShown = this.displayedUpstreamList.length;
            const remain = Math.max(0, 3 - upstreamShown);
            return list.slice(0, remain);
        },
        displayedVillageGroups() {
            if (this.isExpanded) return this.villageChiefGroups;
            const upstreamShown = this.displayedUpstreamList.length;
            let remain = Math.max(0, 3 - upstreamShown);
            if (remain <= 0) return [];

            const groups = [];
            for (let i = 0; i < this.villageChiefGroups.length && remain > 0; i++) {
                const g = this.villageChiefGroups[i];
                const chiefs = g.chiefs.slice(0, remain);
                if (chiefs.length) {
                    groups.push({ sectionName: g.sectionName, chiefs });
                    remain -= chiefs.length;
                }
            }
            return groups;
        }
    },
    methods: {
        toggleExpand() {
            this.isExpanded = !this.isExpanded;
        },
        resetChiefDisplay() {
            this.upstreamChiefList = [];
            this.villageChiefGroups = [];
            this.showVillageGrouped = false;
        },
        async getBaseInfo() {
            const response = await this.$http.get(`/app-api/signboard/info/by-qr/${this.getQrcode}`);
            return response.data;
        },
        chiefRowKey(chief, suffix) {
            const tag = suffix != null ? String(suffix) : '';
            if (chief && chief.id !== undefined && chief.id !== null && String(chief.id).trim() !== '') {
                return `head-${tag}${chief.id}`;
            }
            return `head-${tag}idx`;
        },
        fillTemplateData() {
            this.riverDuty = '--';
            this.resetChiefDisplay();

            try {
                if (!this.riverInfo || typeof this.riverInfo !== 'object') {
                    return;
                }

                const isReservoir = this.riverInfo.waterReservoirId;

                if (isReservoir) {
                    const wr = this.riverInfo.waterReservoirs;
                    const heads =
                        Array.isArray(wr) && wr.length > 0 && Array.isArray(wr[0].heads) ? wr[0].heads : [];
                    this.upstreamChiefList = heads.map(mapHeadToChiefItem).filter(Boolean);
                    this.villageChiefGroups = [];
                    this.showVillageGrouped = false;
                    const dutyHead = heads.find(h => h && h.responsibilities);
                    if (dutyHead) this.riverDuty = dutyHead.responsibilities;
                    return;
                }

                const river =
                    Array.isArray(this.riverInfo.rivers) && this.riverInfo.rivers[0]
                        ? this.riverInfo.rivers[0]
                        : null;
                const { upstream, villageGroups } = buildDisplayChiefsFromRiver(river);

                this.upstreamChiefList = upstream.map(mapHeadToChiefItem).filter(Boolean);
                this.villageChiefGroups = villageGroups.map(g => ({
                    sectionName: g.sectionName,
                    chiefs: (g.heads || []).map(mapHeadToChiefItem).filter(Boolean)
                }));
                this.showVillageGrouped = this.villageChiefGroups.length > 1;

                const duty = findFirstResponsibilities(upstream, villageGroups);
                if (duty) this.riverDuty = duty;
            } catch (e) {
                console.error('[RiverInfoContent] fillTemplateData', e);
                this.riverDuty = '--';
                this.resetChiefDisplay();
            }
        }
    }
};
</script>

<style lang="scss" scoped>
.river-info-content {
    .section {
        margin-bottom: 16px;

        &:last-of-type {
            margin-bottom: 0;
        }
    }

    .contentTit {
        font-size: 32px;
        color: #3D3D3D;
        font-weight: 600;
        display: block;
        margin-bottom: 8px;
    }

    .duty-content {
        background-color: #f8f8f8;
        border-radius: 4px;
        padding: 16px;
        margin-top: 8px;
    }

    .duty-text {
        font-size: 28px;
        color: #4a4a4a;
        line-height: 1.6;
        min-height: 200px;
        height: 200px;
        overflow-y: auto;
        -webkit-overflow-scrolling: touch;
        word-break: break-all;
        display: block;

        &::-webkit-scrollbar {
            width: 4px;
        }

        &::-webkit-scrollbar-track {
            background: #f0f0f0;
            border-radius: 2px;
        }

        &::-webkit-scrollbar-thumb {
            background: #cccccc;
            border-radius: 2px;

            &:hover {
                background: #aaaaaa;
            }
        }
    }

    .river-chief-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 8px;
        width: 100%;

        &.is-expanded {
            max-height: 480px;
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
            padding-right: 8px;

            &::-webkit-scrollbar {
                width: 6px;
            }

            &::-webkit-scrollbar-track {
                background: #f0f0f0;
                border-radius: 3px;
            }

            &::-webkit-scrollbar-thumb {
                background: #cccccc;
                border-radius: 3px;

                &:hover {
                    background: #aaaaaa;
                }
            }
        }
    }

    .village-section-group {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .section-group-title {
        font-size: 28px;
        font-weight: 600;
        color: #3d3d3d;
        padding: 4px 0 0;
    }

    .river-chief-card {
        position: relative;
        width: 100%;
        background: #f8f8f8;
        border-radius: 4px;
        padding: 24px 16px;
        overflow: hidden;
        box-sizing: border-box;
        flex-shrink: 0;
    }

    .expand-toggle-box {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        padding: 12px 0 0;
        cursor: pointer;
        font-size: 28px;
        color: #3D3D3D;
        font-weight: 500;

        .expand-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            transition: transform 0.3s ease;

            &.is-rotate {
                transform: rotate(180deg);
            }
        }
    }

    .chief-info {
        position: relative;
        z-index: 2;
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .info-item {
        display: flex;
        font-size: 28px;
        line-height: 1.5;

        .label {
            color: #2c2c2c;
            margin-right: 16px;
            white-space: nowrap;
            font-weight: 600;
        }

        .value {
            color: #4a4a4a;
            flex: 1;
            word-break: break-all;
            font-weight: 400;
        }
    }

    .duty-empty {
        margin-top: 8px;
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;

        .empty-placeholder {
            width: 100%;
            min-height: 200px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background-color: #f8f8f8;
            border-radius: 4px;
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

    .river-chief-empty {
        margin-top: 8px;
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;

        .empty-placeholder {
            width: 100%;
            min-height: 120px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background-color: #f8f8f8;
            border-radius: 4px;
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
