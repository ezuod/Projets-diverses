<script setup lang="ts">
import { ref } from 'vue';
import { api } from '../../http-api.ts';

const target = ref<HTMLInputElement | null>(null);

function submitFile() {
    if (target.value && target.value.files && target.value.files.length > 0) {
        const file = target.value.files[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        api.createImage(formData)
            .then(() => {
                if (target.value) target.value.value = ''; // Reset input
                // Emit event to refresh gallery or handle via global state
                window.dispatchEvent(new Event('imageUploaded'));
            })
            .catch((e) => {
                console.log(e.message);
            });
    }
}

function handleFileUpload(event: Event) {
    target.value = event.target as HTMLInputElement;
    submitFile(); // Auto-submit on file selection
}
</script>

<template>
    <div class="upload-button">
        <label for="file-upload" class="plus-btn">+</label>
        <input id="file-upload" type="file" accept="image/png, image/jpeg" @change="handleFileUpload" hidden />
    </div>
</template>

<style scoped>
.upload-button {
    display: inline-block;
}

.plus-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background-color: #4a90e2;
    color: white;
    font-size: 1.5rem;
    border-radius: 50%;
    cursor: pointer;
    user-select: none;
}

.plus-btn:hover {
    background-color: #357abd;
}
</style>