<script setup lang="ts">
import { ref } from 'vue';
import Upload from './other/Upload.vue';

const searchQuery = ref('');
const emit = defineEmits(['search']);

function handleSearch() {
    if (!searchQuery.value.trim()) {
        emit('search', '');
        return;
    }
    emit('search', searchQuery.value.trim());
}
</script>

<template>
    <div class="search-bar">
        <input v-model="searchQuery" type="text" placeholder="Type keywords..." @keyup.enter="handleSearch" />
        <button @click="handleSearch">Search</button>
        <Upload />
    </div>
</template>


<style scoped>
.search-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
}

.search-bar input {
    width: 50%;
    max-width: 600px;
    padding: 0.75rem;
    border: 1px solid #ccc;
    border-radius: 20px;
    font-size: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-bar button {
    padding: 0.75rem 1.5rem;
    background-color: #585858;
    color: white;
    border: none;
    border-radius: 20px;
    cursor: pointer;
}

.search-bar button:hover {
    background-color: #757575;
}
</style>