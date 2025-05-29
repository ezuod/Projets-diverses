<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { api } from '../http-api';
import { useRouter } from 'vue-router';
import type { ImageType } from '../image';

import Image from './other/Image.vue';
import SearchBar from './Bar.vue';

const router = useRouter();
const imageList = ref<ImageType[]>([]);
const isDragging = ref(false);

onMounted(() => {
    getImageList();
    window.addEventListener('imageUploaded', getImageList);
});

onUnmounted(() => {
    window.removeEventListener('imageUploaded', getImageList);
});

function getImageList() {
    api.getImageList()
        .then((data) => {
            imageList.value = data;
        })
        .catch((e) => {
            console.error('Failed to fetch images:', e.message);
        });
}

function handleSearch(query: string) {
    if (!query) {
        getImageList();
        return;
    }
    api.searchImages(query)
        .then((data) => {
            imageList.value = data;
        })
        .catch((e) => {
            console.error('Search failed:', e.message);
            imageList.value = [];
        });
}

const handleClick = (id: number) => {
    router.push(`/view_image/${id}`);
};

function onDragOver(event: DragEvent) {
    event.preventDefault();
    isDragging.value = true;
}

function onDragLeave() {
    isDragging.value = false;
}

function onDrop(event: DragEvent) {
    event.preventDefault();
    isDragging.value = false;
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
        const file = files[0];
        if (file.type === 'image/png' || file.type === 'image/jpeg' || file.type === 'image/jpg') {
            const formData = new FormData();
            formData.append('file', file);
            api.createImage(formData)
                .then(() => {
                    window.dispatchEvent(new Event('imageUploaded'));
                })
                .catch((e) => {
                    console.error('Upload failed:', e.message);
                });
        } else {
            alert('Please drop a PNG or JPEG image.');
        }
    }
}
</script>

<template>
    <header>
        <SearchBar @search="handleSearch" />
    </header>
    <div class="gallery-container" :class="{ 'dragging': isDragging }" @dragover.prevent="onDragOver"
        @dragleave="onDragLeave" @drop.prevent="onDrop">
        <div class="gallery">
            <div v-for="image in imageList" :key="image.id" class="img-wrapper" @click="handleClick(image.id)">
                <Image :id="image.id" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.gallery-container {
    display: flex;
    justify-content: center;
    width: 100%;
    padding: 0;
    box-sizing: border-box;
}

.gallery-container.dragging {
    border: 3px dashed #2fc52d;
}

.gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 5px;
    padding: 5px;
    max-width: 1400px;
    width: 100%;
    box-sizing: border-box;
}

.img-wrapper {
    width: 100%;
    aspect-ratio: 1 / 1;
    overflow: hidden;
    cursor: pointer;
    background-color: #f0f0f0;
}

.img-wrapper figure {
    width: 100% !important;
    height: 100% !important;
    margin: 0;
    overflow: hidden;
}

.img-wrapper figure img {
    width: 100% !important;
    height: 100% !important;
    object-fit: cover;
    object-position: center;
}

/* https://stackoverflow.com/questions/13550541/media-min-width-max-width */

/* Desktop */
@media (min-width: 960px) {
    .gallery-container {
        padding: 0 10px;
    }

    .gallery {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 5px;
        padding: 5px;
    }
}

/* Mobile */
@media (max-width: 959px) {
    .gallery {
        grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
        gap: 3px;
        padding: 3px;
    }
}

/* webkit optimization */
@media (-webkit-min-device-pixel-ratio: 2),
(min-resolution: 192dpi) {
    .img-wrapper figure img {
        image-rendering: crisp-edges;
        image-rendering: -webkit-optimize-contrast;
    }
}
</style>