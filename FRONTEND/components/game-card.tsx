import Link from "next/link";
import {
Card,
CardContent,
CardFooter,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { Game } from "@/lib/types";
import Image from "next/image";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

interface GameCardProps {
  game: Game;
  showStatus?: boolean;
}

const IMAGE_FILE_BASE_URL = "http://localhost:8080/uploads/";

export function GameCard({ game, showStatus = false }: GameCardProps) {
  const getStatusClasses = (status: Game["status"]) => {
    switch (status) {
      case "APPROVED":
        return "bg-green-500/20 text-green-400 border-green-500/30";
      case "PENDING":
        return "bg-yellow-500/20 text-yellow-400 border-yellow-500/30";
      case "REJECTED":
        return "bg-red-500/20 text-red-400 border-red-500/30";
      default:
        return "bg-gray-500/20 text-gray-400 border-gray-500/30";
    }
  };

  const imageUrl = game.previewImageUrl
    ? `${IMAGE_FILE_BASE_URL}${game.previewImageUrl}`
    : "/stylized-game-scene.png";

  return (
    <motion.div
      whileHover={{ y: -5 }}
      transition={{ duration: 0.2 }}
      className="h-full"
    >
      <Link href={`/games/${game.id}`} className="block h-full">
        <Card className="flex flex-col h-full bg-gray-900 border-gray-800 hover:border-primary transition-colors duration-300 overflow-hidden">
          <div className="relative aspect-video w-full">
            <Image
              src={imageUrl || "/placeholder.svg"}
              alt={`Preview for ${game.name}`}
              fill
              className="object-cover"
            />
          </div>
          <CardContent className="p-4 flex-1">
            <h3 className="font-bold text-lg text-white">{game.name}</h3>
            <p className="mt-1 text-sm text-gray-400 line-clamp-2">
              {game.description}
            </p>
          </CardContent>
          <CardFooter className="p-4 pt-0">
            {showStatus ? (
              <div className="w-full">
                <Badge className={cn("w-full justify-center", getStatusClasses(game.status))}>
                  {game.status}
                </Badge>
                {game.apiKeyMessage && game.status === 'APPROVED' && (
                  <p className="text-xs text-center text-muted-foreground bg-gray-800 p-2 rounded-md mt-2">
                    {game.apiKeyMessage}
                  </p>
                )}
              </div>
            ) : (
              <div className="text-sm text-primary font-semibold">
                View Details &rarr;
              </div>
            )}
          </CardFooter>
        </Card>
      </Link>
    </motion.div>
  );
}
