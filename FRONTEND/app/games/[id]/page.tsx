"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Image from "next/image";
import { getGameById } from "@/lib/api";
import type { Game } from "@/lib/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Download, Gamepad, HardDrive, Info } from 'lucide-react';
import { motion } from "framer-motion";


export default function GameDetailPage() {
  const params = useParams();
  const [game, setGame] = useState<Game | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const gameId = Number(params.id);
    if (isNaN(gameId)) {
      setError("Invalid game ID.");
      setLoading(false);
      return;
    }

    const fetchGame = async () => {
      try {
        const fetchedGame = await getGameById(gameId);
        setGame(fetchedGame);
      } catch (err) {
        setError("Failed to load game details. It might not exist.");
      } finally {
        setLoading(false);
      }
    };

    fetchGame();
  }, [params.id]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <Skeleton className="h-[450px] w-full rounded-xl mb-8 bg-gray-800" />
        <div className="grid md:grid-cols-3 gap-8">
          <div className="md:col-span-2 space-y-4">
            <Skeleton className="h-96 w-full bg-gray-800" />
          </div>
          <div className="space-y-4">
            <Skeleton className="h-64 w-full bg-gray-800" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !game) {
    return (
      <div className="container mx-auto px-4 py-8 text-center">
        <h1 className="text-2xl font-bold">An Error Occurred</h1>
        <p className="text-muted-foreground">{error}</p>
      </div>
    );
  }

  // UPDATED: Use the full URL directly, with a fallback.
  const imageUrl = game.previewImageUrl || "/stylized-game-scene.png";

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      <div className="relative h-[300px] md:h-[450px] w-full">
        <Image
          src={imageUrl || "/placeholder.svg"}
          alt={`Banner for ${game.name}`}
          fill
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-950 to-transparent" />
      </div>

      <div className="container mx-auto max-w-6xl px-4 pb-12 -mt-24 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
            >
              <Card className="bg-gray-900/80 backdrop-blur-sm border-gray-700">
                <CardHeader>
                  <CardTitle className="text-4xl font-extrabold tracking-tight text-white">{game.name}</CardTitle>
                  <p className="text-muted-foreground">Developed by: User {game.developerId}</p>
                </CardHeader>
                <CardContent>
                  <p className="text-gray-300 whitespace-pre-wrap leading-relaxed">{game.description || "No description provided."}</p>
                </CardContent>
              </Card>
            </motion.div>
          </div>
          <div className="lg:col-span-1 space-y-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
            >
              <Card className="bg-gray-900/80 backdrop-blur-sm border-gray-700">
                <CardHeader>
                  <CardTitle className="flex items-center text-xl"><HardDrive className="mr-3 h-5 w-5 text-primary" />System Requirements</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-gray-400 whitespace-pre-wrap">{game.requirements || "No requirements specified."}</p>
                </CardContent>
              </Card>
              <Card className="bg-gray-900/80 backdrop-blur-sm border-gray-700 mt-6">
                <CardHeader>
                  <CardTitle className="flex items-center text-xl"><Info className="mr-3 h-5 w-5 text-primary" />Features</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-wrap gap-2">
                  {game.supportLeaderboard ? <Badge variant="secondary">Leaderboard</Badge> : <Badge variant="outline">No Leaderboard</Badge>}
                  {game.supportPoints ? <Badge variant="secondary">Points System</Badge> : <Badge variant="outline">No Points System</Badge>}
                </CardContent>
              </Card>
              <Button size="lg" className="w-full flex items-center mt-6" asChild>
                <a href={game.apkFileUrl} target="_blank" rel="noopener noreferrer">
                  <Download className="mr-2 h-5 w-5" />
                  Download Now
                </a>
              </Button>
            </motion.div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
