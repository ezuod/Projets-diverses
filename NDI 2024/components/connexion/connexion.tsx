'use client'

import { useState } from 'react'
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card"

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    try {
      // Envoi de la requête de connexion à l'API
      const response = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      const data = await response.json()

      if (response.ok) {
        // Enregistrer l'email de l'utilisateur dans le localStorage
        localStorage.setItem('user_email', email)

        // Redirection vers la page d'accueil ou un autre endroit
        window.location.href = '/' // Par exemple, rediriger vers un tableau de bord
      } else {
        setError(data.message) // Afficher l'erreur si la connexion échoue
      }
    } catch (error) {
      setError('Erreur lors de la connexion')
    }
  }

  return (
      <Card className="w-full max-w-lg bg-white shadow-xl rounded-xl" style={{ padding: '20px', margin: '0 auto' }}>
        <CardHeader>
          <CardTitle className="text-4xl font-extrabold text-teal-600">Connexion</CardTitle>
          <CardDescription className="text-gray-700">Entrez vos identifiants pour vous connecter</CardDescription>
        </CardHeader>
        <CardContent className="space-y-7">
          {error && <p className="text-red-600">{error}</p>}
          <div className="space-y-3">
            <Label htmlFor="email" className="text-gray-700">Adresse e-mail</Label>
            <Input
              id="email"
              type="email"
              placeholder="exemple@domaine.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="border-2 border-teal-400 focus:ring focus:ring-teal-500"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password" className="text-gray-700">Mot de passe</Label>
            <Input
              id="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="border-2 border-teal-400 focus:ring focus:ring-teal-500"
            />
          </div>
        </CardContent>
        <CardFooter>
          <Button className="w-full bg-teal-500 hover:bg-teal-600 text-white font-bold py-2 px-4 rounded-md transition-all duration-200" onClick={handleSubmit}>
            Se connecter
          </Button>
        </CardFooter>
      </Card>
  )
}
