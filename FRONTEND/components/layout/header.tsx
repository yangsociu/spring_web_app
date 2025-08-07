"use client";

import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Gamepad2 } from 'lucide-react';
import { cn } from "@/lib/utils";
import { usePathname } from "next/navigation";

export function Header() {
  const { user, logout } = useAuth();
  const pathname = usePathname();

  const getDashboardLink = () => {
    if (!user) return "/";
    return `/${user.role.toLowerCase()}/dashboard`;
  };

  return (
    <header className={cn(
      "sticky top-0 z-50 w-full border-b border-gray-800",
      pathname === '/' ? "bg-transparent" : "bg-gray-950/80 backdrop-blur-sm"
    )}>
      <div className="container flex h-16 items-center">
        <Link href="/" className="mr-6 flex items-center space-x-2">
          <Gamepad2 className="h-6 w-6 text-primary" />
          <span className="font-bold text-lg">GameHub</span>
        </Link>
        <div className="flex flex-1 items-center" />
        <div className="flex items-center space-x-2">
          {user ? (
            <>
              <Button variant="ghost" asChild>
                <Link href={getDashboardLink()}>Dashboard</Link>
              </Button>
              <Button onClick={logout} variant="secondary">Logout</Button>
            </>
          ) : (
            <>
              <Button variant="ghost" asChild>
                <Link href="/login">Login</Link>
              </Button>
              <Button asChild>
                <Link href="/register">Sign Up</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
