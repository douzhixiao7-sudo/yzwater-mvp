<!-- 河道级别组件 -->
<template>
    <div class="river-level">
        <span class="river-level-value">{{ level }}</span>
    </div>
</template>

<script>
export default {
    name: 'RiverLevel',
    props: {
        level: {
            type: String,
            default: ''
        }
    }
}
</script>

<style lang="scss" scoped>
.river-level {
    position: absolute;
    top: 0;
    right: 0;
    width: 106px;
    height: 44px;
    text-align: center;
    line-height: 44px;
    font-size: 28px;
    color: #ffffff;
    font-weight: 400;
    background: url('@/assets/img/riverLevelBg.png') no-repeat center center;
    background-size: 100% 100%;
    padding-left: 13px;
    
    .river-level-value {
        display: inline-block;
    }
}
</style>

