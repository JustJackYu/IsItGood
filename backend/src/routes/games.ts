import { Router, Request, Response } from "express";
import { searchGames } from "../services/rawg";
import { searchGameReviews } from "../services/tavily";
import { summarizeGameReviews } from "../services/gemini";
import { GameSummary } from "../services/gemini";
import { getGamePrices } from "../services/itad";

const router = Router();

// Search for games in RAWG
router.get("/search", async (req: Request, res: Response) => {
    try {
        const query = req.query.q as string;
        if (!query) {
            return res.status(400).json({ message: "Query parameter is required" });
        }

        const games = await searchGames(query);
        return res.json(games);
    }
    catch (error) {
        console.error("Error searching games:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
})

// Get game summary from RAWG
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
    }
    catch (error) {
        console.error("Error summarizing game reviews:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
})

// Get game prices from ITAD
router.get("/:id/prices", async (req: Request, res: Response) => {
    try {
        const gameName = req.query.name as string;

        if (!gameName) {
            return res.status(400).json({ message: "Game name is required" });
        }

        const deals = await getGamePrices(gameName);

        return res.json(deals);
    }
    catch (error) {
        console.error("Error getting game prices:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
})

export default router;
