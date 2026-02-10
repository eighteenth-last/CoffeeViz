<template>
  <div class="page-ai-generate flex flex-col h-[calc(100vh-144px)] relative overflow-hidden">
    
    <!-- Main Content Area (Results) -->
    <!-- Only visible when hasStarted is true -->
    <main class="flex-1 flex flex-col overflow-hidden relative z-0 pt-4">
        
        <!-- Result Split View Container -->
        <div v-if="hasStarted" class="flex-1 flex w-full h-full max-w-[1600px] mx-auto animate-fade-in relative transition-all duration-500 ease-in-out">
            
            <!-- Left: Text Output (Explanation & SQL) -->
            <div class="flex flex-col h-full bg-transparent overflow-hidden transition-all duration-500 ease-in-out"
                 :class="[showCanvas ? 'w-[30%] min-w-[400px]' : 'w-full max-w-[800px] mx-auto']">
                
                <div class="flex-1 overflow-y-auto p-5 space-y-6 custom-scrollbar" ref="outputContainer">
                    
                    <!-- User Prompt -->
                    <div v-if="currentPrompt" class="flex justify-end animate-fade-in">
                        <div class="bg-[#28292A] text-[#E3E3E3] px-5 py-3 rounded-2xl rounded-tr-sm max-w-[80%] leading-relaxed whitespace-pre-wrap border border-[#444746]">{{ currentPrompt }}</div>
                    </div>

                    <!-- Thinking Phase -->
                    <div v-if="isThinking" class="flex flex-col gap-3 p-0">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-blue-500 to-red-500 animate-spin"></div>
                            <span class="gemini-gradient-text font-medium text-sm">Thinking...</span>
                        </div>
                    </div>

                    <!-- Content Streaming Phase -->
                    <div v-if="!isThinking" class="space-y-8 pb-32">
                        
                        <!-- Explanation -->
                        <div class="animate-slide-up">
                            <div class="flex items-start gap-4">
                                <div class="w-8 h-8 rounded-full bg-white flex-shrink-0 flex items-center justify-center mt-1">
                                    <i class="fas fa-star text-transparent bg-clip-text bg-gradient-to-tr from-blue-600 to-red-600 text-xs"></i>
                                </div>
                                <div class="flex-1 text-[#E3E3E3] leading-7 text-[15px] markdown-body">
                                    <span v-html="renderedExplanation"></span><span v-if="streaming.explanation" class="cursor-blink"></span>
                                </div>
                            </div>
                        </div>

                        <!-- SQL Code -->
                        <div v-if="streamedSql" class="animate-slide-up pl-12">
                            <div class="gemini-card overflow-hidden mb-4">
                                <div class="flex items-center justify-between px-4 py-2 bg-[#28292A] border-b border-[#444746]">
                                    <span class="text-xs text-neutral-400 font-medium">SQL</span>
                                    <button @click="copySql" class="text-xs px-2 py-1 rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition"><i class="fas fa-copy mr-1"></i> Copy</button>
                                </div>
                                <pre class="language-sql m-0 !bg-[#1E1F20] !p-4 text-xs font-mono"><code ref="sqlCodeBlock" class="language-sql">{{ streamedSql }}</code></pre>
                            </div>
                            
                            <!-- Open Canvas Chip (Visible if canvas is closed but data is ready) -->
                            <button v-if="!showCanvas && mermaidCode" @click="showCanvas = true" class="flex items-center gap-2 px-4 py-2 rounded-full bg-[#28292A] hover:bg-[#3C4043] border border-[#444746] transition-colors text-sm text-neutral-200">
                                <i class="fas fa-columns text-blue-400"></i> Open Canvas
                            </button>
                        </div>

                    </div>
                </div>
            </div>

            <!-- Right: Diagram Preview (Canvas Panel) -->
            <div class="flex flex-col h-full bg-transparent transition-all duration-500 ease-in-out overflow-hidden"
                 :class="[showCanvas ? 'flex-1 opacity-100 translate-x-0' : 'w-0 opacity-0 translate-x-10 absolute right-0']">
                 
                <div class="gemini-card w-full h-full flex flex-col overflow-hidden relative border border-[#444746] m-4 mt-0 ml-0 !rounded-2xl shadow-2xl">
                    
                    <!-- Canvas Header Toolbar -->
                    <div class="h-14 bg-[#1E1F20] border-b border-[#444746] flex items-center justify-between px-4 shrink-0">
                        <div class="flex items-center gap-3">
                            <i class="fas fa-bezier-curve text-blue-400"></i>
                            <span class="text-sm font-medium text-neutral-200">CoffeeViz 架构可视化生成器</span>
                            <div class="h-4 w-[1px] bg-[#444746] mx-1"></div>
                            <div class="flex gap-1">
                                <button class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 transition" title="Undo"><i class="fas fa-undo text-xs"></i></button>
                                <button class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 transition" title="Redo"><i class="fas fa-redo text-xs"></i></button>
                            </div>
                        </div>
                        
                        <div class="flex items-center gap-2">
                            <div class="flex bg-[#28292A] rounded-lg p-0.5 border border-[#444746] mr-2">
                                <button 
                                    @click="canvasView = 'code'" 
                                    class="px-3 py-1 rounded text-xs font-medium transition"
                                    :class="[canvasView === 'code' ? 'bg-[#3C4043] text-white' : 'text-neutral-400 hover:text-neutral-200']"
                                >代码</button>
                                <button 
                                    @click="canvasView = 'preview'" 
                                    class="px-3 py-1 rounded text-xs font-medium transition"
                                    :class="[canvasView === 'preview' ? 'bg-[#3C4043] text-white' : 'text-neutral-400 hover:text-neutral-200']"
                                >预览</button>
                            </div>
                            
                            <button @click="clearSession" class="px-3 py-1.5 flex items-center gap-1.5 rounded-lg bg-[#28292A] hover:bg-[#3C4043] text-neutral-400 hover:text-white transition border border-[#444746] text-xs font-medium" title="新建会话">
                                <i class="fas fa-plus"></i>
                                <span>新建会话</span>
                            </button>
                            
                            <div class="h-4 w-[1px] bg-[#444746] mx-1"></div>
                            
                            <button @click="handleSave" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Save to MinIO">
                                <i class="fas fa-cloud-upload-alt"></i>
                            </button>
                            <button @click="handleDownload" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Download PNG">
                                <i class="fas fa-download"></i>
                            </button>
                            <button @click="handleCopy" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Copy Image">
                                <i class="fas fa-copy"></i>
                            </button>
                            
                            <div class="h-4 w-[1px] bg-[#444746] mx-1"></div>
                            
                            <button @click="showCanvas = false" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Close Canvas"><i class="fas fa-times"></i></button>
                        </div>
                    </div>

                    <!-- Diagram Canvas Area -->
                    <div class="flex-1 relative overflow-hidden flex items-center justify-center bg-[#1E1F20]">
                        
                        <!-- Empty / Waiting State -->
                        <div v-if="!mermaidCode && !loading" class="text-neutral-500 flex flex-col items-center">
                            <div class="w-12 h-12 rounded-full bg-[#28292A] flex items-center justify-center mb-3">
                                <i class="fas fa-project-diagram text-lg"></i>
                            </div>
                            <p class="text-xs font-medium">Visualization Preview</p>
                        </div>

                        <!-- Drawing State -->
                        <div v-if="streaming.diagram" class="flex flex-col items-center justify-center absolute inset-0 z-10 bg-[#1E1F20]/80">
                            <div class="w-8 h-8 rounded-full border-2 border-t-blue-500 border-r-purple-500 border-b-red-500 border-l-transparent animate-spin mb-3"></div>
                            <p class="gemini-gradient-text text-xs font-medium">Generating Diagram...</p>
                        </div>

                        <!-- Rendered Diagram (Preview Mode) -->
                        <div v-show="canvasView === 'preview' && mermaidCode" class="mermaid-container w-full h-full p-8 overflow-auto flex items-center justify-center" ref="mermaidContainer"></div>
                        
                        <!-- Mermaid Code (Code Mode) -->
                        <div v-show="canvasView === 'code' && mermaidCode" class="w-full h-full p-0 overflow-hidden bg-[#1E1F20]">
                            <textarea 
                                readonly 
                                class="w-full h-full bg-[#1E1F20] text-[#A8C7FA] font-mono text-sm p-6 resize-none outline-none border-none custom-scrollbar"
                                :value="mermaidCode"
                            ></textarea>
                        </div>

                    </div>
                </div>
            </div>

        </div>

    </main>

    <!-- Input Box Container -->
    <div 
        class="absolute z-50 transition-all duration-500 ease-in-out pointer-events-none"
        :class="[ 
            hasStarted 
                ? (showCanvas ? 'bottom-4 left-0 w-[30%] min-w-[400px] px-6' : 'bottom-4 left-1/2 -translate-x-1/2 w-full max-w-[800px] px-4') 
                : 'top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl px-4'
        ]"
    >
        <div class="w-full pointer-events-auto relative">
            
            <!-- Welcome Title (Only visible when centered) -->
            <div 
                class="text-center input-transition overflow-hidden"
                :class="[ hasStarted ? 'max-h-0 opacity-0 mb-0' : 'max-h-60 opacity-100 mb-8' ]"
            >
                <h2 class="text-4xl font-bold mb-3 tracking-wide gemini-gradient-text">您想构建什么？</h2>
                <p class="text-neutral-400">描述业务需求，AI 将为您生成数据库设计与 ER 图</p>
            </div>

            <!-- Input Box -->
            <div 
                class="gemini-input rounded-3xl p-2 flex flex-col gap-2 relative group transition-all duration-300"
                :class="[ hasStarted ? '!bg-[#1E1F20] border-[#444746]' : '' ]"
            >
                <!-- Textarea -->
                <textarea 
                    v-model="prompt" 
                    @keydown.ctrl.enter="handleAction"
                    @input="autoResize"
                    ref="textareaRef"
                    rows="1" 
                    class="bg-transparent border-none outline-none text-white w-full px-4 py-2 resize-none placeholder-neutral-500 max-h-60 custom-scrollbar text-lg leading-relaxed"
                    placeholder="输入您的需求，例如：设计一个即时通讯系统..."
                ></textarea>

                <!-- Footer Actions -->
                <div class="flex justify-between items-center px-2 pb-0">
                    <!-- Quick Tags -->
                    <div class="flex gap-2 overflow-x-auto no-scrollbar">
                        <button @click="fillPrompt('设计一个电商订单系统，包含用户、商品、订单和支付')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-shopping-cart mr-1"></i> 电商系统
                        </button>
                        <button @click="fillPrompt('设计一个博客内容管理系统，支持多作者和评论')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-newspaper mr-1"></i> 博客系统
                        </button>
                        <button @click="fillPrompt('设计一个项目管理工具，支持看板和任务分配')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-tasks mr-1"></i> 项目管理
                        </button>
                    </div>

                    <!-- Send Button -->
                    <button 
                        @click="handleAction" 
                        :disabled="!loading && !prompt.trim()"
                        class="ml-3 w-10 h-10 rounded-xl bg-white text-black hover:bg-amber-400 hover:scale-105 transition-all flex items-center justify-center disabled:opacity-30 disabled:hover:bg-white disabled:hover:scale-100 disabled:cursor-not-allowed shadow-lg"
                    >
                        <i v-if="loading" class="fas fa-stop text-sm"></i>
                        <i v-else class="fas fa-arrow-up text-sm"></i>
                    </button>
                </div>
            </div>
            
            <!-- Footer Info (Only visible when centered) -->
            <div 
                class="text-center text-xs text-neutral-600 input-transition overflow-hidden"
                :class="[ hasStarted ? 'max-h-0 opacity-0 mt-0' : 'max-h-10 opacity-100 mt-4' ]"
            >
                <p>Powered by CoffeeViz AI • Supports Streaming & Mermaid Visualization</p>
            </div>
        </div>
    </div>

    <!-- Save Project Modal -->
    <div v-if="showSaveModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-[100] flex items-center justify-center fade-in">
        <div class="glass-card w-full max-w-lg p-8 rounded-2xl border border-neutral-800 shadow-2xl">
            <div class="flex items-center justify-between mb-6">
                <h3 class="text-2xl font-bold text-white">保存到归档库</h3>
                <button @click="showSaveModal = false" class="w-8 h-8 rounded-full bg-neutral-900 flex items-center justify-center text-neutral-500 hover:text-white transition-colors">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            
            <div class="space-y-6">
                <div class="bg-green-500/10 border border-green-500/20 rounded-xl p-4 flex items-start">
                    <i class="fas fa-check-circle text-green-500 text-xl mr-3 mt-0.5"></i>
                    <div>
                        <div class="text-green-500 font-bold mb-1">AI 架构设计生成成功</div>
                        <div class="text-sm text-neutral-400">已生成数据库设计和 ER 图</div>
                    </div>
                </div>

                <!-- 保存模式选择 -->
                <div>
                    <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-3">保存方式 *</label>
                    <div class="flex gap-3">
                        <button 
                            @click="saveFormData.saveMode = 'new'"
                            class="flex-1 py-3 px-4 rounded-xl border transition-all font-medium text-sm"
                            :class="[
                                saveFormData.saveMode === 'new' 
                                    ? 'bg-amber-600 border-amber-600 text-white' 
                                    : 'bg-black/50 border-neutral-800 text-neutral-400 hover:border-neutral-600'
                            ]"
                        >
                            <i class="fas fa-plus-circle mr-2"></i>
                            创建新架构库
                        </button>
                        <button 
                            @click="saveFormData.saveMode = 'existing'"
                            class="flex-1 py-3 px-4 rounded-xl border transition-all font-medium text-sm"
                            :class="[
                                saveFormData.saveMode === 'existing' 
                                    ? 'bg-amber-600 border-amber-600 text-white' 
                                    : 'bg-black/50 border-neutral-800 text-neutral-400 hover:border-neutral-600'
                            ]"
                        >
                            <i class="fas fa-folder-open mr-2"></i>
                            保存到现有库
                        </button>
                    </div>
                </div>

                <!-- 选择现有架构库 -->
                <div v-if="saveFormData.saveMode === 'existing'">
                    <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">
                        选择架构库 * 
                        <span class="text-neutral-600 font-normal normal-case text-xs">(共 {{ repositories.length }} 个)</span>
                    </label>
                    <select 
                        v-model="saveFormData.repositoryId"
                        class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all"
                    >
                        <option value="" disabled>请选择架构库</option>
                        <option v-for="repo in repositories" :key="repo.id" :value="repo.id">
                            {{ repo.repositoryName }} ({{ repo.diagramCount }} 个图)
                        </option>
                    </select>
                    <div v-if="repositories.length === 0" class="mt-2 text-xs text-neutral-500">
                        暂无架构库，请先创建新架构库
                    </div>
                </div>

                <!-- 项目名称（新建模式）或图表名称（现有模式） -->
                <div>
                    <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">
                        {{ saveFormData.saveMode === 'new' ? '架构库名称' : '图表名称' }} *
                    </label>
                    <input 
                        v-model="saveFormData.projectName"
                        type="text" 
                        class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700"
                        :placeholder="saveFormData.saveMode === 'new' ? '例如：电商订单系统架构' : '例如：订单模块 ER 图'"
                    >
                </div>
                
                <div>
                    <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">描述 <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
                    <textarea 
                        v-model="saveFormData.description"
                        rows="3" 
                        class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all resize-none placeholder-neutral-700"
                        placeholder="记录项目用途、业务需求等..."
                    ></textarea>
                </div>
                
                <div class="pt-4 flex space-x-4">
                    <button @click="showSaveModal = false" class="flex-1 py-3 rounded-xl border border-neutral-800 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-all font-bold">取消</button>
                    <button 
                        @click="handleConfirmSave" 
                        :disabled="!canSave || saving"
                        class="flex-1 py-3 bg-green-600 hover:bg-green-500 rounded-xl text-white font-bold shadow-lg shadow-green-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                    >
                        <i v-if="saving" class="fas fa-spinner fa-spin mr-2"></i>
                        <span>{{ saving ? '保存中...' : '保存项目' }}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Settings Modal -->
    <div v-if="showSettings" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-[100] flex items-center justify-center fade-in">
        <div class="glass-card w-96 p-6 rounded-2xl">
            <h3 class="text-lg font-bold mb-4">设置</h3>
            <div class="space-y-4">
                <div>
                    <label class="block text-xs text-neutral-400 mb-1">Backend API URL</label>
                    <input v-model="settings.apiUrl" type="text" class="w-full bg-neutral-900 border border-neutral-700 rounded p-2 text-sm text-white focus:border-amber-500 outline-none">
                </div>

            </div>
            <div class="mt-6 flex justify-end">
                <button @click="showSettings = false" class="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-sm font-bold transition">Close</button>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, watch, computed } from 'vue';
import { useMessage } from 'naive-ui';

// Dynamic Load Helper
const loadScript = (src) => {
    return new Promise((resolve, reject) => {
        if (document.querySelector(`script[src="${src}"]`)) {
            resolve();
            return;
        }
        const script = document.createElement('script');
        script.src = src;
        script.onload = resolve;
        script.onerror = reject;
        document.head.appendChild(script);
    });
};

const loadStyle = (href) => {
    if (document.querySelector(`link[href="${href}"]`)) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = href;
    document.head.appendChild(link);
};


const message = useMessage();
const prompt = ref('');
const currentPrompt = ref('');
const loading = ref(false);
const stopFlag = ref(false);
const hasStarted = ref(false); // Controls Layout State
const isThinking = ref(false); // Controls Thinking UI
const showSettings = ref(false);
const showCanvas = ref(false); // Controls the visibility of the right canvas panel
const showSaveModal = ref(false); // Controls save modal
const saving = ref(false); // Saving state
const canvasView = ref('preview'); // 'preview' | 'code'
const isMarkedLoaded = ref(false);

const streamedExplanation = ref('');
const streamedSql = ref('');
const mermaidCode = ref('');
const sqlDdl = ref(''); // 存储 SQL DDL 代码
const tableCount = ref(0); // 表数量
const relationCount = ref(0); // 关系数量

const outputContainer = ref(null);
const sqlCodeBlock = ref(null);
const mermaidContainer = ref(null);
const textareaRef = ref(null);

const streaming = reactive({
    explanation: false,
    sql: false,
    diagram: false
});

const settings = reactive({
    apiUrl: 'http://localhost:8080/api/ai/generate'
});

const saveFormData = reactive({
    saveMode: 'new', // 'new' | 'existing'
    repositoryId: '',
    projectName: '',
    description: ''
});

const repositories = ref([]); // 架构库列表

const canSave = computed(() => {
    if (!saveFormData.projectName.trim()) return false;
    if (saveFormData.saveMode === 'existing' && !saveFormData.repositoryId) return false;
    return true;
});

const renderedExplanation = computed(() => {
    if (!streamedExplanation.value) return '';
    if (isMarkedLoaded.value && window.marked) {
        return window.marked.parse(streamedExplanation.value);
    }
    return streamedExplanation.value;
});

// Watch for start to reset canvas
watch(hasStarted, (val) => {
    if (!val) showCanvas.value = false;
});

// 监听关键数据变化，自动保存会话状态
watch(
    [hasStarted, currentPrompt, streamedExplanation, streamedSql, mermaidCode, showCanvas],
    () => {
        // 只有在有内容时才保存
        if (hasStarted.value) {
            try {
                const session = {
                    hasStarted: hasStarted.value,
                    currentPrompt: currentPrompt.value,
                    streamedExplanation: streamedExplanation.value,
                    streamedSql: streamedSql.value,
                    mermaidCode: mermaidCode.value,
                    showCanvas: showCanvas.value,
                    timestamp: Date.now()
                };
                sessionStorage.setItem('aiGenerateSession', JSON.stringify(session));
            } catch (e) {
                console.error('保存会话失败:', e);
            }
        }
    },
    { deep: true }
);

onMounted(async () => {
    // 1. 恢复会话状态
    try {
        const savedSession = sessionStorage.getItem('aiGenerateSession');
        if (savedSession) {
            const session = JSON.parse(savedSession);
            
            // 恢复状态
            if (session.hasStarted) {
                hasStarted.value = session.hasStarted;
                currentPrompt.value = session.currentPrompt || '';
                streamedExplanation.value = session.streamedExplanation || '';
                streamedSql.value = session.streamedSql || '';
                mermaidCode.value = session.mermaidCode || '';
                showCanvas.value = session.showCanvas || false;
                
                // 如果有 Mermaid 代码，等待库加载后渲染
                if (session.mermaidCode) {
                    // 延迟渲染，等待 mermaid 库加载完成
                    setTimeout(async () => {
                        if (window.mermaid && mermaidContainer.value) {
                            await renderMermaid(session.mermaidCode);
                        }
                    }, 1000);
                }
                
                console.log('已恢复 AI 生成会话');
            }
        }
    } catch (e) {
        console.error('恢复会话失败:', e);
    }
    
    // 2. Dynamic Load Prism and Mermaid and Marked
    try {
        loadStyle('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css');
        await Promise.all([
            loadScript('https://cdn.bootcdn.net/ajax/libs/mermaid/10.6.1/mermaid.min.js'),
            loadScript('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/prism.min.js'),
            loadScript('https://cdn.bootcdn.net/ajax/libs/marked/12.0.0/marked.min.js')
        ]);
        await loadScript('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/components/prism-sql.min.js');
        
        isMarkedLoaded.value = true;

        if (window.mermaid) {
            window.mermaid.initialize({ startOnLoad: false, theme: 'dark', securityLevel: 'loose' });
        }
    } catch (e) {
        console.error('Failed to load dependencies', e);
    }

    // Focus input on load
    if(textareaRef.value) textareaRef.value.focus();
});

// Auto resize textarea
const autoResize = () => {
    const el = textareaRef.value;
    if(el) {
        el.style.height = 'auto';
        el.style.height = el.scrollHeight + 'px';
    }
};

const fillPrompt = (text) => {
    prompt.value = text;
    nextTick(() => {
        autoResize();
        if(textareaRef.value) textareaRef.value.focus();
    });
};

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

const streamText = async (fullText, targetRef, type) => {
    streaming[type] = true;
    targetRef.value = '';
    
    const chunkSize = 3; 
    for (let i = 0; i < fullText.length; i += chunkSize) {
        if (stopFlag.value) break;
        targetRef.value += fullText.slice(i, i + chunkSize);
        
        if (outputContainer.value) {
            outputContainer.value.scrollTop = outputContainer.value.scrollHeight;
        }
        
        if (type === 'sql' && i % 50 === 0 && sqlCodeBlock.value && window.Prism) {
            window.Prism.highlightElement(sqlCodeBlock.value);
        }

        await sleep(10 + Math.random() * 20); 
    }
    
    if (type === 'sql' && sqlCodeBlock.value && window.Prism) {
        window.Prism.highlightElement(sqlCodeBlock.value);
    }
    streaming[type] = false;
};

const renderMermaid = async (code) => {
    if (!mermaidContainer.value || !window.mermaid) return;
    streaming.diagram = true;
    mermaidContainer.value.innerHTML = '';
    
    await sleep(800); // Fake delay for "Drawing..." effect

    try {
        const { svg } = await window.mermaid.render('mermaid-graph-' + Date.now(), code);
        mermaidContainer.value.innerHTML = svg;
    } catch (e) {
        console.error('Mermaid render error:', e);
        mermaidContainer.value.innerHTML = '<div class="text-red-500">Error rendering diagram</div>';
    } finally {
        streaming.diagram = false;
    }
};

const stop = () => {
    stopFlag.value = true;
    loading.value = false;
    isThinking.value = false;
    Object.keys(streaming).forEach(k => streaming[k] = false);
};

const getDiagramBlob = async () => {
    if (!mermaidContainer.value) return null;
    const svgEl = mermaidContainer.value.querySelector('svg');
    if (!svgEl) return null;
    
    // Serialize SVG
    const svgData = new XMLSerializer().serializeToString(svgEl);
    
    // Create Canvas
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    // Get dimensions (use viewBox or bounding client rect)
    const rect = svgEl.getBoundingClientRect();
    const viewBoxAttr = svgEl.getAttribute('viewBox');
    let width = rect.width;
    let height = rect.height;
    
    if (viewBoxAttr) {
        const viewBox = viewBoxAttr.split(/\s+|,/).map(Number);
        if (viewBox.length === 4) {
            width = viewBox[2];
            height = viewBox[3];
        }
    }
    
    // Scale for better quality
    const scale = 2;
    canvas.width = width * scale;
    canvas.height = height * scale;
    
    const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(svgBlob);
    
    return new Promise((resolve) => {
        img.onload = () => {
            // Fill background
            ctx.fillStyle = '#1E1F20'; 
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob((blob) => {
                URL.revokeObjectURL(url);
                resolve(blob);
            }, 'image/png');
        };
        img.onerror = () => {
            URL.revokeObjectURL(url);
            resolve(null);
        };
        img.src = url;
    });
};

const handleSave = async () => {
    if (!mermaidCode.value) {
        message.warning('请先生成架构图');
        return;
    }
    
    // 加载架构库列表
    await loadRepositories();
    
    // 显示保存弹窗
    showSaveModal.value = true;
};

// 加载架构库列表
const loadRepositories = async () => {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            message.error('未登录，请先登录');
            return;
        }
        
        const response = await fetch('http://localhost:8080/api/repository/list?page=1&size=100', {
            method: 'GET',
            headers: {
                'Authorization': token
            }
        });
        
        const result = await response.json();
        
        console.log('架构库列表 API 响应:', result);
        
        if (result.code === 200 && result.data) {
            // API 返回的是 list 而不是 records
            repositories.value = result.data.list || result.data.records || [];
            console.log('已加载架构库数量:', repositories.value.length);
            console.log('架构库列表:', repositories.value);
        } else {
            message.error('加载架构库列表失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('加载架构库列表失败:', error);
        message.error('加载架构库列表失败: ' + error.message);
    }
};

const handleDownload = async () => {
    if (!mermaidCode.value) return;
    const blob = await getDiagramBlob();
    if (!blob) return;
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `coffeeviz-diagram-${Date.now()}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
};

const handleCopy = async () => {
    if (!mermaidCode.value) return;
    try {
        const blob = await getDiagramBlob();
        if (!blob) return;
        await navigator.clipboard.write([
            new ClipboardItem({ 'image/png': blob })
        ]);
        message.success('Image copied to clipboard');
    } catch (e) {
        console.error(e);
        message.error('Copy failed: ' + e.message);
    }
};

const handleAction = () => {
    if (loading.value) {
        stop();
    } else {
        generate();
    }
};

const generate = async () => {
    const textToSend = prompt.value.trim();
    if (!textToSend) return;
    
    currentPrompt.value = textToSend;
    prompt.value = '';
    
    stopFlag.value = false;
    loading.value = true;
    hasStarted.value = true; // Trigger layout change immediately
    isThinking.value = true; // Show thinking state
    
    // Reset outputs
    streamedExplanation.value = '';
    streamedSql.value = '';
    mermaidCode.value = '';

    // Reset textarea height after send
    if(textareaRef.value) {
         textareaRef.value.style.height = 'auto';
    }

    try {
        let data;
        
        // 1. Thinking Phase (Simulate Backend Call or Mock)
        // const startTime = Date.now();
        
        const token = localStorage.getItem('token');
        if (!token) {
            throw new Error('未登录，请先登录系统');
        }

        const res = await fetch(settings.apiUrl, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({
                prompt: textToSend,
                dbType: 'mysql',
                namingStyle: 'snake_case'
            })
        });
        
        const json = await res.json();
        if (json.code === 200) {
            data = json.data;
            // 保存表数量和关系数量
            tableCount.value = data.tableCount || 0;
            relationCount.value = data.relationCount || 0;
            // 保存 SQL DDL
            sqlDdl.value = data.sqlDdl || '';
        } else {
            throw new Error(json.message || 'API Error');
        }

        // 2. Output Phase
        if (stopFlag.value) return;
        isThinking.value = false; // Hide thinking, start streaming
        loading.value = false; // Enable input again

        // Stream Explanation
        await streamText(data.aiExplanation || data.explanation, streamedExplanation, 'explanation');
        if (stopFlag.value) return;
        
        // Stream SQL
        await streamText(data.sqlDdl, streamedSql, 'sql');
        if (stopFlag.value) return;

        // 3. Draw Phase - Open Canvas
        showCanvas.value = true;
        
        // Sanitize Mermaid Code from LLM
        // 1. Remove Markdown code blocks
        // 2. Remove backticks from entity names (Fixes Parse error: Expecting 'EOF', 'SPACE'..., got '`')
        let cleanMermaid = data.mermaidCode
            .replace(/```mermaid/g, '')
            .replace(/```/g, '')
            .trim();
        
        // Remove backticks around entity names (e.g. `User` -> User)
        cleanMermaid = cleanMermaid.replace(/`([^`]+)`/g, '$1');
        
        mermaidCode.value = cleanMermaid;
        await renderMermaid(cleanMermaid);

    } catch (error) {
        console.error(error);
        alert('Error: ' + error.message);
        loading.value = false;
        isThinking.value = false;
    } finally {
         // prompt.value = ''; 
    }
};

const copySql = () => {
    navigator.clipboard.writeText(streamedSql.value);
    // Minimal feedback logic can be added here
};

const toggleSettings = () => {
    showSettings.value = !showSettings.value;
};

// 清空会话
const clearSession = () => {
    // 确认对话框
    if (hasStarted.value && !confirm('确定要清空当前会话吗？所有未保存的内容将丢失。')) {
        return;
    }
    
    // 清空所有状态
    hasStarted.value = false;
    currentPrompt.value = '';
    streamedExplanation.value = '';
    streamedSql.value = '';
    mermaidCode.value = '';
    sqlDdl.value = '';
    tableCount.value = 0;
    relationCount.value = 0;
    showCanvas.value = false;
    prompt.value = '';
    loading.value = false;
    isThinking.value = false;
    stopFlag.value = false;
    
    // 清空 sessionStorage
    sessionStorage.removeItem('aiGenerateSession');
    
    // 清空 Mermaid 容器
    if (mermaidContainer.value) {
        mermaidContainer.value.innerHTML = '';
    }
    
    // 重置 textarea 高度
    if (textareaRef.value) {
        textareaRef.value.style.height = 'auto';
        textareaRef.value.focus();
    }
    
    message.success('会话已清空');
};

// 确认保存到数据库
const handleConfirmSave = async () => {
    if (!saveFormData.projectName.trim()) {
        message.warning(saveFormData.saveMode === 'new' ? '请输入架构库名称' : '请输入图表名称');
        return;
    }
    
    if (saveFormData.saveMode === 'existing' && !saveFormData.repositoryId) {
        message.warning('请选择架构库');
        return;
    }
    
    saving.value = true;
    
    try {
        // 1. 生成 PNG 图片
        const blob = await getDiagramBlob();
        if (!blob) {
            message.error('生成图片失败');
            saving.value = false;
            return;
        }
        
        // 2. 转换为 Base64
        const reader = new FileReader();
        reader.onloadend = async () => {
            try {
                const base64 = reader.result;
                const token = localStorage.getItem('token');
                if (!token) {
                    message.error('未登录，请先登录');
                    saving.value = false;
                    return;
                }
                
                let response, result;
                
                if (saveFormData.saveMode === 'new') {
                    // 创建新架构库
                    response = await fetch('http://localhost:8080/api/project/create', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': token
                        },
                        body: JSON.stringify({
                            projectName: saveFormData.projectName,
                            description: saveFormData.description,
                            mermaidCode: mermaidCode.value,
                            sqlDdl: sqlDdl.value,
                            pngBase64: base64,
                            tableCount: tableCount.value,
                            relationCount: relationCount.value,
                            sourceType: 'AI',
                            dbType: 'mysql'
                        })
                    });
                } else {
                    // 保存到现有架构库
                    response = await fetch('http://localhost:8080/api/diagram/create', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': token
                        },
                        body: JSON.stringify({
                            repositoryId: saveFormData.repositoryId,
                            diagramName: saveFormData.projectName,
                            description: saveFormData.description,
                            mermaidCode: mermaidCode.value,
                            sqlDdl: sqlDdl.value,
                            pngBase64: base64,
                            tableCount: tableCount.value,
                            relationCount: relationCount.value,
                            sourceType: 'AI',
                            dbType: 'mysql'
                        })
                    });
                }
                
                result = await response.json();
                
                if (result.code === 200) {
                    message.success(saveFormData.saveMode === 'new' ? '架构库创建成功！' : '图表保存成功！');
                    showSaveModal.value = false;
                    
                    // 清空表单
                    saveFormData.saveMode = 'new';
                    saveFormData.repositoryId = '';
                    saveFormData.projectName = '';
                    saveFormData.description = '';
                } else {
                    message.error('保存失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('保存失败:', error);
                message.error('保存失败: ' + error.message);
            } finally {
                saving.value = false;
            }
        };
        
        reader.readAsDataURL(blob);
        
    } catch (error) {
        console.error('保存失败:', error);
        message.error('保存失败: ' + error.message);
        saving.value = false;
    }
};
</script>

<style scoped>
/* Gemini Style Overrides */
.gemini-card {
    background-color: #1E1F20;
    border-radius: 1.5rem; /* rounded-3xl */
    border: 1px solid transparent;
}

.gemini-input {
    background-color: #1E1F20;
    border-radius: 2rem; /* Pill shape */
    border: 1px solid transparent;
    transition: all 0.2s ease;
}

.gemini-input:focus-within {
    background-color: #28292A;
    box-shadow: none;
}

.gemini-gradient-text {
    background: linear-gradient(90deg, #4285F4, #9B72CB, #D96570);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* Custom Scrollbar - Thinner and darker */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}
::-webkit-scrollbar-track {
    background: transparent; 
}
::-webkit-scrollbar-thumb {
    background: #444746; 
    border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
    background: #5e5e5e; 
}

/* Cursor blinking for streaming effect */
.cursor-blink::after {
    content: '●'; /* Gemini uses a circle or just a cursor */
    font-size: 0.8em;
    animation: blink 1s step-end infinite;
    color: #4285F4;
    margin-left: 2px;
}
@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

/* Animations */
.fade-enter-active, .fade-leave-active {
    transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
}

.slide-up-enter-active, .slide-up-leave-active {
    transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
    transform: translateY(20px);
    opacity: 0;
}

.input-transition {
    transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Markdown Styles */
.markdown-body h1, .markdown-body h2, .markdown-body h3 {
    margin-top: 1.5em;
    margin-bottom: 0.5em;
    font-weight: 600;
    color: #E3E3E3;
}
.markdown-body h2 { font-size: 1.25em; }
.markdown-body h3 { font-size: 1.1em; }
.markdown-body p { margin-bottom: 1em; }
.markdown-body ul { list-style-type: disc; padding-left: 1.5em; margin-bottom: 1em; }
.markdown-body ol { list-style-type: decimal; padding-left: 1.5em; margin-bottom: 1em; }
.markdown-body li { margin-bottom: 0.25em; }
.markdown-body strong { font-weight: 700; color: #fff; }
.markdown-body code { 
    background-color: rgba(255,255,255,0.1); 
    padding: 0.2em 0.4em; 
    border-radius: 4px; 
    font-family: monospace; 
    font-size: 0.9em;
}
.markdown-body pre {
    background-color: #1E1F20;
    padding: 1em;
    border-radius: 8px;
    overflow-x: auto;
    margin-bottom: 1em;
}
</style>