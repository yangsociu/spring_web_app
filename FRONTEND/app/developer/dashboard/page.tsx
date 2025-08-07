"use client";

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { GameUploadForm } from "./components/GameUploadForm";
import { MyGamesList } from "./components/MyGamesList";
import { PageHeader } from "@/components/layout/page-header";
import { motion } from "framer-motion";

export default function DeveloperDashboardPage() {
  return (
    <motion.div
      className="container mx-auto px-4 py-8"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <PageHeader
        title="Developer Dashboard"
        description="Manage your game submissions and view their status."
      />
      <Tabs defaultValue="my-games" className="mt-6">
        <TabsList className="grid w-full grid-cols-2 bg-gray-800">
          <TabsTrigger value="my-games">My Games</TabsTrigger>
          <TabsTrigger value="upload-game">Upload New Game</TabsTrigger>
        </TabsList>
        <TabsContent value="my-games">
          <MyGamesList />
        </TabsContent>
        <TabsContent value="upload-game">
          <GameUploadForm />
        </TabsContent>
      </Tabs>
    </motion.div>
  );
}
