"use client"

import type React from "react"

import { useState } from "react"
import { createGame } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Checkbox } from "@/components/ui/checkbox"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useToast } from "@/hooks/use-toast"
import { Upload } from "lucide-react"

export function GameUploadForm({ onGameCreated }: { onGameCreated?: () => void }) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    requirements: "",
    apkFileUrl: "",
    supportLeaderboard: false,
    supportPoints: false,
  })
  const [previewImage, setPreviewImage] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!previewImage) {
      toast({
        variant: "destructive",
        title: "Error",
        description: "Please select a preview image.",
      })
      return
    }

    setLoading(true)
    try {
      const data = new FormData()
      data.append("name", formData.name)
      data.append("description", formData.description)
      data.append("requirements", formData.requirements)
      data.append("apkFileUrl", formData.apkFileUrl)
      data.append("supportLeaderboard", formData.supportLeaderboard.toString())
      data.append("supportPoints", formData.supportPoints.toString())
      data.append("previewImage", previewImage)

      await createGame(data)

      toast({
        title: "Success",
        description: "Game uploaded successfully! Waiting for admin approval.",
      })

      // Reset form
      setFormData({
        name: "",
        description: "",
        requirements: "",
        apkFileUrl: "",
        supportLeaderboard: false,
        supportPoints: false,
      })
      setPreviewImage(null)

      if (onGameCreated) {
        onGameCreated()
      }
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to upload game.",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Upload className="w-5 h-5" />
          Upload New Game
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="name">Game Name *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>

          <div>
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              rows={3}
            />
          </div>

          <div>
            <Label htmlFor="requirements">System Requirements</Label>
            <Textarea
              id="requirements"
              value={formData.requirements}
              onChange={(e) => setFormData({ ...formData, requirements: e.target.value })}
              rows={2}
            />
          </div>

          <div>
            <Label htmlFor="previewImage">Preview Image *</Label>
            <Input
              id="previewImage"
              type="file"
              accept="image/*"
              onChange={(e) => setPreviewImage(e.target.files?.[0] || null)}
              required
            />
          </div>

          <div>
            <Label htmlFor="apkFileUrl">APK Download URL *</Label>
            <Input
              id="apkFileUrl"
              type="url"
              value={formData.apkFileUrl}
              onChange={(e) => setFormData({ ...formData, apkFileUrl: e.target.value })}
              placeholder="https://example.com/game.apk"
              required
            />
          </div>

          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="supportLeaderboard"
                checked={formData.supportLeaderboard}
                onCheckedChange={(checked) => setFormData({ ...formData, supportLeaderboard: checked as boolean })}
              />
              <Label htmlFor="supportLeaderboard">Support Leaderboard</Label>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="supportPoints"
                checked={formData.supportPoints}
                onCheckedChange={(checked) => setFormData({ ...formData, supportPoints: checked as boolean })}
              />
              <Label htmlFor="supportPoints">Support Points System</Label>
            </div>
          </div>

          <Button type="submit" disabled={loading} className="w-full">
            {loading ? "Uploading..." : "Upload Game"}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
