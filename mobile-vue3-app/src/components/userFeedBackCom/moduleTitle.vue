<template>

    <div class="module-title">

        <div class="module-title-text">
            <span class="module-title-text-right" v-if="required">* </span>
            <span class="module-title-text-left">{{ title }}</span>


            <span class="module-title-text-info">{{ info }}</span>


        </div>


    </div>


</template>

<script>
export default {
    name: 'ModuleTitle',
    props: {
        title: {
            type: String,
            default: ''
        },
        required: {
            type: Boolean,
            default: false
        },
        info: {
            type: String,
            default: ''
        }
    }
}
</script>

<style lang="scss" scoped>
.module-title {
    width: 100%;
    height: 80px;
    border-bottom: 1px solid rgba(238, 238, 238,0.5);
    line-height: 80px;
    margin-bottom: 24px;

    .module-title-text {

        .module-title-text-left {
            font-size: 32px;
            color: #3d3d3d;
            font-weight: 500;
        }

        .module-title-text-right {
            font-size: 32px;
            color: red;
        }

        .module-title-text-info {
            font-size: 20px;
            color: #3d3d3d;
            font-weight: 400;
            position: relative;
            top: -4px;
            left: 20px;
        }
    }

}
</style>