<!-- 页面标题组件 -->
<template>
    <div class="page-header">
        <div class="title-text">{{ title }}</div>
        <button class="close-btn" @click="handleClose">
            <svg width="48" height="48" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 4L4 12M4 4L12 12" stroke="#333333" stroke-width="1.5" stroke-linecap="round"
                    stroke-linejoin="round" />
            </svg>
        </button>
    </div>
</template>

<script>
export default {
    name: 'PageHeader',
    props: {
        // 标题文字
        title: {
            type: String,
            default: '标题'
        }
    },
    methods: {
        handleClose() {
            // 触发关闭事件
            this.$emit('close');
        }
    }
}
</script>

<style lang="scss" scoped>
.page-header {
    width: 100%;
    height: 100px;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    pointer-events: auto;

    .title-text {
        font-size: 36px;
        color: #333333;
        text-align: center;
        font-weight: 500;
    }

    .close-btn {
        position: absolute;
        left: 32px;
        top: 52%;
        transform: translateY(-50%);
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: transparent;
        border: none;
        cursor: pointer;
        padding: 0;
        pointer-events: auto;

        &:active {
            opacity: 0.6;
        }

        svg {
            display: block;
        }
    }
}
</style>

