"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
Card,
CardContent,
CardDescription,
CardHeader,
CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
Select,
SelectContent,
SelectItem,
SelectTrigger,
SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/components/ui/use-toast";
import { registerUser } from "@/lib/api";
import { Loader2 } from 'lucide-react';

export default function RegisterPage() {
const [email, setEmail] = useState("");
const [password, setPassword] = useState("");
const [role, setRole] = useState("PLAYER");
const [fullName, setFullName] = useState("");
const [portfolioUrl, setPortfolioUrl] = useState("");
const [experienceYears, setExperienceYears] = useState<number | string>("");
const [loading, setLoading] = useState(false);
const { toast } = useToast();
const router = useRouter();

const isProfessionalRole = role === "DEVELOPER" || role === "DESIGNER";

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);

  const registrationData: any = {
    email,
    password,
    role,
  };

  if (isProfessionalRole) {
    registrationData.fullName = fullName;
    registrationData.portfolioUrl = portfolioUrl;
    registrationData.experienceYears = Number(experienceYears);
  }

  try {
    await registerUser(registrationData);
    if (isProfessionalRole) {
      toast({
        title: "Registration Submitted",
        description: "Your account is pending approval from an administrator. You will be notified upon approval.",
      });
      router.push("/login");
    } else {
      toast({
        title: "Registration Successful",
        description: "You can now log in with your new account.",
      });
      router.push("/login");
    }
  } catch (error: any) {
    toast({
      variant: "destructive",
      title: "Registration Failed",
      description: error.message || "An unexpected error occurred.",
    });
  } finally {
    setLoading(false);
  }
};

return (
  <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center p-4">
    <Card className="mx-auto max-w-sm w-full">
      <CardHeader>
        <CardTitle className="text-xl">Sign Up</CardTitle>
        <CardDescription>
          Enter your information to create an account
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="grid gap-4">
          <div className="grid gap-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="m@example.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="role">Role</Label>
            <Select value={role} onValueChange={setRole}>
              <SelectTrigger>
                <SelectValue placeholder="Select a role" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="PLAYER">Player</SelectItem>
                <SelectItem value="DEVELOPER">Developer</SelectItem>
                <SelectItem value="DESIGNER">Designer</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {isProfessionalRole && (
            <>
              <div className="grid gap-2">
                <Label htmlFor="fullName">Full Name</Label>
                <Input
                  id="fullName"
                  placeholder="John Doe"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="portfolioUrl">Portfolio URL</Label>
                <Input
                  id="portfolioUrl"
                  placeholder="https://your-portfolio.com"
                  required
                  value={portfolioUrl}
                  onChange={(e) => setPortfolioUrl(e.target.value)}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="experienceYears">Years of Experience</Label>
                <Input
                  id="experienceYears"
                  type="number"
                  placeholder="3"
                  required
                  value={experienceYears}
                  onChange={(e) => setExperienceYears(e.target.value)}
                />
              </div>
            </>
          )}

          <Button type="submit" className="w-full" disabled={loading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Create an account
          </Button>
        </form>
        <div className="mt-4 text-center text-sm">
          Already have an account?{" "}
          <Link href="/login" className="underline">
            Sign in
          </Link>
        </div>
      </CardContent>
    </Card>
  </div>
);
}
