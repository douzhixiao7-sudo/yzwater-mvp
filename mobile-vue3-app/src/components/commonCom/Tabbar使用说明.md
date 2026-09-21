# Tabbar 组件使用说明

## 📋 组件概述

`Tabbar` 是一个可复用的标签栏组件，支持动态切换标签和内容，带有平滑的过渡动画效果。

## 🚀 使用流程

### 第一步：创建内容组件

为每个标签创建对应的内容组件，例如：

```vue
<!-- src/components/MyContent1.vue -->
<template>
    <div class="my-content-1">
        <p>这是第一个标签的内容</p>
    </div>
</template>

<script>
export default {
    name: 'MyContent1'
}
</script>

<style lang="scss" scoped>
.my-content-1 {
    // 你的样式
}
</style>
```

```vue
<!-- src/components/MyContent2.vue -->
<template>
    <div class="my-content-2">
        <p>这是第二个标签的内容</p>
    </div>
</template>

<script>
export default {
    name: 'MyContent2'
}
</script>

<style lang="scss" scoped>
.my-content-2 {
    // 你的样式
}
</style>
```

### 第二步：在使用页面导入组件

```vue
<script>
import Tabbar from '@/components/Tabbar.vue';
import MyContent1 from '@/components/MyContent1.vue';
import MyContent2 from '@/components/MyContent2.vue';

export default {
    components: {
        Tabbar,
        MyContent1,
        MyContent2
    },
    // ...
}
</script>
```

### 第三步：配置标签数据

在 `data()` 中配置标签数组和组件映射，**重要：需要使用 `markRaw()` 包装组件**：

```vue
<script>
import { markRaw } from 'vue';

export default {
    data() {
        return {
            // 标签配置数组
            tabs: [
                { label: '标签1', key: 'tab1' },
                { label: '标签2', key: 'tab2' },
                { label: '标签3', key: 'tab3' }
            ],
            // 标签对应的组件映射，使用 markRaw 防止组件被响应式化
            tabComponents: {
                'tab1': markRaw(MyContent1),
                'tab2': markRaw(MyContent2),
                'tab3': markRaw(MyContent3)  // 如果有第三个组件
            }
        }
    }
}
</script>
```

**⚠️ 重要提示**：必须使用 `markRaw()` 包装组件，否则会收到 Vue 的警告：
```
[Vue warn]: Vue received a Component that was made a reactive object.
```

### 第四步：在模板中使用

```vue
<template>
    <div class="my-page">
        <!-- 其他内容 -->
        
        <div class="content-wrapper">
            <Tabbar 
                :tabs="tabs"
                :components="tabComponents"
                :default-active-key="'tab1'"
                @tab-change="handleTabChange"
            />
        </div>
    </div>
</template>
```

### 第五步：处理标签切换事件（可选）

```vue
<script>
export default {
    methods: {
        // 处理标签切换事件
        handleTabChange(tabKey, index) {
            console.log('切换到标签:', tabKey, '索引:', index);
            // 可以在这里处理标签切换后的逻辑
            // 例如：加载数据、发送请求等
        }
    }
}
</script>
```

## 📝 Props 参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `tabs` | Array | ✅ 是 | `[]` | 标签配置数组，格式：`[{ label: '标签名', key: '标签key' }]` |
| `components` | Object | ✅ 是 | `{}` | 组件映射对象，格式：`{ '标签key': 组件 }` |
| `defaultActiveKey` | String | ❌ 否 | `''` | 默认激活的标签 key，如果不传则默认第一个标签 |

### tabs 数组格式

```javascript
tabs: [
    { label: '标签显示名称', key: '唯一标识key' },
    { label: '基础信息', key: 'base' },
    { label: '河长信息', key: 'river' }
]
```

### components 对象格式

```javascript
tabComponents: {
    '标签key1': 组件1,
    '标签key2': 组件2,
    'base': BaseInfoContent,
    'river': RiverInfoContent
}
```

## 🎯 事件说明

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `@tab-change` | `(tabKey, index)` | 标签切换时触发，`tabKey` 是标签的 key，`index` 是标签的索引 |

## 💡 完整示例

```vue
<template>
    <div class="example-page">
        <div class="drawer-content">
            <Tabbar 
                :tabs="tabs"
                :components="tabComponents"
                :default-active-key="'info'"
                @tab-change="handleTabChange"
            />
        </div>
    </div>
</template>

<script>
import Tabbar from '@/components/Tabbar.vue';
import InfoContent from '@/components/InfoContent.vue';
import DetailContent from '@/components/DetailContent.vue';

export default {
    name: 'ExamplePage',
    components: {
        Tabbar,
        InfoContent,
        DetailContent
    },
    data() {
        return {
            // 标签配置
            tabs: [
                { label: '信息', key: 'info' },
                { label: '详情', key: 'detail' }
            ],
            // 组件映射
            tabComponents: {
                'info': InfoContent,
                'detail': DetailContent
            }
        }
    },
    methods: {
        handleTabChange(tabKey, index) {
            console.log('标签切换:', tabKey, index);
            // 可以在这里处理业务逻辑
            if (tabKey === 'info') {
                // 切换到信息标签时的逻辑
            } else if (tabKey === 'detail') {
                // 切换到详情标签时的逻辑
            }
        }
    }
}
</script>

<style lang="scss" scoped>
.example-page {
    .drawer-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
}
</style>
```

## ⚠️ 注意事项

1. **必须使用 `markRaw()`**：在配置 `tabComponents` 时，必须使用 `markRaw()` 包装组件，避免 Vue 3 的响应式警告
   ```javascript
   import { markRaw } from 'vue';
   tabComponents: {
       'key': markRaw(Component)  // ✅ 正确
       // 'key': Component  // ❌ 错误，会收到警告
   }
   ```

2. **key 必须唯一**：每个标签的 `key` 必须唯一，不能重复
3. **组件映射要完整**：`tabComponents` 对象中的 key 必须与 `tabs` 数组中的 key 一一对应
4. **组件必须注册**：所有内容组件都需要在 `components` 中注册
5. **容器样式**：使用 Tabbar 的父容器需要设置 `flex: 1` 和 `overflow: hidden`，确保布局正确
6. **内容组件**：内容组件应该是一个完整的 Vue 组件，可以接收 props 和触发事件

## 🔄 使用流程图

```
1. 创建内容组件
   ↓
2. 在使用页面导入 Tabbar 和内容组件
   ↓
3. 配置 tabs 数组（标签配置）
   ↓
4. 配置 tabComponents 对象（组件映射）
   ↓
5. 在模板中使用 <Tabbar> 组件
   ↓
6. （可选）监听 @tab-change 事件处理业务逻辑
```

## 📦 文件结构建议

```
src/
├── components/
│   ├── Tabbar.vue              # Tabbar 主组件
│   ├── BaseInfoContent.vue     # 示例：基础信息内容
│   ├── RiverInfoContent.vue    # 示例：河长信息内容
│   └── YourContent1.vue         # 你的内容组件1
│   └── YourContent2.vue         # 你的内容组件2
└── views/
    └── YourPage.vue             # 使用 Tabbar 的页面
```

## 🎨 样式定制

Tabbar 组件的样式已经封装在组件内部，如果需要定制样式，可以：

1. 修改 `src/components/Tabbar.vue` 中的样式
2. 或者通过 CSS 变量（如果后续添加支持）

---

**提示**：如果需要在多个页面使用不同的样式，可以考虑将样式相关的 props 提取出来，或者创建多个 Tabbar 变体组件。

