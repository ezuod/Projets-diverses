'use client'

import { useState, useEffect } from 'react'
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Users, Send } from 'lucide-react'
import { cn } from "@/lib/utils"
import { useRouter } from 'next/navigation'

type Message = {
  email: string
  prenom: string
  content: string
  timestamp: string
}

export default function MessengerGroups() {
  const router = useRouter()
  const [selectedGroup, setSelectedGroup] = useState<string | null>(null)
  const [groups, setGroups] = useState<string[]>([])
  const [groupMessages, setGroupMessages] = useState<Message[]>([])
  const [newMessage, setNewMessage] = useState("")
  const [email, setEmail] = useState<string>("")
  const [prenom, setPrenom] = useState<string>("")
  const [loading, setLoading] = useState(true) // Indicateur de chargement

  useEffect(() => {
    const initData = async () => {
      try {
        const userEmail = localStorage.getItem('user_email')
        if (!userEmail) {
          console.error("Aucun email trouvé dans le localStorage")
          router.push('/connexion')
          return
        }

        setEmail(userEmail)
        const extractedPrenom = userEmail.split('@')[0]
        setPrenom(extractedPrenom.charAt(0).toUpperCase() + extractedPrenom.slice(1))

        const groupRes = await fetch('/api/groupes')
        if (!groupRes.ok) throw new Error('Erreur lors du chargement des groupes')
        const groupData = await groupRes.json()
        setGroups(groupData)
      } catch (error) {
        console.error("Erreur d'initialisation :", error)
      } finally {
        setLoading(false) // Chargement terminé
      }
    }

    initData()
  }, [])

  useEffect(() => {
    if (!selectedGroup) return

    const fetchMessages = async () => {
      try {
        const res = await fetch(`/api/salles?groupName=${selectedGroup}`)
        if (!res.ok) throw new Error('Erreur lors du chargement des messages')
        const data = await res.json()
        setGroupMessages(data)
      } catch (error) {
        console.error("Erreur lors du chargement des messages :", error)
      }
    }

    fetchMessages()
  }, [selectedGroup])

  const handleSendMessage = async () => {
    if (newMessage.trim() !== "") {
      const now = new Date()
      const date = now.toLocaleDateString('fr-FR', {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      })
      const time = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })

      const newMessageObj = {
        email,
        prenom,
        content: newMessage,
        timestamp: `${time} | ${date}`,
      }

      try {
        const response = await fetch('/api/salles', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            groupName: selectedGroup,
            email,
            prenom,
            content: newMessage,
          }),
        })

        if (response.ok) {
          setGroupMessages([...groupMessages, newMessageObj])
          setNewMessage("")
        } else {
          console.error('Erreur lors de l\'envoi du message')
        }
      } catch (error) {
        console.error('Erreur lors de l\'envoi du message', error)
      }
    }
  }

  if (loading) {
    return <p className="text-center text-gray-500 mt-8">Chargement des données...</p>
  }

  return (
    <div className="w-full max-w-6xl mx-auto mt-8">
      <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-4">
        <Card className="w-full h-[500px] bg-white overflow-hidden">
          <CardContent className="p-0 h-full">
            <div className="p-4 border-b">
              <h2 className="text-lg font-semibold">Groupes</h2>
            </div>
            <ScrollArea className="h-[calc(100%_-_64px)]">
              {groups.map((group) => (
                <div
                  key={group}
                  className={cn(
                    "flex items-center space-x-4 p-4 hover:bg-blue-50 cursor-pointer",
                    selectedGroup === group ? "bg-blue-100" : ""
                  )}
                  onClick={() => setSelectedGroup(group)}
                >
                  <Avatar>
                    <AvatarImage src="/placeholder.svg?height=40&width=40" alt={group} />
                    <AvatarFallback>{group.substring(0, 2)}</AvatarFallback>
                  </Avatar>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-blue-600 truncate">{group}</p>
                  </div>
                </div>
              ))}
            </ScrollArea>
          </CardContent>
        </Card>

        <Card className="w-full h-[500px] bg-white overflow-hidden">
          <CardContent className="p-0 h-full flex flex-col">
            <div className="p-4 border-b border-gray-200 bg-white">
              <h2 className="text-lg font-semibold flex items-center text-blue-600">
                <Users className="mr-2" />
                {selectedGroup || "Sélectionnez un groupe"}
              </h2>
            </div>
            <ScrollArea className="flex-1 overflow-y-auto">
              <div className="p-4 space-y-4">
                {groupMessages.length > 0 ? (
                  groupMessages.map((message, index) => (
                    <div
                      key={index}
                      className={cn(
                        "flex flex-col p-3 max-w-[80%] mb-2 rounded-lg",
                        message.email === email
                          ? "bg-blue-100 text-white self-start"
                          : "bg-gray-100 text-gray-800 self-end"
                      )}
                    >
                      <div className="flex items-center space-x-2">
                        <span className="font-semibold text-blue-700">{message.prenom}</span>
                        <span className="text-xs text-blue-400">{message.timestamp}</span>
                      </div>
                      <p className="mt-1">{message.content}</p>
                    </div>
                  ))
                ) : (
                  <p className="text-center text-gray-500">Aucun message dans ce groupe</p>
                )}
              </div>
            </ScrollArea>
            <div className="p-4 border-t border-gray-200 bg-white flex-shrink-0">
              <div className="flex space-x-2">
                <Input
                  placeholder="Écrivez un message..."
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                  className="flex-grow"
                />
                <Button onClick={handleSendMessage} className="bg-blue-500 hover:bg-blue-600">
                  <Send className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
