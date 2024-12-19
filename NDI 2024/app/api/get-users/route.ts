import fs from 'fs';
import path from 'path';
import { NextResponse } from 'next/server';

// Désactiver l'export statique pour cette route
export const dynamic = "force-dynamic";

export async function GET() {
  const dataDirectory = path.join(process.cwd(), 'data');
  const filePath = path.join(dataDirectory, 'users.json');

  if (!fs.existsSync(filePath)) {
    return NextResponse.json({ message: 'Fichier non trouvé.' }, { status: 404 });
  }

  try {
    const fileContents = fs.readFileSync(filePath, 'utf-8');
    const users = JSON.parse(fileContents);
    return NextResponse.json(users, { status: 200 });
  } catch (error) {
    return NextResponse.json({ message: 'Erreur lors de la lecture du fichier.' }, { status: 500 });
  }
}
