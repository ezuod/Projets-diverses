<script setup lang="ts">
import { api } from '../../http-api.ts';

const props = defineProps<{ id: number }>()

api.getImage(props.id)
  .then((data: Blob) => {
    const reader = new window.FileReader();
    reader.readAsDataURL(data);
    reader.onload = () => {
      const galleryElt = document.getElementById("gallery-" + props.id);
      if (galleryElt !== null) {
        const imgElt = document.createElement("img");
        galleryElt.appendChild(imgElt);
        if (imgElt !== null && reader.result as string) {
          imgElt.setAttribute("src", (reader.result as string));
        }
      }
    };
  })
  .catch(e => {
    console.log(e.message);
  });
</script>

<template>
  <figure :id="'gallery-' + id"></figure>
</template>

<style>
figure {
  width: auto;
  height: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

figure img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>