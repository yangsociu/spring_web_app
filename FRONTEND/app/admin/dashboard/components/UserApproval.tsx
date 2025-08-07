"use client";

import { useEffect, useState } from "react";
import { getPendingUsers, approveUser } from "@/lib/api";
import type { User } from "@/lib/types";
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
import {
Dialog,
DialogContent,
DialogDescription,
DialogHeader,
DialogTitle,
DialogTrigger,
} from "@/components/ui/dialog";
import { Loader2 } from 'lucide-react';

export function UserApproval() {
const [pendingUsers, setPendingUsers] = useState<User[]>([]);
const [loading, setLoading] = useState(true);
const { toast } = useToast();

const fetchUsers = async () => {
  setLoading(true);
  try {
    const users = await getPendingUsers();
    setPendingUsers(users);
  } catch (error) {
    toast({
      variant: "destructive",
      title: "Error",
      description: "Failed to fetch pending users.",
    });
  } finally {
    setLoading(false);
  }
};

useEffect(() => {
  fetchUsers();
}, []);

const handleApproval = async (userId: number, status: "APPROVED" | "REJECTED") => {
  try {
    await approveUser(userId, status);
    toast({
      title: "Success",
      description: `User has been ${status.toLowerCase()}.`,
    });
    fetchUsers(); // Refresh the list
  } catch (error: any) {
    toast({
      variant: "destructive",
      title: "Error",
      description: error.message || "Failed to update user status.",
    });
  }
};

if (loading) {
  return <div className="flex justify-center items-center p-8"><Loader2 className="h-8 w-8 animate-spin" /></div>;
}

return (
  <div>
    <h2 className="text-2xl font-semibold my-4">Pending User Approvals</h2>
    {pendingUsers.length > 0 ? (
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Email</TableHead>
            <TableHead>Role</TableHead>
            <TableHead>Full Name</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {pendingUsers.map((user) => (
            <TableRow key={user.id}>
              <TableCell>{user.email}</TableCell>
              <TableCell>
                <Badge variant="outline">{user.role}</Badge>
              </TableCell>
              <TableCell>{user.fullName}</TableCell>
              <TableCell className="space-x-2">
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">View Details</Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>User Details</DialogTitle>
                      <DialogDescription>
                        Review the user's information before making a decision.
                      </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                      <p><strong>Email:</strong> {user.email}</p>
                      <p><strong>Full Name:</strong> {user.fullName}</p>
                      <p><strong>Role:</strong> {user.role}</p>
                      <p><strong>Portfolio:</strong> <a href={user.portfolioUrl} target="_blank" rel="noopener noreferrer" className="text-blue-400 hover:underline">{user.portfolioUrl}</a></p>
                      <p><strong>Experience:</strong> {user.experienceYears} years</p>
                    </div>
                  </DialogContent>
                </Dialog>
                <Button size="sm" onClick={() => handleApproval(user.id, "APPROVED")}>Approve</Button>
                <Button variant="destructive" size="sm" onClick={() => handleApproval(user.id, "REJECTED")}>Reject</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    ) : (
      <p className="text-muted-foreground">No pending user approvals.</p>
    )}
  </div>
);
}
