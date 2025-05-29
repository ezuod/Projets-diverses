<script setup lang="ts">
import { useRoute } from 'vue-router';
const route = useRoute();

import { ref, onMounted } from 'vue';

const darkMode = ref(false);

const updateBodyClass = () => {
  if (darkMode.value) {
    document.body.classList.add('dark');
  } else {
    document.body.classList.remove('dark');
  }
};

onMounted(() => {
  const saved = localStorage.getItem('darkMode') 
  if (saved === 'true') darkMode.value = true;
  updateBodyClass();
});

const toggleDarkMode = () => {
  darkMode.value = !darkMode.value;
  localStorage.setItem('darkMode', darkMode.value.toString());
  updateBodyClass();
};

</script>

<template>
  <div id="app">
    <button @click="toggleDarkMode" style="position: fixed; top: 1rem; right: 1rem; z-index: 999;">
      {{ darkMode ? '☀️ Light Mode' : '🌙 Dark Mode' }}
    </button>
    <main :class="{ 'with-padding': route.name !== 'view' }">
      <router-view />
    </main>
  </div>
</template>

<style>
body {
  background-color: white;
  color: black;
  transition: background-color 0.3s ease, color 0.3s ease;
}

body.dark {
  background-color:rgb(42, 38, 38);
  color: #f0f0f0;
}

main.with-padding {
  padding: 2rem;
  margin-top: 60px;
}

main {
  padding: 0;
  margin: 0;
}

header {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  padding: 1rem;
  padding-top: 1.5rem;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
}

body.dark header {
  background-color: rgba(20, 20, 20, 0.7);
}
</style>