import CerveauScene from '@/components/3d/corps'
import ThreeScene from '@/components/3d/ThreeScene'
import { Navbar } from '@/components/navbar/navbar'
import React from 'react'

const HomePage = () => {
  return (
    <div>
        <Navbar/>
        <h1>Welcome to Three.js with Next.js</h1>
        <CerveauScene/>
    </div>
  )
}

export default HomePage
