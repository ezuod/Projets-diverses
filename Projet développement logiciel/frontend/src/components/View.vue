<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../http-api';
import Image from './other/Image.vue';
import Details from './Details.vue';
import { FastAverageColor } from 'fast-average-color';

const route = useRoute();
const router = useRouter();
const id = ref(Number(route.params.id) || 0);
const numberOfSimilar = ref(5);
const similarImages = ref<any[]>([]);
const averageColor = ref<string>('#FEE7F0');

const updateBackgroundColor = async () => {
    const fac = new FastAverageColor();
    const imgElement = document.querySelector('.image-container img') as HTMLImageElement;
    if (imgElement && imgElement.complete) {
        try {
           const color = await fac.getColorAsync(imgElement);
           averageColor.value = color.hex;
        } catch (error) {
            console.error('Erreur lors du calcul de la couleur moyenne :', error);
        }
    } else {
        imgElement?.addEventListener('load', async () => {
            const color = await fac.getColorAsync(imgElement);
            averageColor.value = color.hex;
        });
    }
};

watch(id, async () => {
    await updateBackgroundColor();
});

onMounted(() => {
    updateBackgroundColor();
});

const deleteImage = async () => {
    try {
        await api.deleteImage(id.value);
        alert('Image deleted successfully');
        router.push('/gallery');
    } catch (error) {
        console.error('Failed to delete image:', error);
        alert('Failed to delete image');
    }
};

// fetch similar
const fetchSimilarImages = async (n: number) => {
    try {
        const data = await api.getSimilarImages(id.value, n);
        similarImages.value = data;
    } catch (e) {
        console.error('Error fetching similar images:', e);
    }
};

const handleClick = (imageId: number) => {
    router.push({ name: 'view', params: { id: imageId.toString() } });
};

const downloadImage = async () => {
    try {
        const blob = await api.getImage(id.value);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `image-${id.value}`;
        a.click();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Error downloading image:', error);
    }
};

// to update id and data
watch(() => route.params.id, (newId) => {
    id.value = Number(newId) || 0;
    fetchSimilarImages(numberOfSimilar.value);
}, { immediate: true });

// changes in numberOfSimilar 
watch(numberOfSimilar, (newValue) => {
    if (newValue > 0) {
        fetchSimilarImages(newValue);
    }
});

// update similar images
watch(id, () => {
    fetchSimilarImages(numberOfSimilar.value);
}, { immediate: true });

defineProps<{ id?: number }>();
</script>

<template>
    <div class="buttons-container">
        <button @click="router.push('/gallery');" class="return-button">Return</button>
        <button @click="deleteImage" class="delete-button">Delete</button>
        <button @click="downloadImage" class="download-button">Download</button>
    </div>

    <!-- Force re-render by using key with id -->
    <Details :id="id" :key="id" />
    <div class="image-container" :style="{ backgroundColor: averageColor }">
        <Image :id="id" :key="id" class="img" />
    </div>

    <div class="similar-controls">
        <h4>Similar to this one:</h4>
        <select id="number-select" v-model="numberOfSimilar" class="number-select">
            <option value="1">1</option>
            <option value="3">3</option>
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
        </select>
    </div>

    <div v-bind="$attrs" class="gallery">
        <div v-for="image in similarImages" :key="image.id" class="img-wrapper" @click="handleClick(image.id)">
            <Image :id="image.id" class="image-similar" />
            <span class="distance">{{ image.distance.toFixed(2) }}</span>
        </div>
    </div>
</template>

<style scoped>
.image-container {
    width: 100%;
    height: 70%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #FEE7F0;
    overflow: hidden;
    transition: background-color 0.5s ease;
}
.img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.delete-button {
    position: absolute;
    right: 5%;
    padding: 0.5rem 1rem;
}

.buttons-container {
    margin-top: 1rem;
}

.return-button {
    position: absolute;
    left: 5%;
    padding: 0.5rem 1rem;
}

.download-button {
    position: absolute;
    right: 15%;
    padding: 0.5rem 1rem;
}

.img-wrapper {
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.image-similar {
    width: 100px;
    height: 100px;
    object-fit: cover;
}

.similar-controls {
    margin: 1rem 0;
    display: flex;
    align-items: center;
    gap: 1rem;
}

.number-select {
    padding: 0.5rem;
    border-radius: 4px;
    border: 1px solid #ccc;
}

.distance {
    font-size: 0.9rem;
    color: #666;
}

.gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;
    padding: 10px;
}
</style>