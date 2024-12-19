'use client'

import React, { useEffect, useRef } from 'react'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'

const CerveauScene: React.FC = () => {
  const mountRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    // Initialisation de la scène, de la caméra et du rendu
    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
    const renderer = new THREE.WebGLRenderer()
    renderer.setSize(window.innerWidth, window.innerHeight)

    if (mountRef.current) {
      mountRef.current.appendChild(renderer.domElement)
    }

    // Créer une lumière pour voir les objets
    const light = new THREE.AmbientLight(0x404040, 1) // lumière ambiante
    scene.add(light)

    const directionalLight = new THREE.DirectionalLight(0xffffff, 1)
    directionalLight.position.set(5, 5, 5).normalize()
    scene.add(directionalLight)

    // Charger le modèle GLB depuis le dossier public
    const loader = new GLTFLoader()
    loader.load('/models/cerveau.glb', (gltf) => {
      const model = gltf.scene
      scene.add(model)

      // Ajuster la position du modèle si nécessaire
      model.scale.set(1, 1, 1) // Redimensionner le modèle si nécessaire
      model.position.set(0, 0, 0) // Positionner le modèle

      // Animer le modèle (exemple de rotation continue)
      const animate = () => {
        requestAnimationFrame(animate)

        model.rotation.x += 0.01
        model.rotation.y += 0.01

        renderer.render(scene, camera)
      }

      animate()
    }, undefined, (error) => {
      console.error('Erreur lors du chargement du modèle:', error)
    })

    // Positionner la caméra
    camera.position.z = 5

    // Nettoyage lors de la destruction du composant
    return () => {
      if (mountRef.current) {
        mountRef.current.removeChild(renderer.domElement)
      }
    }
  }, [])

  return <div ref={mountRef}></div>
}

export default CerveauScene
