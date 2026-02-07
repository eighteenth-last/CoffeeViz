<template>
  <section id="page-subscribe" class="page-view active">
    <div class="text-center mb-16">
      <h1 class="text-4xl font-black text-white mb-4 tracking-tight">选择适合您的 <span class="text-amber-500">CoffeeViz</span> 计划</h1>
      <p class="text-neutral-400 max-w-2xl mx-auto">解锁高级架构可视化功能，让数据库文档管理更智能、更高效。</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
      <!-- Free Plan -->
      <div class="glass-card p-8 rounded-2xl flex flex-col relative overflow-hidden group hover:border-neutral-700 transition-all">
        <div class="mb-6">
          <div class="text-sm font-bold text-neutral-500 uppercase tracking-widest mb-2">社区版</div>
          <div class="text-4xl font-black text-white">¥0<span class="text-lg text-neutral-500 font-medium">/月</span></div>
          <p class="text-sm text-neutral-500 mt-2">适合个人开发者与小型项目体验。</p>
        </div>
        
        <ul class="space-y-4 mb-8 flex-1">
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-green-500 mr-3"></i> 基础 SQL 导入 (MySQL Only)
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-green-500 mr-3"></i> 最多 3 个活跃项目
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-green-500 mr-3"></i> 基础 ER 图生成
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-green-500 mr-3"></i> 本地数据存储
          </li>
        </ul>

        <button class="w-full py-3 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-xl text-white font-bold transition-all">当前版本</button>
      </div>

      <!-- Pro Plan -->
      <div class="glass-card p-8 rounded-2xl flex flex-col relative overflow-hidden border-amber-600/50 bg-amber-600/[0.02] transform md:-translate-y-4 shadow-2xl shadow-amber-900/20">
        <div class="absolute top-0 right-0 bg-amber-600 text-white text-[10px] font-bold px-3 py-1 rounded-bl-lg">RECOMMENDED</div>
        
        <div class="mb-6">
          <div class="text-sm font-bold text-amber-500 uppercase tracking-widest mb-2">专业版</div>
          <div class="text-4xl font-black text-white">¥29<span class="text-lg text-neutral-500 font-medium">/月</span></div>
          <p class="text-sm text-amber-500/80 mt-2">独立开发者的最佳生产力工具。</p>
        </div>
        
        <ul class="space-y-4 mb-8 flex-1">
          <li class="flex items-center text-sm text-white">
            <i class="fas fa-check text-amber-500 mr-3"></i> <span class="font-bold">无限</span> 项目数量
          </li>
          <li class="flex items-center text-sm text-white">
            <i class="fas fa-check text-amber-500 mr-3"></i> 多方言 SQL 支持 (PG, Oracle)
          </li>
          <li class="flex items-center text-sm text-white">
            <i class="fas fa-check text-amber-500 mr-3"></i> 实时数据库连接同步
          </li>
          <li class="flex items-center text-sm text-white">
            <i class="fas fa-check text-amber-500 mr-3"></i> AI 智能辅助建模 (GPT-4)
          </li>
          <li class="flex items-center text-sm text-white">
            <i class="fas fa-check text-amber-500 mr-3"></i> 导出高清 SVG/PNG
          </li>
        </ul>

        <button class="w-full py-3 btn-amber rounded-xl text-white font-bold flex items-center justify-center group">
          立即订阅
          <i class="fas fa-arrow-right ml-2 group-hover:translate-x-1 transition-transform"></i>
        </button>
      </div>

      <!-- Enterprise Plan -->
      <div class="glass-card p-8 rounded-2xl flex flex-col relative overflow-hidden group hover:border-neutral-700 transition-all">
        <div class="mb-6">
          <div class="text-sm font-bold text-purple-500 uppercase tracking-widest mb-2">团队版</div>
          <div class="text-4xl font-black text-white">¥99<span class="text-lg text-neutral-500 font-medium">/人/月</span></div>
          <p class="text-sm text-neutral-500 mt-2">为技术团队协作而生。</p>
        </div>
        
        <ul class="space-y-4 mb-8 flex-1">
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-purple-500 mr-3"></i> 包含所有专业版功能
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-purple-500 mr-3"></i> 团队协作与权限管理
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-purple-500 mr-3"></i> 版本控制与变更审计
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-purple-500 mr-3"></i> API 自动化集成
          </li>
          <li class="flex items-center text-sm text-neutral-300">
            <i class="fas fa-check text-purple-500 mr-3"></i> 私有化部署支持
          </li>
        </ul>

        <button class="w-full py-3 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-xl text-white font-bold transition-all">联系销售</button>
      </div>
    </div>
    
    <div class="mt-20 text-center">
      <h3 class="text-xl font-bold text-white mb-8">常见问题</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto text-left">
        <div>
          <h4 class="text-amber-500 font-bold mb-2">如何切换订阅计划？</h4>
          <p class="text-sm text-neutral-400">您可以随时在账户设置中升级或降级您的订阅计划，变更将在下一个计费周期生效。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">支持哪些支付方式？</h4>
          <p class="text-sm text-neutral-400">我们支持支付宝、微信支付以及主流信用卡（Visa, MasterCard）。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">数据安全如何保障？</h4>
          <p class="text-sm text-neutral-400">所有数据传输均采用 SSL 加密，敏感配置信息（如数据库密码）仅存储在您的本地浏览器中。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">可以开发票吗？</h4>
          <p class="text-sm text-neutral-400">支持开具增值税电子普通发票，请在支付完成后在订单详情页申请。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  // Animation logic or analytics can go here
})
</script>
