'use client'

import React, { useRef, useEffect } from 'react'
import * as THREE from 'three'

const ThreeScene: React.FC = () => {
  const mountRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)

    // Créer un rendu WebGL
    const renderer = new THREE.WebGLRenderer()
    renderer.setSize(window.innerWidth, window.innerHeight)
    if (mountRef.current) {
      mountRef.current.appendChild(renderer.domElement)
    }

    // Créer une pyramide avec ConeGeometry
    const geometry = new THREE.ConeGeometry(1, 2, 4) // 1: rayon de base, 2: hauteur, 4: nombre de faces
    const material = new THREE.MeshBasicMaterial({ color: 0x0000ff }) // Couleur bleue
    const pyramid = new THREE.Mesh(geometry, material)
    scene.add(pyramid)

    // Positionner la caméra
    camera.position.z = 5

    // Fonction d'animation
    const animate = () => {
      requestAnimationFrame(animate)

      // Faire tourner la pyramide
      pyramid.rotation.x += 0.01
      pyramid.rotation.y += 0.01

      renderer.render(scene, camera)
    }

    animate()

    // Nettoyer à la destruction du composant
    return () => {
      if (mountRef.current) {
        mountRef.current.removeChild(renderer.domElement)
      }
    }
  }, [])

  return <div ref={mountRef}></div>
}

export default ThreeScene
