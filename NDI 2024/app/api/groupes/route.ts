// api/salles.ts
import fs from 'fs'
import path from 'path'
import { NextResponse } from 'next/server'

// Fonction pour récupérer les groupes depuis salles.json
export async function GET() {
  const filePath = path.join(process.cwd(), 'data', 'salles.json')

  if (!fs.existsSync(filePath)) {
    return NextResponse.json({ message: 'Fichier non trouvé.' }, { status: 404 })
  }

  try {
    const fileContents = fs.readFileSync(filePath, 'utf-8')
    const groupes = JSON.parse(fileContents)

    // Récupérer les clés (les groupes)
    const groupNames = Object.keys(groupes)
    
    return NextResponse.json(groupNames, { status: 200 })
  } catch (error) {
    return NextResponse.json({ message: 'Erreur lors de la lecture du fichier.' }, { status: 500 })
  }
}
