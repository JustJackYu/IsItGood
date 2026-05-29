import { Router, Request, Response } from "express";
import { searchGames } from "../services/rawg";
import { searchGameReviews } from "../services/tavily";
import { summarizeGameReviews } from "../services/gemini";
import { GameSummary } from "../services/gemini";

const router = Router();

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

export default router;
