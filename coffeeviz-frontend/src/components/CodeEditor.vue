<template>
  <div class="code-editor">
    <n-card :title="title" :bordered="false">
      <template #header-extra>
        <n-space>
          <n-button @click="handleClear" :disabled="disabled">
            <template #icon>
              <n-icon><TrashOutline /></n-icon>
            </template>
            清空
          </n-button>
          
          <n-button @click="handleFormat" :disabled="disabled" v-if="showFormat">
            <template #icon>
              <n-icon><CodeSlashOutline /></n-icon>
            </template>
            格式化
          </n-button>
          
          <n-button type="primary" @click="handleExecute" :disabled="disabled || !modelValue" :loading="loading">
            <template #icon>
              <n-icon><PlayOutline /></n-icon>
            </template>
            {{ executeText }}
          </n-button>
        </n-space>
      </template>
      
      <n-input
        v-model:value="content"
        type="textarea"
        :placeholder="placeholder"
        :rows="rows"
        :disabled="disabled"
        @update:value="handleInput"
      />
      
      <div v-if="showStats" class="editor-stats">
        <n-text depth="3">
          字符数: {{ content.length }} | 行数: {{ lineCount }}
        </n-text>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { NIcon } from 'naive-ui'
import { TrashOutline, CodeSlashOutline, PlayOutline } from '@vicons/ionicons5'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '代码编辑器'
  },
  placeholder: {
    type: String,
    default: '请输入代码...'
  },
  rows: {
    type: Number,
    default: 20
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  showFormat: {
    type: Boolean,
    default: false
  },
  showStats: {
    type: Boolean,
    default: true
  },
  executeText: {
    type: String,
    default: '执行'
  }
})

const emit = defineEmits(['update:modelValue', 'execute', 'clear', 'format'])

const content = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  content.value = newValue
})

const lineCount = computed(() => {
  return content.value.split('\n').length
})

const handleInput = (value) => {
  emit('update:modelValue', value)
}

const handleClear = () => {
  content.value = ''
  emit('update:modelValue', '')
  emit('clear')
}

const handleFormat = () => {
  emit('format', content.value)
}

const handleExecute = () => {
  emit('execute', content.value)
}
</script>

<style scoped>
.code-editor {
  width: 100%;
}

.editor-stats {
  margin-top: 8px;
  text-align: right;
}
</style>
