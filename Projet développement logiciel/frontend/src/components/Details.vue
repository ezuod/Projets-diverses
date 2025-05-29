<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '../http-api';

// Define the interface for image details
interface ImageDetails {
    id: number;
    name: string;
    type: string;
    size: string;
}

const props = defineProps<{ id: number }>();
const details = ref<ImageDetails | null>(null);

onMounted(async () => {
    try {
        const data = await api.getImageDetails(props.id);
        details.value = data;
    } catch (e) {
        console.error('Failed to fetch image details:', e);
    }
});
</script>

<template>
    <div v-if="details" class="details-container">
        <div>
            <h4 class="details-name">{{ details.name }}</h4>
            <p class="details-meta">
                Type: {{ details.type }} | Size: {{ details.size }}
            </p>
        </div>
    </div>
    <div v-else>
        <p>Loading image details...</p>
    </div>
</template>

<style scoped>
.details-container {
    display: flex;
    margin-left: 15%;
    align-items: center;
    margin-bottom: 1rem;
}

.details-name {
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
}

.details-meta {
    margin: 0;
    font-size: 0.85rem;
    color: #777;
}
</style>
