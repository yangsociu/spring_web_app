import { type NextRequest, NextResponse } from "next/server"

export async function POST(request: NextRequest) {
  try {
    const authHeader = request.headers.get("authorization");
    console.log("AUTH HEADER:", authHeader);

    if (!authHeader) {
      return NextResponse.json({ message: "Authorization header required" }, { status: 401 });
    }

    const rawBody = await request.json();
const body = {
  userId: rawBody.userId,
  status: rawBody.approved ? "APPROVED" : "REJECTED",
};
    console.log("BODY:", body);

    const url = `${process.env.BACKEND_URL}/api/v1/admin/approve`;
    console.log("Calling backend URL:", url);

    const response = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: authHeader,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const contentType = response.headers.get("content-type");

    let data;
    try {
      if (contentType && contentType.includes("application/json")) {
        data = await response.json();
      } else {
        data = { message: await response.text() };
      }
    } catch (error) {
      console.error("Failed to parse response:", error);
      data = { message: "Invalid or malformed response from backend." };
    }

    if (!response.ok) {
      console.error("Backend responded with error:", data);
      return NextResponse.json(data, { status: response.status });
    }

    return NextResponse.json(data);
  } catch (error) {
    console.error("Approve user API error:", error);
    return NextResponse.json({ message: "Internal server error" }, { status: 500 });
  }
}
