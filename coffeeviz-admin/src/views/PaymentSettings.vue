<template>
  <div class="space-y-6">
    <!-- 支付方式卡片列表 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- 微信支付卡片 -->
      <div 
        class="bg-bg-card rounded-xl border border-white/5 p-6 hover:border-green-500/30 transition-all cursor-pointer group relative overflow-hidden"
        @click="openConfig('wechatPay')"
      >
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <n-icon size="80" class="text-green-500">
            <LogoWechat />
          </n-icon>
        </div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 rounded-lg bg-green-500/10 flex items-center justify-center">
            <n-icon size="24" class="text-green-500">
              <LogoWechat />
            </n-icon>
          </div>
          <n-switch v-model:value="config.wechatPay.enabled" @click.stop @update:value="saveConfig" size="small" />
        </div>
        <h3 class="text-lg font-bold text-white mb-1 relative z-10">微信支付</h3>
        <p class="text-gray-500 text-xs mb-4 h-8 relative z-10">配置微信支付商户号、密钥等参数，支持 Native/JSAPI 支付。</p>
        <n-button size="small" secondary class="w-full relative z-10" @click.stop="openConfig('wechatPay')">
          <template #icon>
            <n-icon><SettingsOutline /></n-icon>
          </template>
          配置参数
        </n-button>
      </div>

      <!-- 支付宝卡片 -->
      <div 
        class="bg-bg-card rounded-xl border border-white/5 p-6 hover:border-blue-500/30 transition-all cursor-pointer group relative overflow-hidden"
        @click="openConfig('alipay')"
      >
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <n-icon size="80" class="text-blue-500">
            <WalletOutline />
          </n-icon>
        </div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 rounded-lg bg-blue-500/10 flex items-center justify-center">
            <n-icon size="24" class="text-blue-500">
              <WalletOutline />
            </n-icon>
          </div>
          <n-switch v-model:value="config.alipay.enabled" @click.stop @update:value="saveConfig" size="small" />
        </div>
        <h3 class="text-lg font-bold text-white mb-1 relative z-10">支付宝</h3>
        <p class="text-gray-500 text-xs mb-4 h-8 relative z-10">配置支付宝应用ID、密钥等参数，支持电脑网站/手机网站支付。</p>
        <n-button size="small" secondary class="w-full relative z-10" @click.stop="openConfig('alipay')">
          <template #icon>
            <n-icon><SettingsOutline /></n-icon>
          </template>
          配置参数
        </n-button>
      </div>

      <!-- 测试支付卡片 -->
      <div 
        class="bg-bg-card rounded-xl border border-white/5 p-6 hover:border-amber-500/30 transition-all cursor-pointer group relative overflow-hidden"
        @click="openConfig('test')"
      >
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <n-icon size="80" class="text-amber-500">
            <FlaskOutline />
          </n-icon>
        </div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 rounded-lg bg-amber-500/10 flex items-center justify-center">
            <n-icon size="24" class="text-amber-500">
              <FlaskOutline />
            </n-icon>
          </div>
          <n-switch v-model:value="config.test.enabled" @click.stop @update:value="saveConfig" size="small" />
        </div>
        <h3 class="text-lg font-bold text-white mb-1 relative z-10">测试支付</h3>
        <p class="text-gray-500 text-xs mb-4 h-8 relative z-10">开发/测试环境使用，模拟支付流程无需真实扣款。</p>
        <n-button size="small" secondary class="w-full relative z-10" @click.stop="openConfig('test')">
          <template #icon>
            <n-icon><SettingsOutline /></n-icon>
          </template>
          配置参数
        </n-button>
      </div>
    </div>

    <!-- 配置弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" class="w-[600px]" :bordered="false">
      <div class="space-y-4">
        <!-- 微信支付配置表单 -->
        <div v-if="currentType === 'wechatPay'" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="商户号 (mchId)" label-placement="top">
              <n-input v-model:value="config.wechatPay.mchId" placeholder="1627500294" />
            </n-form-item>
            <n-form-item label="AppID" label-placement="top">
              <n-input v-model:value="config.wechatPay.appId" placeholder="wxe97894ad8c7ef7e0" />
            </n-form-item>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="API Key" label-placement="top">
              <n-input v-model:value="config.wechatPay.apiKey" type="password" show-password-on="click" placeholder="API密钥" />
            </n-form-item>
            <n-form-item label="API V3 Key" label-placement="top">
              <n-input v-model:value="config.wechatPay.apiV3Key" type="password" show-password-on="click" placeholder="APIv3密钥" />
            </n-form-item>
          </div>
          <n-form-item label="商户私钥 (Private Key)" label-placement="top">
            <n-input v-model:value="config.wechatPay.privateKey" type="textarea" :rows="3" placeholder="-----BEGIN PRIVATE KEY-----..." />
          </n-form-item>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="证书序列号" label-placement="top">
              <n-input v-model:value="config.wechatPay.certSerialNo" placeholder="证书序列号" />
            </n-form-item>
            <n-form-item label="回调通知URL" label-placement="top">
              <n-input v-model:value="config.wechatPay.notifyUrl" placeholder="https://yourdomain.com/api/payment/callback/wechat" />
              <template #feedback>
                <span class="text-xs text-gray-500">回调路径必须为: /api/payment/callback/wechat，系统会自动提取域名拼接正确路径</span>
              </template>
            </n-form-item>
          </div>
        </div>

        <!-- 支付宝配置表单 -->
        <div v-if="currentType === 'alipay'" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="应用ID (AppID)" label-placement="top">
              <n-input v-model:value="config.alipay.appId" placeholder="2021005192689177" />
            </n-form-item>
            <n-form-item label="签名类型" label-placement="top">
              <n-select v-model:value="config.alipay.signType" :options="[{label:'RSA2',value:'RSA2'},{label:'RSA',value:'RSA'}]" />
            </n-form-item>
          </div>
          <n-form-item label="应用私钥" label-placement="top">
            <n-input v-model:value="config.alipay.privateKey" type="textarea" :rows="3" placeholder="MIIEvQIBADANBgkq..." />
          </n-form-item>
          <n-form-item label="支付宝公钥" label-placement="top">
            <n-input v-model:value="config.alipay.publicKey" type="textarea" :rows="3" placeholder="MIIBIjANBgkq..." />
          </n-form-item>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="网关地址" label-placement="top">
              <n-select v-model:value="config.alipay.gatewayUrl" :options="gatewayOptions" />
            </n-form-item>
            <n-form-item label="字符集" label-placement="top">
              <n-input v-model:value="config.alipay.charset" placeholder="UTF-8" />
            </n-form-item>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <n-form-item label="回调通知URL" label-placement="top">
              <n-input v-model:value="config.alipay.notifyUrl" placeholder="https://yourdomain.com/api/payment/callback/alipay" />
              <template #feedback>
                <span class="text-xs text-gray-500">回调路径必须为: /api/payment/callback/alipay，系统会自动提取域名拼接正确路径</span>
              </template>
            </n-form-item>
            <n-form-item label="前端回调URL" label-placement="top">
              <n-input v-model:value="config.alipay.returnUrl" placeholder="https://yourdomain.com/payment/result" />
            </n-form-item>
          </div>
        </div>

        <!-- 测试支付配置表单 -->
        <div v-if="currentType === 'test'" class="space-y-4">
          <n-form-item label="描述说明" label-placement="top">
            <n-input v-model:value="config.test.description" placeholder="测试环境模拟支付，无需真实支付" />
          </n-form-item>
          <n-alert type="warning">
            测试支付模式下，用户点击支付后将直接模拟成功，不会产生真实交易。请勿在生产环境启用。
          </n-alert>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="saveConfigAndClose">保存</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 底部保存按钮 (可选，如果希望保留全局保存) -->
    <!-- <div class="flex justify-end">
      <n-button type="primary" :loading="saving" @click="saveConfig" size="large">
        保存全部配置
      </n-button>
    </div> -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { LogoWechat, WalletOutline, FlaskOutline, SettingsOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const saving = ref(false)
const showModal = ref(false)
const currentType = ref('')

const config = reactive({
  wechatPay: {
    enabled: false, mchId: '', appId: '', apiKey: '', apiV3Key: '',
    privateKey: '', certSerialNo: '', notifyUrl: ''
  },
  alipay: {
    enabled: false, appId: '', privateKey: '', publicKey: '',
    signType: 'RSA2', charset: 'UTF-8',
    gatewayUrl: 'https://openapi.alipay.com/gateway.do',
    notifyUrl: '', returnUrl: ''
  },
  test: {
    enabled: false, description: '测试环境模拟支付，无需真实支付'
  }
})

const gatewayOptions = [
  { label: '正式环境', value: 'https://openapi.alipay.com/gateway.do' },
  { label: '沙箱环境', value: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do' }
]

const modalTitle = computed(() => {
  const titles = {
    wechatPay: '配置微信支付',
    alipay: '配置支付宝',
    test: '配置测试支付'
  }
  return titles[currentType.value] || '支付配置'
})

const openConfig = (type) => {
  currentType.value = type
  showModal.value = true
}

onMounted(async () => {
  try {
    const res = await api.get('/api/admin/settings/payment')
    if (res.data) {
      const d = res.data
      if (d.wechatPay) Object.assign(config.wechatPay, d.wechatPay)
      if (d.alipay) Object.assign(config.alipay, d.alipay)
      if (d.test) Object.assign(config.test, d.test)
    }
  } catch { /* 接口未实现，使用默认值 */ }
})

const saveConfig = async () => {
  saving.value = true
  try {
    await api.post('/api/admin/settings/payment', config)
    message.success('支付配置已保存并生效')
  } catch (e) {
    message.error('保存失败: ' + (e.message || e))
  } finally {
    saving.value = false
  }
}

const saveConfigAndClose = async () => {
  await saveConfig()
  showModal.value = false
}
</script>
