import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import Image from "next/image";

export function HeroSection() {
  return (
    <div className="relative h-[60vh] min-h-[500px] w-full flex items-center justify-center text-center">
      <Image
        src="/futuristic-game-world.png"
        alt="Hero background"
        fill
        className="object-cover"
        priority
      />
      <div className="absolute inset-0 bg-black/60" />
      <motion.div
        className="relative z-10 max-w-3xl px-4"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7, ease: "easeOut" }}
      >
        <h1 className="text-4xl font-extrabold tracking-tight text-white sm:text-5xl md:text-6xl">
          Discover Your Next Favorite Game
        </h1>
        <p className="mt-6 text-lg leading-8 text-gray-300">
          Explore a universe of games crafted by independent developers. Find hidden gems, read reviews, and join the community.
        </p>
        <div className="mt-10">
          <Button size="lg" asChild>
            <a href="#latest-games">Explore Games</a>
          </Button>
        </div>
      </motion.div>
    </div>
  );
}
