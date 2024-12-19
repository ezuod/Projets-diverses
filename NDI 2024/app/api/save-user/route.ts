import fs from 'fs';
import path from 'path';
import { NextResponse } from 'next/server';

// Définir un type pour l'utilisateur
interface User {
  prenom: string;
  nom: string;
  email: string;
  password: string;
}

// Fonction pour enregistrer un utilisateur dans le fichier JSON
export async function POST(req: Request) {
  try {
    // Lire les données envoyées par le formulaire
    const userData: User = await req.json(); // Typage de `userData` en tant que `User`

    // Définir le répertoire et le fichier où les utilisateurs seront enregistrés
    const dataDirectory = path.join(process.cwd(), 'data');
    const filePath = path.join(dataDirectory, 'users.json');

    // Lire les utilisateurs existants
    const existingUsers: User[] = fs.existsSync(filePath)
      ? JSON.parse(fs.readFileSync(filePath, 'utf-8'))
      : [];

    // Vérifier si un utilisateur avec le même email existe déjà
    const emailExists = existingUsers.some(user => user.email === userData.email);

    if (emailExists) {
      // Si l'email existe déjà, renvoyer une erreur
      return NextResponse.json(
        { message: 'Un utilisateur avec cet email existe déjà.' },
        { status: 400 }
      );
    }

    // Ajouter le nouvel utilisateur
    existingUsers.push(userData);

    // Enregistrer les utilisateurs dans le fichier JSON
    fs.writeFileSync(filePath, JSON.stringify(existingUsers, null, 2));

    return NextResponse.json({ message: 'Utilisateur enregistré avec succès!' }, { status: 200 });
  } catch (error) {
    console.error(error);
    return NextResponse.json({ message: 'Erreur lors de l\'enregistrement de l\'utilisateur.' }, { status: 500 });
  }
}
