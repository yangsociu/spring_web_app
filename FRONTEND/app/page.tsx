"use client"

import { useAuth } from "@/components/auth-provider"
import { Navbar } from "@/components/navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Gamepad2, Users, Code, Palette, Download, Star, Trophy, Zap, Shield, Heart } from "lucide-react"
import Link from "next/link"

export default function HomePage() {
  const { user, loading } = useAuth()

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background to-muted">
      <Navbar />

      {/* Hero Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto text-center">
          <div className="max-w-4xl mx-auto">
            <Badge variant="secondary" className="mb-4">
              <Zap className="h-3 w-3 mr-1" />
              Now Available
            </Badge>
            <h1 className="text-4xl md:text-6xl font-bold mb-6 bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
              Welcome to GameHub
            </h1>
            <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
              The ultimate gaming platform where players discover amazing games and developers showcase their
              creativity. Join our community of gamers, designers, and developers.
            </p>
            {!user && (
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button size="lg" asChild>
                  <Link href="/auth">Get Started</Link>
                </Button>
                <Button size="lg" variant="outline">
                  Learn More
                </Button>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4 bg-muted/50">
        <div className="container mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">Why Choose GameHub?</h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Experience gaming like never before with our comprehensive platform designed for everyone.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <Card className="text-center">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-4">
                  <Gamepad2 className="h-6 w-6 text-primary" />
                </div>
                <CardTitle>For Players</CardTitle>
                <CardDescription>Discover and play amazing games from talented developers worldwide</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li>• Curated game collection</li>
                  <li>• Community reviews</li>
                  <li>• Achievement system</li>
                  <li>• Social features</li>
                </ul>
              </CardContent>
            </Card>

            <Card className="text-center">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-green-500/10 rounded-lg flex items-center justify-center mb-4">
                  <Code className="h-6 w-6 text-green-500" />
                </div>
                <CardTitle>For Developers</CardTitle>
                <CardDescription>Showcase your games and connect with players and other developers</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li>• Game publishing platform</li>
                  <li>• Analytics dashboard</li>
                  <li>• Developer community</li>
                  <li>• Monetization tools</li>
                </ul>
              </CardContent>
            </Card>

            <Card className="text-center">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-purple-500/10 rounded-lg flex items-center justify-center mb-4">
                  <Palette className="h-6 w-6 text-purple-500" />
                </div>
                <CardTitle>For Designers</CardTitle>
                <CardDescription>Create stunning game assets and collaborate with development teams</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li>• Asset marketplace</li>
                  <li>• Portfolio showcase</li>
                  <li>• Collaboration tools</li>
                  <li>• Design challenges</li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto">
          <div className="grid md:grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold text-primary mb-2">10K+</div>
              <div className="text-muted-foreground">Active Players</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-green-500 mb-2">500+</div>
              <div className="text-muted-foreground">Games Published</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-purple-500 mb-2">200+</div>
              <div className="text-muted-foreground">Developers</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-orange-500 mb-2">50+</div>
              <div className="text-muted-foreground">Designers</div>
            </div>
          </div>
        </div>
      </section>

      {/* Community Section */}
      <section className="py-20 px-4 bg-muted/50">
        <div className="container mx-auto">
          <div className="max-w-4xl mx-auto text-center">
            <h2 className="text-3xl md:text-4xl font-bold mb-6">Join Our Community</h2>
            <p className="text-xl text-muted-foreground mb-12">
              Connect with fellow gamers, share experiences, and be part of something bigger.
            </p>

            <div className="grid md:grid-cols-3 gap-6 mb-12">
              <div className="flex items-center justify-center space-x-2">
                <Users className="h-5 w-5 text-primary" />
                <span>Active Community</span>
              </div>
              <div className="flex items-center justify-center space-x-2">
                <Trophy className="h-5 w-5 text-yellow-500" />
                <span>Competitions</span>
              </div>
              <div className="flex items-center justify-center space-x-2">
                <Heart className="h-5 w-5 text-red-500" />
                <span>Support System</span>
              </div>
            </div>

            {user && user.role === "PLAYER" && (
              <Card className="max-w-2xl mx-auto">
                <CardHeader>
                  <CardTitle className="flex items-center justify-center space-x-2">
                    <Star className="h-5 w-5 text-yellow-500" />
                    <span>Welcome, Player!</span>
                  </CardTitle>
                  <CardDescription>
                    You're all set to explore amazing games and connect with the community.
                  </CardDescription>
                </CardHeader>
              </Card>
            )}

            {user && (user.role === "DESIGNER" || user.role === "DEVELOPER") && (
              <Card className="max-w-2xl mx-auto border-orange-200 bg-orange-50">
                <CardHeader>
                  <CardTitle className="flex items-center justify-center space-x-2 text-orange-700">
                    <Shield className="h-5 w-5" />
                    <span>Account Under Review</span>
                  </CardTitle>
                  <CardDescription className="text-orange-600">
                    Your {user.role.toLowerCase()} account is pending admin approval. You'll receive access to all
                    features once approved.
                  </CardDescription>
                </CardHeader>
              </Card>
            )}
          </div>
        </div>
      </section>

      {/* Download Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto text-center">
          <div className="max-w-2xl mx-auto">
            <h2 className="text-3xl md:text-4xl font-bold mb-6">Get the Mobile App</h2>
            <p className="text-xl text-muted-foreground mb-8">
              Take GameHub with you wherever you go. Download our mobile app for the complete gaming experience.
            </p>

            <Card className="p-8">
              <div className="flex items-center justify-center mb-6">
                <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center">
                  <Download className="h-8 w-8 text-primary" />
                </div>
              </div>
              <h3 className="text-2xl font-bold mb-4">GameHub Mobile</h3>
              <p className="text-muted-foreground mb-6">
                Experience all features on your mobile device with our native app.
              </p>
              <Button size="lg" className="w-full sm:w-auto" disabled>
                <Download className="h-4 w-4 mr-2" />
                Download APK (Coming Soon)
              </Button>
              <p className="text-sm text-muted-foreground mt-4">
                The download link will be available soon. Stay tuned for updates!
              </p>
            </Card>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t py-12 px-4">
        <div className="container mx-auto">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-2 mb-4 md:mb-0">
              <Gamepad2 className="h-6 w-6 text-primary" />
              <span className="font-bold text-lg">GameHub</span>
            </div>
            <div className="text-sm text-muted-foreground">© 2024 GameHub. All rights reserved.</div>
          </div>
        </div>
      </footer>
    </div>
  )
}
