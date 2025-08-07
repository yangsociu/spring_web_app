"use client";

import { useEffect, useState } from "react";
import { getMyGames } from "@/lib/api";
import type { Game } from "@/lib/types";
import { GameCard } from "@/components/game-card";
import { useToast } from "@/components/ui/use-toast";
import { Loader2 } from 'lucide-react';

export function MyGamesList() {
const [myGames, setMyGames] = useState<Game[]>([]);
const [loading, setLoading] = useState(true);
const { toast } = useToast();

useEffect(() => {
  const fetchMyGames = async () => {
    setLoading(true);
    try {
      const games = await getMyGames();
      setMyGames(games);
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Error",
        description: "Failed to fetch your games.",
      });
    } finally {
      setLoading(false);
    }
  };
  fetchMyGames();
}, []);

if (loading) {
  return <div className="flex justify-center items-center p-8"><Loader2 className="h-8 w-8 animate-spin" /></div>;
}

return (
  <div className="mt-6">
    <h2 className="text-2xl font-semibold mb-4">My Uploaded Games</h2>
    {myGames.length > 0 ? (
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {myGames.map((game) => (
          <GameCard key={game.id} game={game} showStatus={true} />
        ))}
      </div>
    ) : (
      <p className="text-muted-foreground">You haven't uploaded any games yet.</p>
    )}
  </div>
);
}
