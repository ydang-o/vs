<template>
  <div class="login-container">
    <div class="login-card">
      <div class="header">
        <h1 class="title">合伙人投票系统</h1>
        <p class="subtitle">管理后台</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" :prefix-icon="User" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="footer">
        © 2025 Voting System Admin
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // Mock Login
      setTimeout(() => {
        loading.value = false
        if (form.username === 'admin' && form.password === '123') {
          ElMessage.success('登录成功')
          localStorage.setItem('token', 'mock-token')
          localStorage.setItem('user', JSON.stringify({ name: '系统管理员', role: 'admin' }))
          router.push('/')
        } else {
          ElMessage.error('账号或密码错误')
        }
      }, 1000)
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #F3F4F6;
  background-image: radial-gradient(#E5E7EB 1px, transparent 1px);
  background-size: 20px 20px;
}

.login-card {
  width: 400px;
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid #E5E7EB;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 24px;
  color: #3B82F6;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #6B7280;
  margin: 0;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  padding: 20px 0;
  font-size: 16px;
}

.footer {
  margin-top: 32px;
  text-align: center;
  color: #9CA3AF;
  font-size: 12px;
}
</style>
