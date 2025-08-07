"use client";

import { useEffect, useState } from "react";
import { getPendingGames, approveGame } from "@/lib/api"; // Corrected API call
import type { Game } from "@/lib/types";
import {
Table,
TableBody,
TableCell,
TableHead,
TableHeader,
TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/use-toast";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from 'lucide-react';

export function GameApproval() {
const [pendingGames, setPendingGames] = useState<Game[]>([]);
const [loading, setLoading] = useState(true);
const { toast } = useToast();

const fetchGames = async () => {
  setLoading(true);
  try {
    // Use the new, dedicated endpoint for pending games
    const games = await getPendingGames();
    setPendingGames(games);
  } catch (error) {
    toast({
      variant: "destructive",
      title: "Error",
      description: "Failed to fetch pending games.",
    });
  } finally {
    setLoading(false);
  }
};

useEffect(() => {
  fetchGames();
}, []);

const handleApproval = async (gameId: number, status: "APPROVED" | "REJECTED") => {
  try {
    await approveGame(gameId, status);
    toast({
      title: "Success",
      description: `Game has been ${status.toLowerCase()}.`,
    });
    fetchGames(); // Refresh the list
  } catch (error: any) {
    toast({
      variant: "destructive",
      title: "Error",
      description: error.message || "Failed to update game status.",
    });
  }
};

if (loading) {
  return <div className="flex justify-center items-center p-8"><Loader2 className="h-8 w-8 animate-spin" /></div>;
}

return (
  <div>
    <h2 className="text-2xl font-semibold my-4">Pending Game Approvals</h2>
    {pendingGames.length > 0 ? (
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Game Name</TableHead>
            <TableHead>Developer ID</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {pendingGames.map((game) => (
            <TableRow key={game.id}>
              <TableCell>{game.name}</TableCell>
              <TableCell>{game.developerId}</TableCell>
              <TableCell>
                <Badge variant="secondary">{game.status}</Badge>
              </TableCell>
              <TableCell className="space-x-2">
                <Button size="sm" onClick={() => handleApproval(game.id, "APPROVED")}>Approve</Button>
                <Button variant="destructive" size="sm" onClick={() => handleApproval(game.id, "REJECTED")}>Reject</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    ) : (
      <p className="text-muted-foreground">No pending game approvals.</p>
    )}
  </div>
);
}
