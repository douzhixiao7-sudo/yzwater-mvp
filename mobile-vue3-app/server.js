import express from 'express'
import cors from 'cors'

const app = express()
const PORT = 3001

const mockLocation = {
  '我是zxc':{
    //经纬度
    latitude: 112.31,
    longitude: 30.57
  },
  'asd':{
    //经纬度
    latitude: 113.31,
    longitude: 30.57
  },
  'qwe':{
    //经纬度
    latitude: 32.53486,
    longitude: 119.451845
  }
}

// 允许跨域
app.use(cors())

// 解析 JSON 请求体
app.use(express.json())

// 测试接口 - 打印一句话
app.get('/api/test', (req, res) => {
  // 获取 GET 请求的查询参数
  const queryParams = req.query
  
  console.log('✅ 接口被调用了！')
  console.log('📥 GET 请求参数:', queryParams)
  
  res.json({
    code: 200,
    message: '接口调用成功！这是一条测试消息',
    data: {
      timestamp: new Date().toISOString(),
      queryParams: queryParams, // 返回请求参数
      msg: mockLocation[queryParams.queryCode]
    }
  })
})

// 启动服务器
app.listen(PORT, () => {
  console.log(`🚀 服务器已启动，运行在 http://localhost:${PORT}`)
  console.log(`📡 测试接口: http://localhost:${PORT}/api/test`)
})

