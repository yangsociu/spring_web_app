"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { useToast } from "@/components/ui/use-toast";
import { createGame } from "@/lib/api";
import { Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

type FormData = {
name: string;
description: string;
requirements: string;
previewImage: FileList;
apkFileUrl: string; // Changed from apkFile
supportLeaderboard: boolean;
supportPoints: boolean;
};

export function GameUploadForm() {
const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>();
const [loading, setLoading] = useState(false);
const { toast } = useToast();

const onSubmit = async (data: FormData) => {
  setLoading(true);
  const formData = new FormData();
  formData.append("name", data.name);
  formData.append("description", data.description);
  formData.append("requirements", data.requirements);
  formData.append("previewImage", data.previewImage[0]);
  formData.append("apkFileUrl", data.apkFileUrl); // Changed to send the URL
  formData.append("supportLeaderboard", String(data.supportLeaderboard));
  formData.append("supportPoints", String(data.supportPoints));

  try {
    await createGame(formData);
    toast({
      title: "Game Submitted!",
      description: "Your game has been submitted for approval.",
    });
    reset();
  } catch (error: any) {
    toast({
      variant: "destructive",
      title: "Submission Failed",
      description: error.message || "Could not submit the game.",
    });
  } finally {
    setLoading(false);
  }
};

return (
  <Card className="mt-6">
    <CardHeader>
      <CardTitle>Upload a New Game</CardTitle>
      <CardDescription>Fill out the details below to submit your game for review.</CardDescription>
    </CardHeader>
    <CardContent>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="space-y-2">
          <Label htmlFor="name">Game Name</Label>
          <Input id="name" {...register("name", { required: "Game name is required" })} />
          {errors.name && <p className="text-sm text-destructive">{errors.name.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Description</Label>
          <Textarea id="description" {...register("description")} />
        </div>
        <div className="space-y-2">
          <Label htmlFor="requirements">System Requirements</Label>
          <Textarea id="requirements" {...register("requirements")} />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <Label htmlFor="previewImage">Preview Image (PNG, JPG)</Label>
            <Input id="previewImage" type="file" accept="image/png, image/jpeg" {...register("previewImage", { required: "Preview image is required" })} />
            {errors.previewImage && <p className="text-sm text-destructive">{errors.previewImage.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="apkFileUrl">Game Download Link</Label>
            <Input id="apkFileUrl" type="url" placeholder="https://example.com/game.apk" {...register("apkFileUrl", { required: "Download link is required" })} />
            {errors.apkFileUrl && <p className="text-sm text-destructive">{errors.apkFileUrl.message}</p>}
          </div>
        </div>
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <Checkbox id="supportLeaderboard" {...register("supportLeaderboard")} />
            <Label htmlFor="supportLeaderboard">Support Leaderboard</Label>
          </div>
          <div className="flex items-center space-x-2">
            <Checkbox id="supportPoints" {...register("supportPoints")} />
            <Label htmlFor="supportPoints">Support Points</Label>
          </div>
        </div>
        <Button type="submit" disabled={loading} className="w-full sm:w-auto">
          {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Submit for Approval
        </Button>
      </form>
    </CardContent>
  </Card>
);
}
