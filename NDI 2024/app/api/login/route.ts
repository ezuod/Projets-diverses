import fs from 'fs'
import path from 'path'
import bcrypt from 'bcryptjs'
import { NextResponse } from 'next/server'

export async function POST(req: Request) {
  try {
    const { email, password } = await req.json()

    // Chemin vers le fichier JSON contenant les utilisateurs
    const dataDirectory = path.join(process.cwd(), 'data')
    const filePath = path.join(dataDirectory, 'users.json')

    // Lire les utilisateurs existants
    const users = fs.existsSync(filePath) ? JSON.parse(fs.readFileSync(filePath, 'utf-8')) : []

    // Trouver l'utilisateur avec l'email donné
    const user = users.find((user: { email: string }) => user.email === email)

    if (!user) {
      return NextResponse.json({ message: 'Utilisateur non trouvé' }, { status: 404 })
    }

    // Vérifier le mot de passe en utilisant bcrypt
    const isPasswordValid = await bcrypt.compare(password, user.password)

    if (!isPasswordValid) {
      return NextResponse.json({ message: 'Mot de passe incorrect' }, { status: 401 })
    }

    // Si l'authentification est réussie, vous pouvez renvoyer un message de succès ou un token
    return NextResponse.json({ message: 'Connexion réussie' }, { status: 200 })

  } catch (error) {
    return NextResponse.json({ message: 'Erreur lors de la connexion' }, { status: 500 })
  }
}
