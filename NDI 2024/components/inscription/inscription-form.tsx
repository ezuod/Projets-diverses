'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation' 
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import bcrypt from 'bcryptjs'

export default function InscriptionForm() {
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    prenom: '',
    nom: '',
    email: '',
    password: '',
  })

  const router = useRouter()

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target
    setFormData((prev) => ({ ...prev, [id]: value }))
  }

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onloadend = () => {
        setImagePreview(reader.result as string)
      }
      reader.readAsDataURL(file)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Crypter le mot de passe
    const hashedPassword = await bcrypt.hash(formData.password, 10);
  
    // Préparer les données pour l'enregistrement
    const userData = {
      ...formData,
      password: hashedPassword,
    };
  
    // Enregistrer les données dans un fichier JSON
    try {
      const response = await fetch('/api/save-user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
      });
  
      if (response.ok) {
        alert("Utilisateur enregistré avec succès !");
        router.push('/connexion')
      } else {
        const error = await response.json();
        alert(error.message || 'Une erreur est survenue.');
      }
    } catch (error: unknown) {
      if (error instanceof Error) {
        // Gérer l'erreur si c'est une instance d'Error
        alert('Erreur lors de l\'enregistrement : ' + error.message);
      } else {
        // Gérer les autres types d'erreurs
        alert('Erreur inconnue lors de l\'enregistrement.');
      }
    }
  };
  
  const containerStyle = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    backgroundImage: 'url("https://www.svgrepo.com/show/353912/waves.svg")', // Fond avec vagues
    backgroundColor: '#b3e5fc', // Couleur océanique
    backgroundSize: 'cover',
    backgroundRepeat: 'repeat-x',
    padding: '20px',
  }

  return (
      <Card style={{...containerStyle}}
        className="w-full max-w-lg mx-auto bg-gradient-to-br from-teal-400 via-sky-400 to-sky-500 shadow-xl rounded-xl" 
        style={{ padding: '20px', margin: '0 auto' }}
      >
        <CardHeader>
          <CardTitle className="text-5xl font-extrabold text-center text-white">Inscription</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="space-y-6" onSubmit={handleSubmit}>
            <div className="flex flex-col items-center space-y-4">
              <Avatar className="w-28 h-28">
                <AvatarImage src={imagePreview || ''} alt="Avatar" />
                <AvatarFallback>IMG</AvatarFallback>
              </Avatar>
              <Label htmlFor="picture" className="cursor-pointer text-sm text-teal-700 hover:text-teal-700 transition-all">
                Choisir une image
              </Label>
              <Input 
                id="picture" 
                type="file" 
                accept="image/*" 
                className="hidden" 
                onChange={handleImageChange}
              />
            </div>
            <div className="space-y-3">
              <Label htmlFor="prenom" className="text-gray-700">Prénom</Label>
              <Input 
                id="prenom" 
                placeholder="Votre prénom" 
                onChange={handleInputChange}
                className="border-2 border-teal-500 focus:ring focus:ring-teal-300"
              />
            </div>
            <div className="space-y-3">
              <Label htmlFor="nom" className="text-gray-700">Nom</Label>
              <Input 
                id="nom" 
                placeholder="Votre nom" 
                onChange={handleInputChange}
                className="border-2 border-teal-500 focus:ring focus:ring-teal-300"
              />
            </div>
            <div className="space-y-3">
              <Label htmlFor="email" className="text-gray-700">Email</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="votre@email.com" 
                onChange={handleInputChange}
                className="border-2 border-teal-500 focus:ring focus:ring-teal-300"
              />
            </div>
            <div className="space-y-3">
              <Label htmlFor="password" className="text-gray-700">Mot de passe</Label>
              <Input 
                id="password" 
                type="password" 
                placeholder="••••••••" 
                onChange={handleInputChange}
                className="border-2 border-teal-500 focus:ring focus:ring-teal-300"
              />
            </div>
            <Button 
              type="submit" 
              className="w-full bg-teal-500 hover:bg-teal-600 text-white font-bold py-2 px-4 rounded-md transition-all duration-200"
            >
              S'inscrire
            </Button>
          </form>
        </CardContent>
      </Card>
  )
}
