# Public 目录说明

## 📁 目录作用

`public` 目录用于存放**静态资源**，这些文件会被 Vite 直接复制到构建输出目录的根目录，不会被 Vite 处理或打包。

## 📝 使用方式

### 1. 引用方式

在代码中引用 public 目录中的文件时，使用**绝对路径**（以 `/` 开头）：

```html
<!-- HTML 中 -->
<link rel="icon" href="/favicon.ico">
<img src="/logo.png" alt="Logo">
```

```javascript
// JavaScript 中
const logoUrl = '/logo.png'
```

```vue
<!-- Vue 组件中 -->
<template>
  <img src="/logo.png" alt="Logo">
</template>
```

### 2. 与 src/assets 的区别

| 特性 | public/ | src/assets/ |
|------|---------|-------------|
| 处理方式 | 直接复制，不处理 | 会被 Vite 处理（压缩、优化等） |
| 引用方式 | 绝对路径 `/` | 相对路径或 `@/assets/` |
| 适用场景 | favicon、robots.txt、不需要处理的文件 | 图片、字体等需要优化的资源 |
| 文件大小 | 不优化 | 会被优化和压缩 |

## 📦 常见文件

### 1. favicon.ico
网站图标，浏览器标签页显示

### 2. robots.txt
搜索引擎爬虫规则文件

### 3. manifest.json
PWA 应用清单文件

### 4. 静态图片
不需要处理的图片文件

### 5. 第三方库文件
需要直接引用的 JS/CSS 文件

## ⚠️ 注意事项

1. **文件路径**
   - 引用时使用 `/文件名`，不是 `/public/文件名`
   - Vite 会自动将 public 目录的内容复制到输出根目录

2. **文件大小**
   - public 中的文件不会被压缩或优化
   - 大文件建议放在 src/assets 中

3. **环境变量**
   - 可以使用 `%PUBLIC_URL%` 或 `%VITE_PUBLIC_URL%`（如果配置了）

4. **构建输出**
   - 构建后，public 目录的内容会直接复制到 `dist/` 根目录

## 🔗 参考

- [Vite 静态资源处理](https://cn.vitejs.dev/guide/assets.html#the-public-directory)

