"use client"

import * as React from "react"
import Link from "next/link"
import { Menu } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  Sheet,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet"

// Définition des éléments de navigation
const navItems = [
  { href: "/", label: "Accueil" },
  { href: "/contact", label: "Contact" },
  { href: "/salles", label: "Salons" },
  { href: "/3D", label: "3D" },
]

const authItems = [
  { href: "/connexion", label: "Connexion" },
]

export function Navbar() {
  const [isOpen, setIsOpen] = React.useState(false)
  const [isLoggedIn, setIsLoggedIn] = React.useState(false)

  React.useEffect(() => {
    // Vérifiez si l'utilisateur est connecté
    const userToken = localStorage.getItem("user_email") // Exemple: Vérifiez une clé unique
    setIsLoggedIn(!!userToken) // Si un token existe, l'utilisateur est connecté
  }, [])

  const handleLogout = () => {
    localStorage.removeItem("user_email") // Supprimez le token ou toute info d'authentification
    setIsLoggedIn(false) // Mettez à jour l'état pour refléter la déconnexion
  }

  // Classe commune pour un style uniforme
  const buttonClass =
    "text-sm font-medium px-4 py-2 rounded-md transition-all duration-300 " +
    "focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary";

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <nav className="container flex items-center justify-between p-4">
        <Link href="/" className="flex items-center space-x-2">
          {/* Image du logo agrandie, deux fois plus grand */}
          <img src="/logo.png" alt="Logo" className="h-20 w-auto" />
          {/* Texte du logo */}
          <span className="text-2xl font-bold">WaterMine</span>
        </Link>
        {/* Navigation Desktop */}
        <div className="hidden md:flex space-x-4">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`${buttonClass} text-primary hover:bg-gray-100`}
            >
              {item.label}
            </Link>
          ))}
          {!isLoggedIn ? (
            <>
              {authItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`${buttonClass} text-primary hover:bg-gray-100`}
                >
                  {item.label}
                </Link>
              ))}
              <Link
                href="/inscription"
                className={`${buttonClass} bg-black text-white hover:bg-gray-800`}
              >
                Inscription
              </Link>
            </>
          ) : (
            <button
              className={`${buttonClass} bg-red-500 text-white hover:bg-red-600`}
              onClick={handleLogout}
            >
              Déconnexion
            </button>
          )}
        </div>
        {/* Navigation Mobile */}
        <Sheet open={isOpen} onOpenChange={setIsOpen}>
          <SheetTrigger asChild>
            <Button variant="ghost" className="md:hidden" size="icon">
              <Menu className="h-5 w-5" />
              <span className="sr-only">Toggle menu</span>
            </Button>
          </SheetTrigger>
          <SheetContent side="right">
            <nav className="flex flex-col space-y-4">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`${buttonClass} text-primary hover:bg-gray-100`}
                  onClick={() => setIsOpen(false)}
                >
                  {item.label}
                </Link>
              ))}
              {!isLoggedIn ? (
                <>
                  {authItems.map((item) => (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={`${buttonClass} text-primary hover:bg-gray-100`}
                      onClick={() => setIsOpen(false)}
                    >
                      {item.label}
                    </Link>
                  ))}
                  <Link
                    href="/inscription"
                    className={`${buttonClass} bg-black text-white hover:bg-gray-800`}
                    onClick={() => setIsOpen(false)}
                  >
                    Inscription
                  </Link>
                </>
              ) : (
                <button
                  className={`${buttonClass} bg-red-500 text-white hover:bg-red-600`}
                  onClick={() => {
                    handleLogout()
                    setIsOpen(false) // Fermez le menu mobile
                  }}
                >
                  Déconnexion
                </button>
              )}
            </nav>
          </SheetContent>
        </Sheet>
      </nav>
    </header>
  )
}
