import { Router, Request, Response } from "express";
import { chatAboutGame } from "../services/gemini";
import { searchWebContext } from "../services/tavily";
import authMiddleware from "../middleware/auth";
import { getEffectivePreferences } from "../services/preferences";

interface AuthRequest extends Request {
    user?: {
        id: number;
        email: string;
    }
}

const router = Router();

router.post("/", authMiddleware, async (req: AuthRequest, res: Response) => {
    const { message, gameTitle, summary, history } = req.body;

    if (!message || !gameTitle || !summary || !history) {
        return res.status(400).json({ message: "Missing required fields" });
    }

    if (!Array.isArray(history)) {
        return res.status(400).json({ message: "History is invalid" });
    }

    try {
        const prefs = await getEffectivePreferences(req.user!.id);

        let webContext: Array<{ content: string; url: string }> = [];
        try {
            webContext = await searchWebContext(`${gameTitle} ${message}`);
        } catch (e) {
            console.warn("Web search failed, continuing without web context:", e);
        }

        const response = await chatAboutGame(message, gameTitle, summary, history, prefs, webContext);
        return res.json({ reply: response });
    } catch (error) {
        console.error("Error chatting about game:", error);
        return res.status(500).json({ message: "Internal server error" });
    }
});

export default router;