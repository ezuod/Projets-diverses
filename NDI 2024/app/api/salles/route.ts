import fs from 'fs'
import path from 'path'
import { NextResponse } from 'next/server'

// Fonction pour récupérer les messages d'un groupe spécifique
export async function GET(req: Request) {
    const url = new URL(req.url)
    const groupName = url.searchParams.get("groupName")
    const dataDirectory = path.join(process.cwd(), 'data')
    const filePath = path.join(dataDirectory, 'salles.json')

    if (!fs.existsSync(filePath)) {
        return NextResponse.json({ message: 'Fichier non trouvé.' }, { status: 404 })
    }

    try {
        const fileContents = fs.readFileSync(filePath, 'utf-8')
        const groups = JSON.parse(fileContents)

        if (groupName && groups[groupName]) {
            return NextResponse.json(groups[groupName], { status: 200 })
        } else {
            return NextResponse.json({ message: 'Groupe non trouvé.' }, { status: 404 })
        }
    } catch (error) {
        return NextResponse.json({ message: 'Erreur lors de la lecture du fichier.' }, { status: 500 })
    }
}

// Fonction pour ajouter un message à un groupe spécifique
export async function POST(req: Request) {
    const { groupName, email, prenom, content } = await req.json()

    const dataDirectory = path.join(process.cwd(), 'data')
    const filePath = path.join(dataDirectory, 'salles.json')

    if (!fs.existsSync(filePath)) {
        return NextResponse.json({ message: 'Fichier non trouvé.' }, { status: 404 })
    }

    try {
        const fileContents = fs.readFileSync(filePath, 'utf-8')
        const groups = JSON.parse(fileContents)

        // Vérifier si le groupe existe
        if (!groups[groupName]) {
            return NextResponse.json({ message: 'Groupe non trouvé.' }, { status: 404 })
        }

        // Créer un nouveau message
        const newMessage = {
            email,
            prenom,
            content,
            timestamp: new Date().toLocaleTimeString(),
        }

        // Ajouter le nouveau message au groupe
        groups[groupName].push(newMessage)

        // Sauvegarder le fichier JSON mis à jour (ajout du message dans le groupe)
        fs.writeFileSync(filePath, JSON.stringify(groups, null, 2))

        return NextResponse.json({ message: 'Message ajouté avec succès.' }, { status: 200 })
    } catch (error) {
        return NextResponse.json({ message: 'Erreur lors de l\'ajout du message.' }, { status: 500 })
    }
}
