import { Router, Request, Response } from "express";
import { searchGames } from "../services/rawg";
import { searchGameReviews } from "../services/tavily";
import { summarizeGameReviews } from "../services/gemini";
import { GameSummary } from "../services/gemini";
import { getGamePrices } from "../services/itad";
import authMiddleware from "../middleware/auth";
import prisma from "../prisma/client";

interface AuthRequest extends Request {
    user?: {
        id: number;
        email: string;
    }
}

const router = Router();

// Get saved games — must be before /:id routes or Express reads "saved" as an id
router.get("/saved", authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const userId = req.user!.id;
        const savedGames = await prisma.savedGame.findMany({
            where: { userId },
            orderBy: { createdAt: "desc" }
        });
        return res.json(savedGames);
    } catch (error) {
        console.error("Error fetching saved games:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

// Save a game
router.post("/save", authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const userId = req.user!.id;
        const { rawgId, title, coverImage, rating, released } = req.body;

        if (!rawgId || !title) {
            return res.status(400).json({ message: "rawgId and title are required" });
        }

        const savedGame = await prisma.savedGame.create({
            data: { userId, rawgId, title, coverImage, rating, released }
        });

        return res.status(201).json(savedGame);
    } catch (error: any) {
        if (error.code === "P2002") {
            return res.status(409).json({ message: "Game already saved" });
        }
        console.error("Error saving game:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

// Unsave a game
router.delete("/save/:rawgId", authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const userId = req.user!.id;
        const rawgId = parseInt(req.params["rawgId"] as string);

        await prisma.savedGame.delete({
            where: { userId_rawgId: { userId, rawgId } }
        });

        return res.json({ message: "Game removed from saved list" });
    } catch (error: any) {
        if (error.code === "P2025") {
            return res.status(404).json({ message: "Saved game not found" });
        }
        console.error("Error removing saved game:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

// Search for games
router.get("/search", async (req: Request, res: Response) => {
    try {
        const query = req.query.q as string;
        if (!query) {
            return res.status(400).json({ message: "Query parameter is required" });
        }
        const games = await searchGames(query);
        return res.json(games);
    } catch (error) {
        console.error("Error searching games:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

// Get game summary
router.get("/:id/summary", async (req: Request, res: Response) => {
    try {
        const id = req.params.id;
        const gameName = req.query.name as string;
        if (!id || !gameName) {
            return res.status(400).json({ message: "Game ID and name are required" });
        }
        const reviews = await searchGameReviews(gameName);
        const summary = await summarizeGameReviews(gameName, reviews);
        return res.json(summary);
    } catch (error) {
        console.error("Error summarizing game reviews:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

// Get game prices
router.get("/:id/prices", async (req: Request, res: Response) => {
    try {
        const gameName = req.query.name as string;
        if (!gameName) {
            return res.status(400).json({ message: "Game name is required" });
        }
        const deals = await getGamePrices(gameName);
        return res.json(deals);
    } catch (error) {
        console.error("Error getting game prices:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

export default router;