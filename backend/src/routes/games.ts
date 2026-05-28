import { Router, Request, Response } from "express";
import { searchGames } from "../services/rawg";

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

export default router;
